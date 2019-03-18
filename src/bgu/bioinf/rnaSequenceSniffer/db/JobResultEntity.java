package bgu.bioinf.rnaSequenceSniffer.db;

import javax.persistence.*;
import java.util.List;

/**
 * Created by matan on 27/11/14.
 * Entity for job results
 */
@Entity
@Table(name = "JobResult", schema = "", catalog = "RNASequenceSniffer")
@IdClass(JobResultEntityPK.class)
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "JobResult.GetAllByJobID",
                query = "Select JobResult.* " +
                        "From JobResult " +
                        "Where JobResult.JobId=:jobId " +
                        "Order by JobResult.TargetNo ASC, JobResult.StartIndex ASC, JobResult.GapStr ASC",
                resultClass = JobResultEntity.class
        ),
        @NamedNativeQuery(name = "JobResults.RemoveAllResultsWithId",
                query = "DELETE FROM JobResult " +
                        "WHERE JobId = :jobId")})
public class JobResultEntity {
    private String jobId;
    private int targetNo;
    private int resultNo;
    private int startIndex;
    private String gapStr;
    private Float energyScore;
    private Float matrixScore;
    private String resultSequence;
    private JobTargetEntity jobTargetEntity;
    @Transient
    private String alignedStructure;

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

    @Id
    @Column(name = "ResultNo")
    public int getResultNo() {
        return resultNo;
    }

    public void setResultNo(int resultNo) {
        this.resultNo = resultNo;
    }

    @Basic
    @Column(name = "StartIndex")
    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    @Basic
    @Column(name = "GapStr")
    public String getGapStr() {
        return gapStr;
    }

    public void setGapStr(String gapStr) {
        this.gapStr = gapStr;
    }

    @Basic
    @Column(name = "EnergyScore")
    public Float getEnergyScore() {
        return energyScore;
    }

    public void setEnergyScore(Float energyScore) {
        this.energyScore = energyScore;
    }

    @Basic
    @Column(name = "MatrixScore")
    public Float getMatrixScore() {
        return matrixScore;
    }

    public void setMatrixScore(Float matrixScore) {
        this.matrixScore = matrixScore;
    }

    @Basic
    @Column(name = "ResultSequence")
    public String getResultSequence() {
        return resultSequence;
    }

    public void setResultSequence(String resultSequence) {
        this.resultSequence = resultSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobResultEntity that = (JobResultEntity) o;

        if (jobId != that.jobId) return false;
        if (resultNo != that.resultNo) return false;
        if (startIndex != that.startIndex) return false;
        if (targetNo != that.targetNo) return false;
        if (energyScore != null ? !energyScore.equals(that.energyScore) : that.energyScore != null) return false;
        if (gapStr != null ? !gapStr.equals(that.gapStr) : that.gapStr != null) return false;
        if (matrixScore != null ? !matrixScore.equals(that.matrixScore) : that.matrixScore != null) return false;
        return !(resultSequence != null ? !resultSequence.equals(that.resultSequence) : that.resultSequence != null);

    }

    @Override
    public int hashCode() {
        int result = (jobId != null) ? jobId.hashCode() : 0;
        result = 31 * result + targetNo;
        result = 31 * result + resultNo;
        result = 31 * result + startIndex;
        result = 31 * result + (gapStr != null ? gapStr.hashCode() : 0);
        result = 31 * result + (energyScore != null ? energyScore.hashCode() : 0);
        result = 31 * result + (matrixScore != null ? matrixScore.hashCode() : 0);
        result = 31 * result + (resultSequence != null ? resultSequence.hashCode() : 0);
        return result;
    }

    @Transient
    public String getAlignedStructure() {
        return alignedStructure;
    }

    @Transient
    public void setAlignedStructure(String structure) {
        this.alignedStructure = getStructureAlignment(structure);
    }

    @Transient
    public String getStructureAlignment(String structure) {
        String result = "";
        String[] gapsStr = getGapStr().split(",");
        String[] splitStructure = structure.split("\\[[0-9]+\\]");
        for (int i = 0; i < splitStructure.length; ++i) {
            result += splitStructure[i];
            if (i + 1 < splitStructure.length) {
                try {
                    int gap = Integer.valueOf(gapsStr[i]);
                    for (int j = 0; j < gap; ++j)
                        result += ".";
                } catch (Exception ignore) {
                }
            }
        }
        alignedStructure = result;
        return result;
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumns(
            {
                    @JoinColumn(name = "JobId", referencedColumnName = "JobId", insertable = false, updatable = false),
                    @JoinColumn(name = "TargetNo", referencedColumnName = "TargetNo", insertable = false, updatable = false)
            })
    public JobTargetEntity getJobTargetEntity() {
        return jobTargetEntity;
    }

    public void setJobTargetEntity(JobTargetEntity jobTargetEntity) {
        this.jobTargetEntity = jobTargetEntity;
    }

    @Transient
    public String getGapsPrintable() {
        return "[" + getGapStr() + "]";
    }
}
