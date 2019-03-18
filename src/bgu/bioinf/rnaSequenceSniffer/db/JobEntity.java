package bgu.bioinf.rnaSequenceSniffer.db;

import javax.persistence.*;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created by matan on 27/11/14.
 */
@Entity
@Table(name = "Job", schema = "", catalog = "RNASequenceSniffer")
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "Job.GetByNameStartTime",
                query = "Select Job.* " +
                        "From Job " +
                        "Where Job.QueryName=? And Job.StartTime=?",
                resultClass = JobEntity.class
        ),
        @NamedNativeQuery(
                name = "Job.GetAllByQname",
                query = "Select Job.* " +
                        "From Job " +
                        "Where Job.QueryName LIKE :queryNamePatt " +
                        "Order By Job.StartTime DESC",
                resultClass = JobEntity.class
        ),
        @NamedNativeQuery(
                name = "Job.GetJobByFileName",
                query = "Select Job.* " +
                        "From Job " +
                        "Where Job.TargetFile = :targetFile " +
                        "Order By Job.StartTime DESC",
                resultClass = JobEntity.class
        ),
        @NamedNativeQuery(name = "Job.GetWeekOldJobs",
                query = "SELECT Job.* " +
                        "FROM Job " +
                        "WHERE EndTime < NOW() - INTERVAL 1 WEEK",
                resultClass = JobEntity.class)})
public class JobEntity {
    private String jobId;
    private String queryName;
    private String email;
    private String querySequence;
    private String queryStructure;
    private String targetFile;
    private Timestamp startTime;
    private Timestamp endTime;
    private String targetFileStatus;

    @Id
    @Column(name = "JobId")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Basic
    @Column(name = "QueryName")
    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    @Basic
    @Column(name = "Email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "QuerySequence")
    public String getQuerySequence() {
        return querySequence;
    }

    public void setQuerySequence(String querySequence) {
        this.querySequence = querySequence;
    }

    @Basic
    @Column(name = "QueryStructure")
    public String getQueryStructure() {
        return queryStructure;
    }

    public void setQueryStructure(String queryStructure) {
        this.queryStructure = queryStructure;
    }

    @Basic
    @Column(name = "TargetFile")
    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    @Basic
    @Column(name = "StartTime")
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "EndTime")
    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobEntity jobEntity = (JobEntity) o;

        if (jobId != jobEntity.jobId) return false;
        if (email != null ? !email.equals(jobEntity.email) : jobEntity.email != null) return false;
        if (endTime != null ? !endTime.equals(jobEntity.endTime) : jobEntity.endTime != null) return false;
        if (queryName != null ? !queryName.equals(jobEntity.queryName) : jobEntity.queryName != null) return false;
        if (querySequence != null ? !querySequence.equals(jobEntity.querySequence) : jobEntity.querySequence != null)
            return false;
        if (queryStructure != null ? !queryStructure.equals(jobEntity.queryStructure) : jobEntity.queryStructure != null)
            return false;
        if (startTime != null ? !startTime.equals(jobEntity.startTime) : jobEntity.startTime != null) return false;
        if (targetFile != null ? !targetFile.equals(jobEntity.targetFile) : jobEntity.targetFile != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (jobId != null) ? jobId.hashCode() : 0;
        result = 31 * result + (queryName != null ? queryName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (querySequence != null ? querySequence.hashCode() : 0);
        result = 31 * result + (queryStructure != null ? queryStructure.hashCode() : 0);
        result = 31 * result + (targetFile != null ? targetFile.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        return result;
    }

    @Transient
    public String getCleanTargetFile() {
        String cleanTargetFile = getTargetFile();

        try {
            int position = cleanTargetFile.lastIndexOf(File.separatorChar);
            cleanTargetFile = cleanTargetFile.substring(position + 1);
            position = cleanTargetFile.indexOf('-');
            position = cleanTargetFile.indexOf('-', position + 1);
            cleanTargetFile = cleanTargetFile.substring(position + 1);
        } catch (Exception ignore) {
        }

        return cleanTargetFile;
    }

    @Transient
    public String getFormattedStartTime() {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(getStartTime());
    }

    @Transient
    public String getFormattedEndTime() {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(getEndTime());
    }

    @Basic
    @Column(name = "TargetFileStatus")
    public String getTargetFileStatus() {
        return targetFileStatus;
    }

    public void setTargetFileStatus(String targetFileStatus) {
        this.targetFileStatus = targetFileStatus;
    }
}
