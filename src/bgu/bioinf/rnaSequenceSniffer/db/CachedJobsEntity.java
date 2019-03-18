package bgu.bioinf.rnaSequenceSniffer.db;

import bgu.bioinf.rnaSequenceSniffer.algorithmControl.JobRunner;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by matan on 07/04/15.
 */
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "CachedJobs.GetTotalSize",
                query = "Select sum(CachedJobs.Size) as SUM " +
                        "From CachedJobs",
                resultClass = SumEntity.class
        ),
        @NamedNativeQuery(
                name = "CachedJobs.GetReadyByLastUsed",
                query = "Select CachedJobs.* " +
                        "From CachedJobs " +
                        "Where CachedJobs.Status = '" + JobRunner.CACHEJOBS_STATUS_READY + "' " +
                        "AND CachedJobs.UsingNowCount = 0 " +
                        "Order By CachedJobs.LastUse DESC",
                resultClass = CachedJobsEntity.class
        )
})
@Entity
@Table(name = "CachedJobs", schema = "", catalog = "RNASequenceSniffer")
public class CachedJobsEntity {
    private String identifier;
    private Timestamp lastUse;
    private String status;
    private Long size;
    private Integer usingNowCount;

    @Id
    @Column(name = "Identifier")
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Basic
    @Column(name = "LastUse")
    public Timestamp getLastUse() {
        return lastUse;
    }

    public void setLastUse(Timestamp lastUse) {
        this.lastUse = lastUse;
    }

    @Basic
    @Column(name = "Status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedJobsEntity that = (CachedJobsEntity) o;

        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
        if (lastUse != null ? !lastUse.equals(that.lastUse) : that.lastUse != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (usingNowCount != null ? !usingNowCount.equals(that.status) : that.status != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (lastUse != null ? lastUse.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (usingNowCount != null ? usingNowCount.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "Size")
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Basic
    @Column(name = "UsingNowCount")
    public Integer getUsingNowCount() {
        return usingNowCount;
    }

    public void setUsingNowCount(Integer usingNowCount) {
        this.usingNowCount = usingNowCount;
    }
}
