package bgu.bioinf.rnaSequenceSniffer.webInterface;

import bgu.bioinf.rnaSequenceSniffer.Model.JobInformation;
import bgu.bioinf.rnaSequenceSniffer.algorithmControl.JobRunner;
import bgu.bioinf.rnaSequenceSniffer.algorithmControl.MailDispatcher;
import bgu.bioinf.rnaSequenceSniffer.db.DBConnector;
import bgu.bioinf.rnaSequenceSniffer.db.JobBpMatrixEntity;
import bgu.bioinf.rnaSequenceSniffer.db.JobEntity;
import bgu.bioinf.rnaSequenceSniffer.db.JobErrorEntity;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.bio.db.ncbi.GenbankRichSequenceDB;
import org.biojavax.bio.seq.RichSequence;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by matan on 27/11/14.
 * <p/>
 * Submits job information to database and causes the search to start
 */
public class JobSubmitter {
    private JobInformation jobInformation;
    private String localError;

    public JobSubmitter(JobInformation jobInformation) {
        anFileError = false;
        this.jobInformation = jobInformation;
        localError = "";
    }

    public String getError() {
        return localError;
    }

    private void addError(String error) {
        if ("".equals(localError)) {
            localError = "The following errors have occurred:";
        }
        localError += "\n" + error;
    }

    public boolean submit() {
        boolean success;
        success = insertJobInfo();
        if (success) {
            success = submitTask();
            if (success) {
                addError("Failed to submit task for calculation, try again later");
            }
        }
        return success;
    }

    public boolean sendEmail() {
        return MailDispatcher.submissionMail(jobInformation);
    }

    /**
     * Submit calculation task to runnable
     *
     * @return True if submission was successful, False otherwise
     */
    private boolean submitTask() {
        boolean submitted = false;
        try {
            JobRunner jobRunner = new JobRunner(jobInformation);
            WebappContextListener.jobExecutor.submit(jobRunner);
            submitted = true;
        } catch (Exception e) {
            addError("Failed to submit job for calculation");
        }
        return submitted;
    }

    private static final String LEGAL_ID_LETTERS = "QWERTYUIOPLKJHGFDSAZXCVBNM1234567890";

    private String generateRandomUnusedId(EntityManager em) {
        String randomId = null;
        boolean success = false;
        while (!success) {
            randomId = "";
            for (int i = 0; i < 8; ++i) {
                int index = WebappContextListener.rnGesus.nextInt(LEGAL_ID_LETTERS.length());
                randomId += LEGAL_ID_LETTERS.charAt(index);
            }
            JobEntity je = em.find(JobEntity.class, randomId);
            if (je == null) {
                success = true;
            }
        }
        return randomId;
    }

    public static final String FILE_STATUS_ERROR = "ERROR";
    public static final String FILE_STATUS_DOWNLOADING = "DOWNLOAD";

