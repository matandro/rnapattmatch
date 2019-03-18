package bgu.bioinf.rnaSequenceSniffer.db;

import javax.persistence.*;

/**
 * Created by matan on 27/11/14.
 */
@Entity
@Table(name = "JobTarget", schema = "", catalog = "RNASequenceSniffer")
@IdClass(JobTargetEntityPK.class)
@NamedNativeQueries({
        @NamedNativeQuery(name = "JobTarget.RemoveAllJobsWithId",
                query = "DELETE FROM JobTarget " +
                        "WHERE JobId = :jobId")
})
public class JobTargetEntity {
    private String jobId;
    private int targetNo;
    private String targetName;

    @Id
    @Column(name = "JobId")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Id
    @Column(name = "TargetNo")
    public int getTargetNo() {
        return targetNo;
    }

    public void setTargetNo(int targetNo) {
        this.targetNo = targetNo;
    }

    @Basic
    @Column(name = "TargetName")
    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobTargetEntity that = (JobTargetEntity) o;

        if (jobId != null ? jobId.equals(that.jobId) : that.jobId != null) return false;
        if (targetNo != that.targetNo) return false;
        if (targetName != null ? !targetName.equals(that.targetName) : that.targetName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (jobId != null) ? jobId.hashCode() : 0;
        result = 31 * result + targetNo;
        result = 31 * result + (targetName != null ? targetName.hashCode() : 0);
        return result;
    }
}
