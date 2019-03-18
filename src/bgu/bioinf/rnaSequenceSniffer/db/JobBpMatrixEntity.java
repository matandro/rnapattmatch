package bgu.bioinf.rnaSequenceSniffer.db;

import javax.persistence.*;

/**
 * Created by matan on 27/11/14.
 */
@Entity
@Table(name = "JobBpMatrix", schema = "", catalog = "RNASequenceSniffer")
@NamedNativeQueries({
        @NamedNativeQuery(name = "JobBpMatrix.RemoveByJobId",
                query = "DELETE FROM JobBpMatrixEntity " +
                        "WHERE JobId = :jobId")})
public class JobBpMatrixEntity {
    private String jobId;
    private Float ac;
    private Float ag;
    private Float au;
    private Float cg;
    private Float cu;
    private Float gu;

    @Id
    @Column(name = "JobId")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Basic
    @Column(name = "AC")
    public Float getAc() {
        return ac;
    }

    public void setAc(Float ac) {
        this.ac = ac;
    }

    @Basic
    @Column(name = "AG")
    public Float getAg() {
        return ag;
    }

    public void setAg(Float ag) {
        this.ag = ag;
    }

    @Basic
    @Column(name = "AU")
    public Float getAu() {
        return au;
    }

    public void setAu(Float au) {
        this.au = au;
    }

    @Basic
    @Column(name = "CG")
    public Float getCg() {
        return cg;
    }

    public void setCg(Float cg) {
        this.cg = cg;
    }

    @Basic
    @Column(name = "CU")
    public Float getCu() {
        return cu;
    }

    public void setCu(Float cu) {
        this.cu = cu;
    }

    @Basic
    @Column(name = "GU")
    public Float getGu() {
        return gu;
    }

    public void setGu(Float gu) {
        this.gu = gu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobBpMatrixEntity that = (JobBpMatrixEntity) o;

        if (jobId != null ? jobId.equals(that.jobId) : that.jobId != null) return false;
        if (ac != null ? !ac.equals(that.ac) : that.ac != null) return false;
        if (ag != null ? !ag.equals(that.ag) : that.ag != null) return false;
        if (au != null ? !au.equals(that.au) : that.au != null) return false;
        if (cg != null ? !cg.equals(that.cg) : that.cg != null) return false;
        if (cu != null ? !cu.equals(that.cu) : that.cu != null) return false;
        if (gu != null ? !gu.equals(that.gu) : that.gu != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (jobId != null) ? jobId.hashCode() : 0;
        result = 31 * result + (ac != null ? ac.hashCode() : 0);
        result = 31 * result + (ag != null ? ag.hashCode() : 0);
        result = 31 * result + (au != null ? au.hashCode() : 0);
        result = 31 * result + (cg != null ? cg.hashCode() : 0);
        result = 31 * result + (cu != null ? cu.hashCode() : 0);
        result = 31 * result + (gu != null ? gu.hashCode() : 0);
        return result;
    }
}
