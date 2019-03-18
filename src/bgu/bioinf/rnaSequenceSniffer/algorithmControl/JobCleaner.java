package bgu.bioinf.rnaSequenceSniffer.algorithmControl;

import bgu.bioinf.rnaSequenceSniffer.Controllers.SubmitJobController;
import bgu.bioinf.rnaSequenceSniffer.db.*;
import bgu.bioinf.rnaSequenceSniffer.webInterface.WebappContextListener;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by matan on 12/04/15.
 */
public class JobCleaner implements Job {
    private static final String[] tempFiles = {"Thermoanaerobacter_tengcongensis_MB4.fna", "Bacillus_subtilis.fna"};

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy ss:mm:HH");
        System.out.println(simpleDateFormat.format(jobExecutionContext.getFireTime()) + " INFO Starting Cleanup");
        WebappContextListener.maintenanceLock.lock();
        EntityManager em = null;
        EntityTransaction et = null;
        Set<String> oldFiles = new HashSet<String>();
        try {
            em = DBConnector.getEntityManager();
            et = em.getTransaction();
            if (!et.isActive())
                et.begin();
            TypedQuery<JobEntity> oldQuery = em.createNamedQuery("Job.GetWeekOldJobs", JobEntity.class);
            for (JobEntity oldJob : oldQuery.getResultList()) {
                System.out.println(simpleDateFormat.format(new Date()) + " INFO removing job id: "
                        + oldJob.getJobId() + " End Time:" + simpleDateFormat.format(oldJob.getEndTime()));
                // Add file to file list
                oldFiles.add(oldJob.getTargetFile());
                // remove job BP matrix
                JobBpMatrixEntity jobBpMatrixEntity = em.find(JobBpMatrixEntity.class, oldJob.getJobId());
                if (jobBpMatrixEntity != null)
                    em.remove(jobBpMatrixEntity);
                // remove job error
                JobErrorEntity jobErrorEntity = em.find(JobErrorEntity.class, oldJob.getJobId());
                if (jobErrorEntity != null)
                    em.remove(jobErrorEntity);
                // remove job results
                Query query = em.createNamedQuery("JobResults.RemoveAllResultsWithId");
                query.setParameter("jobId", oldJob.getJobId());
                query.executeUpdate();
                // remove job target
                query = em.createNamedQuery("JobTarget.RemoveAllJobsWithId");
                query.setParameter("jobId", oldJob.getJobId());
                query.executeUpdate();
                // remove actual job
                em.remove(oldJob);
            }
            et.commit();
            System.out.println(simpleDateFormat.format(new Date()) + " INFO cleanup commit successful");

            Query fileQuery = em.createQuery("SELECT DISTINCT targetFile FROM JobEntity");
            for (Object fileName : fileQuery.getResultList()) {
                String fileNameStr = (String) fileName;
                oldFiles.remove(fileNameStr);
            }

            for (String fileName : oldFiles) {
                System.out.println(simpleDateFormat.format(new Date()) + " INFO cleanup delete file: " + fileName);
                File file = new File(fileName);
                if (file.exists() &&
                        !isTempFile(fileName)) {
                    file.delete();
                }
            }
            WebappContextListener.maintenanceLock.unlock();
            synchronized (WebappContextListener.cacheLock) {
                if (!et.isActive())
                    et.begin();
                for (String fileName : oldFiles) {
                    CachedJobsEntity cachedJobsEntity = em.find(CachedJobsEntity.class, fileName);
                    if (cachedJobsEntity != null) {
                        em.remove(cachedJobsEntity);
                    } else {
                        System.out.println(simpleDateFormat.format(new Date())
                                + " WARNING cleanup delete Cache entry: ["
                                + fileName + "] not found in DB");
                    }
                    String cleanFileName = fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1);
                    cleanFileName = SubmitJobController.TEMP_LOCATION + SubmitJobController.CACHE_IN_TEMP + cleanFileName;
                    int targetIndex = 0;
                    while (true) {
                        try {
                            File file = new File(cleanFileName + "_" + targetIndex + "_FOR.SFA");
                            if (!file.exists())
                                break;
                            System.out.println(simpleDateFormat.format(new Date()) + " INFO cleanup delete Cache file: "
                                    + cleanFileName + " target index:" + targetIndex);
                            file.delete();
                            file = new File(cleanFileName + "_" + targetIndex + "_REV.SFA");
                            file.delete();
                            file = new File(cleanFileName + "_" + targetIndex + ".AFLK");
                            file.delete();
                        } catch (Exception e) {
                            break;
                        }
                        targetIndex++;
                    }
                }
                et.commit();
            }
        } catch (Exception e) {
            System.out.println(simpleDateFormat.format(new Date()) + " WARNING cleanup failed!");
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
        System.out.println(simpleDateFormat.format(new Date()) + " INFO Finished Cleanup");
    }

    private boolean isTempFile(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1).trim();
        if (tempFiles[0].equals(fileName)
                || tempFiles[1].equals(fileName))
            return true;
        return false;
    }

}