    private boolean insertJobInfo() {
        boolean inserted = false;

        Timestamp now = new Timestamp(new Date().getTime());
        EntityManager em = null;
        EntityTransaction et = null;
        try {
            em = DBConnector.getEntityManager();
            et = em.getTransaction();
            et.begin();

            JobEntity jobEntity = new JobEntity();
            jobEntity.setStartTime(now);
            jobEntity.setQueryName(jobInformation.getQueryName());
            jobEntity.setQuerySequence(jobInformation.getQuerySequence());
            jobEntity.setQueryStructure(jobInformation.getQueryStructure());
            jobEntity.setEmail(jobInformation.getEmail());

            // Locking so we don't insert and delete at the same time, cant release until committed in DB
            WebappContextListener.maintenanceLock.lock();
            if (jobInformation.getTargetType() == JobInformation.TargetType.CACHED) {
                JobEntity sourceJob = em.find(JobEntity.class, jobInformation.getCacheId());
                if (sourceJob == null) {
                    addError("Could not find source job, it may have been deleted. If results for source job are available contact us by mail.");
                    return false;
                }
                jobInformation.setTargetFile(sourceJob.getTargetFile());
            } else if (jobInformation.getTargetType() == JobInformation.TargetType.AN) {
                // get sourceJob such that file target = AN number
                jobEntity.setTargetFile(jobInformation.getTargetFile());

                while (isAnFileReady(em, jobEntity.getTargetFile())) {
                    try {
                        // check every 5 minutes so i can kill threads in case of error in DB
                        WebappContextListener.conditionMaintenanceLock.await(5, TimeUnit.MINUTES);
                    } catch (InterruptedException ignore) {
                        // if something is screwing with out wait, busy wait...
                    }
                }
                if (anFileError) {
                    addError("Error downloading sequence associated with " +
                            jobInformation.getTargetFile().substring(jobInformation.getTargetFile().lastIndexOf(File.separatorChar) + 1));
                    throw new Exception();
                }
                File anFile = new File(jobInformation.getTargetFile());
                if (!anFile.exists()) {
                    jobEntity.setTargetFileStatus(FILE_STATUS_DOWNLOADING);
                }
            }

            jobEntity.setTargetFile(jobInformation.getTargetFile());
            // Locking to avoid race condition on same random id
            synchronized (WebappContextListener.rnGesus) {
                jobInformation.setJobId(generateRandomUnusedId(em));
                jobEntity.setJobId(jobInformation.getJobId());
                em.persist(jobEntity);
                JobBpMatrixEntity jobBpMatrixEntity = new JobBpMatrixEntity();
                jobBpMatrixEntity.setJobId(jobInformation.getJobId());
                jobBpMatrixEntity.setAc(jobInformation.getBasePairValue("AC"));
                jobBpMatrixEntity.setAg(jobInformation.getBasePairValue("AG"));
                jobBpMatrixEntity.setAu(jobInformation.getBasePairValue("AU"));
                jobBpMatrixEntity.setCg(jobInformation.getBasePairValue("CG"));
                jobBpMatrixEntity.setCu(jobInformation.getBasePairValue("CU"));
                jobBpMatrixEntity.setGu(jobInformation.getBasePairValue("GU"));
                if (!et.isActive())
                    et.begin();
                em.persist(jobBpMatrixEntity);
                et.commit();
            }

            if (FILE_STATUS_DOWNLOADING.equals(jobEntity.getTargetFileStatus())) {
                JobErrorEntity jobErrorEntity = null;
                downloadANFile(jobEntity.getTargetFile());
                if (anFileError) {
                    // write error to DB
                    jobEntity.setTargetFileStatus(FILE_STATUS_ERROR);
                    jobEntity.setEndTime(new Timestamp(new Date().getTime()));
                    jobErrorEntity = new JobErrorEntity();
                    jobErrorEntity.setJobId(jobEntity.getJobId());
                    jobErrorEntity.setErrorStr(getError());
                } else {
                    jobEntity.setTargetFileStatus("");
                }
                WebappContextListener.conditionMaintenanceLock.signalAll();
                if (!et.isActive())
                    et.begin();
                em.persist(jobEntity);
                if (jobErrorEntity != null)
                    em.persist(jobErrorEntity);
                et.commit();
            }

            if (!anFileError)
                inserted = true;
        } catch (Exception e) {
            e.printStackTrace();
            if (et != null && et.isActive()) {
                try {
                    et.rollback();
                } catch (Exception ignore) {
                }
            }
        } finally {
            try {
                WebappContextListener.maintenanceLock.unlock();
            } catch (Exception ignore) {
            }
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }

        return inserted;
    }

    /**
     * must contain lock and commit DOWNLOADING on the file status
     *
     * @param targetFile the file to write the an sequence to
     */
    private void downloadANFile(String targetFile) {
        String an = targetFile.substring(targetFile.lastIndexOf(File.separatorChar) + 1);
        RichSequence rs = null;
        try {
            WebappContextListener.maintenanceLock.unlock();
            GenbankRichSequenceDB grsdb = new GenbankRichSequenceDB();
            rs = grsdb.getRichSequence(an);
            System.out.println(rs.getName() + " | " + rs.getDescription());
            SymbolList sl = rs.getInternalSymbolList();
            if (sl.length() > 0) {
                PrintWriter writer = new PrintWriter(targetFile);
                writer.println("> " + rs.getName().replaceAll("\n", " ") + " | " + rs.getDescription().replaceAll("\n", " "));
                writer.println(sl.seqString());
            } else {
                anFileError = true;
                addError("No sequence for accession number " + an);
            }
        } catch (BioException be) {
            be.printStackTrace();
            anFileError = true;
            addError("Accession number " + an + " does not exist");
        } catch (IOException e) {
            e.printStackTrace();
            anFileError = true;
            addError("Failed to write file from genbank");
        } finally {
            try {
                WebappContextListener.maintenanceLock.lock();
            } catch (Exception ignore) {
            }
        }
    }

    public boolean isAnFileReady(EntityManager em, String targetFile) {
        TypedQuery<JobEntity> usedBefore = em.createNamedQuery("Job.GetJobByFileName", JobEntity.class);
        usedBefore.setParameter("targetFile", targetFile);
        boolean isDownloading = false;
        for (JobEntity sameFileJob : usedBefore.getResultList()) {
            if (FILE_STATUS_DOWNLOADING.equals(sameFileJob.getTargetFileStatus())) {
                isDownloading = true;
                break;
            } else if (FILE_STATUS_ERROR.equals(sameFileJob.getTargetFileStatus())) {
                anFileError = true;
            }
        }
        return isDownloading;
    }

    private boolean anFileError;
}
