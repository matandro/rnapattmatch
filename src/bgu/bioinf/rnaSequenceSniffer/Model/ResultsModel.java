package bgu.bioinf.rnaSequenceSniffer.Model;

import bgu.bioinf.rnaSequenceSniffer.db.JobEntity;
import bgu.bioinf.rnaSequenceSniffer.db.JobResultEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by matan on 10/12/14.
 * Results model call via jstl
 */
public class ResultsModel {
    private String jobId;
    private JobEntity jobEntity;
    private Map<Integer, List<JobResultEntity>> results;
    private String error;

    public boolean isSecondSearch() {
        return secondSearch;
    }

    private boolean secondSearch;

    public long getTotalNoOfResults() {
        return totalNoOfResults;
    }

    private long totalNoOfResults;
    private int page;

    public long getMaxResults() {
        return maxResults;
    }

    public int getPage() {
        return page;
    }

    public String getStartTime() {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(jobEntity.getStartTime());
    }

    private long maxResults;

    public ResultsModel(String jobId, JobEntity jobEntity, Map<Integer, List<JobResultEntity>> results, String error,
                        long totalNoOfResults, int page, long maxResults, boolean readyForSecondSearch) {
        this.jobId = jobId;
        this.jobEntity = jobEntity;
        this.results = results;
        this.error = error;
        this.totalNoOfResults = totalNoOfResults;
        this.page = page;
        this.maxResults = maxResults;
        this.secondSearch = readyForSecondSearch;
    }

    public JobEntity getJobEntity() {
        return jobEntity;
    }

    public Map<Integer, List<JobResultEntity>> getResults() {
        return results;
    }

    public String getError() {
        return error;
    }

    public String getJobId() {
        return jobId;
    }

    public boolean isReady() {
        return jobEntity.getEndTime() != null;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) getTotalNoOfResults() / getMaxResults());
    }
}
