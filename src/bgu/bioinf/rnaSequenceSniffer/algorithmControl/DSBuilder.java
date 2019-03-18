package bgu.bioinf.rnaSequenceSniffer.algorithmControl;

import bgu.bioinf.rnaSequenceSniffer.Controllers.SubmitJobController;
import bgu.bioinf.rnaSequenceSniffer.Model.JobInformation;
import bgu.bioinf.rnaSequenceSniffer.db.CachedJobsEntity;
import bgu.bioinf.rnaSequenceSniffer.db.DBConnector;
import bgu.bioinf.rnaSequenceSniffer.db.SumEntity;
import bgu.bioinf.rnaSequenceSniffer.webInterface.WebappContextListener;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matan on 07/04/15.
 */
public class DSBuilder implements Runnable {
    // Max 15 GB of cache
    public static final long MAX_CACHE_ALLOWED = (long) 15 * (long) 1024 * (long) 1024 * (long) 1024;
    private static final String BUILDER_LOCATION = JobRunner.ALG_LOCATION + "RNASequenceSniffer/bin/RNAdsBuilder";
    private JobInformation jobInformation;
    private List<CachedJobsEntity> toDrop;

    public DSBuilder(JobInformation jobInformation) {
        this.jobInformation = jobInformation;
    }

    @Override
    public void run() {
        EntityManager em = null;
        EntityTransaction et = null;
        try {
            em = DBConnector.getEntityManager();
            et = em.getTransaction();
            int exitVal = -1;

            String runningLine = BUILDER_LOCATION;
            File file = new File(jobInformation.getTargetFile());
            runningLine += " " + SubmitJobController.TEMP_LOCATION + SubmitJobController.CACHE_IN_TEMP + file.getName();
            runningLine += " " + jobInformation.getTargetFile();

            try {
                if (makeRoom(em, et)) {
                    Process p = Runtime.getRuntime().exec(runningLine);
                    exitVal = p.waitFor();
                }
            } catch (Exception ignore) {
            }

            CachedJobsEntity cachedJobsEntity = em.find(CachedJobsEntity.class, jobInformation.getTargetFile());
            if (!et.isActive())
                et.begin();
            if (exitVal != 0) {
                // failed to build, remove line
                em.remove(cachedJobsEntity);
            } else {
                // set cache as ready and remove oldies
                cachedJobsEntity.setStatus(JobRunner.CACHEJOBS_STATUS_READY);
                em.persist(cachedJobsEntity);
            }
            et.commit();
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    // notice that an error will delete cached objects for nothing
    private boolean makeRoom(EntityManager em, EntityTransaction et) {
        Long totalSize = null;
        synchronized (WebappContextListener.cacheLock) {
            TypedQuery<SumEntity> getTotalQuery = em.createNamedQuery("CachedJobs.GetTotalSize", SumEntity.class);
            totalSize = getTotalQuery.getSingleResult().longValue();
            if (totalSize != null && totalSize > MAX_CACHE_ALLOWED) {
                TypedQuery<CachedJobsEntity> tryRemovingQuery = em.createNamedQuery("CachedJobs.GetReadyByLastUsed", CachedJobsEntity.class);
                List<CachedJobsEntity> allCached = tryRemovingQuery.getResultList();
                toDrop = new ArrayList<CachedJobsEntity>();
                for (CachedJobsEntity cachedJobsEntity : allCached) {
                    if (totalSize <= MAX_CACHE_ALLOWED)
                        break;
                    totalSize -= cachedJobsEntity.getSize();
                    toDrop.add(cachedJobsEntity);
                }
                if (totalSize <= MAX_CACHE_ALLOWED) {
                    if (!et.isActive())
                        et.begin();
                    // delete all sub file, and remove from DB
                    for (CachedJobsEntity cachedJobsEntity : toDrop) {
                        File file = new File(cachedJobsEntity.getIdentifier());
                        String filePaths = SubmitJobController.TEMP_LOCATION + SubmitJobController.CACHE_IN_TEMP + file.getName();
                        int targetIndex = 0;
                        while (true) {
                            File saFor = new File(filePaths + "_" + targetIndex + "_FOR.SFA");
                            File saRev = new File(filePaths + "_" + targetIndex + "_REV.SFA");
                            File aflk = new File(filePaths + "_" + targetIndex + ".AFLK");
                            targetIndex++;
                            if (!saFor.exists()) {
                                break;
                            }
                            try {
                                saFor.delete();
                                saRev.delete();
                                aflk.delete();
                            } catch (Exception e) {
                                System.err.println("Failed to remove data structure info " + filePaths + " target " + targetIndex);
                            }
                        }
                        em.remove(cachedJobsEntity);
                    }
                    et.commit();
                }
            }
        }
        return (totalSize == null || totalSize <= MAX_CACHE_ALLOWED);
    }
}
