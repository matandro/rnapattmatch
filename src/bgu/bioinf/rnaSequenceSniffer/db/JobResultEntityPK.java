package bgu.bioinf.rnaSequenceSniffer.db;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by matan on 27/11/14.
 */
public class JobResultEntityPK implements Serializable {
    private String jobId;
    private int targetNo;
    private int resultNo;

    @Column(name = "JobId")
    @Id
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Column(name = "TargetNo")
    @Id
    public int getTargetNo() {
        return targetNo;
    }

    public void setTargetNo(int targetNo) {
        this.targetNo = targetNo;
    }

    @Column(name = "ResultNo")
    @Id
    public int getResultNo() {
        return resultNo;
    }

    public void setResultNo(int resultNo) {
        this.resultNo = resultNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobResultEntityPK that = (JobResultEntityPK) o;

        if (jobId != that.jobId) return false;
        if (resultNo != that.resultNo) return false;
        if (targetNo != that.targetNo) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (jobId != null) ? jobId.hashCode() : 0;
        result = 31 * result + targetNo;
        result = 31 * result + resultNo;
        return result;
    }
}
