package bgu.bioinf.rnaSequenceSniffer.webInterface;

import bgu.bioinf.rnaSequenceSniffer.db.DBConnector;
import bgu.bioinf.rnaSequenceSniffer.db.JobEntity;
import bgu.bioinf.rnaSequenceSniffer.db.JobResultEntity;
import bgu.bioinf.rnaSequenceSniffer.db.JobResultEntityPK;

import javax.persistence.EntityManager;

/**
 * Created by matan on 23/12/14.
 */
public class ResultRetriever {
    private JobResultEntity jobResultEntity;
    private JobEntity jobEntity;
    private String jobId;
    private int targetNo;
    private int resultNo;

    public ResultRetriever(String jobId, int targetNo, int resultNo) {
        this.jobId = jobId;
        this.targetNo = targetNo;
        this.resultNo = resultNo;
    }

    public boolean init() {
        boolean result = false;
        EntityManager em = null;
        try {
            em = DBConnector.getEntityManager();
            JobResultEntityPK jobResultEntityPK = new JobResultEntityPK();
            jobResultEntityPK.setJobId(jobId);
            jobResultEntityPK.setTargetNo(targetNo);
            jobResultEntityPK.setResultNo(resultNo);
            jobResultEntity = em.find(JobResultEntity.class, jobResultEntityPK);
            if (jobResultEntity != null) {
                result = (jobEntity = em.find(JobEntity.class, jobId)) != null;
            }
        } catch (Exception ignore) {
        } finally {
            if (em != null) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }
        return result;
    }

    public double getEnergyScore() {
        return jobResultEntity.getEnergyScore();
    }

    public String getSequence() {
        return jobResultEntity.getResultSequence();
    }

    public String getAlignedStructure() {
        return jobResultEntity.getStructureAlignment(jobEntity.getQueryStructure());
    }

    private static final int MAX_TARGET_NAME_TAKEN = 20;

    public String getTopic() {
        int targetNameLengthTaken = Math.min(MAX_TARGET_NAME_TAKEN, jobResultEntity.getJobTargetEntity().getTargetName().length());
        String result = "dG = " + jobResultEntity.getEnergyScore();
        result += " - " + jobEntity.getQueryName();
        result += ": " + jobResultEntity.getJobTargetEntity().getTargetName().
                substring(0, targetNameLengthTaken);
        if (targetNameLengthTaken < jobResultEntity.getJobTargetEntity().getTargetName().length()) {
            result += "...";
        }
        return result;
    }

    public int getStartIndex() {
        return jobResultEntity.getStartIndex();
    }

    public String getGapString() {
        return jobResultEntity.getGapsPrintable();
    }
}
