package bgu.bioinf.rnaSequenceSniffer.db;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by matan on 27/11/14.
 */
public class JobTargetEntityPK implements Serializable {
    private String jobId;
    private int targetNo;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobTargetEntityPK that = (JobTargetEntityPK) o;

        if (jobId != that.jobId) return false;
        if (targetNo != that.targetNo) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (jobId != null) ? jobId.hashCode() : 0;
        result = 31 * result + targetNo;
        return result;
    }
}
