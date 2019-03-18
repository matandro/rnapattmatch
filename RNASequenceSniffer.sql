# Drop user if exists
GRANT USAGE ON *.* TO 'rnaSniffer'@'localhost';
DROP USER 'rnaSniffer'@'localhost';
# Drop schema if exists
DROP SCHEMA IF EXISTS RNASequenceSniffer;
# create schema
CREATE SCHEMA RNASequenceSniffer;
# assume existence of user rnaSniffer
# give user permissions
GRANT SELECT, UPDATE, DELETE, INSERT ON RNASequenceSniffer.* TO 'rnaSniffer'@'localhost';
# Start Creating tables
# Create job table
CREATE TABLE RNASequenceSniffer.Job
(
  JobId       VARCHAR(8) NOT NULL,
  QueryName   VARCHAR(128),
  Email VARCHAR(56),
  QuerySequence  VARCHAR(256) NOT NULL,
  QueryStructure VARCHAR(256) NOT NULL,
  TargetFile       VARCHAR(256) NOT NULL,
  StartTime      TIMESTAMP    NOT NULL,
  EndTime        TIMESTAMP    NULL     DEFAULT NULL,
# target File Status: REMOVED, DOWNLOADING, (null for ready)
  TargetFileStatus VARCHAR(10),
  PRIMARY KEY (JobId),
  INDEX Job_QueryName_Index (QueryName),
  INDEX Job_TargetFile_Index (TargetFile)
);
# Create base pair matrix table
CREATE TABLE RNASequenceSniffer.JobBpMatrix
(
  JobId VARCHAR(8) NOT NULL,
  AC    FLOAT,
  AG    FLOAT,
  AU    FLOAT,
  CG    FLOAT,
  CU    FLOAT,
  GU    FLOAT,
  PRIMARY KEY (JobId),
  FOREIGN KEY JobBpMatrix_JobId_FK (JobId)
  REFERENCES RNASequenceSniffer.Job (JobId)
);
# Create error table
CREATE TABLE RNASequenceSniffer.JobError
(
  JobId VARCHAR(8) NOT NULL,
  ErrorStr VARCHAR(1028) NOT NULL,
  PRIMARY KEY (JobId),
  FOREIGN KEY JobError_JobId_FK (JobId)
  REFERENCES RNASequenceSniffer.Job (JobId)
);
# Create targets table
CREATE TABLE RNASequenceSniffer.JobTarget
(
  JobId VARCHAR(8) NOT NULL,
  TargetNo   INT NOT NULL,
  TargetName VARCHAR(256),
  PRIMARY KEY (JobId, TargetNo),
  FOREIGN KEY (JobId) REFERENCES RNASequenceSniffer.Job (JobId)
);
# Create Result table
CREATE TABLE RNASequenceSniffer.JobResult
(
  JobId VARCHAR(8) NOT NULL,
  TargetNo       INT NOT NULL,
  ResultNo       INT NOT NULL,
  StartIndex     INT NOT NULL,
  GapStr         VARCHAR(56),
  EnergyScore    FLOAT,
  MatrixScore    FLOAT,
  ResultSequence VARCHAR(256),
  PRIMARY KEY (JobId, TargetNo, ResultNo),
  FOREIGN KEY JobResult_JobIdTargetNo_FK (JobId, TargetNo)
  REFERENCES RNASequenceSniffer.JobTarget (JobId, TargetNo)
);

# Table with inormation on Cached DS
CREATE TABLE RNASequenceSniffer.CachedJobs (
# identifier is file name
  Identifier VARCHAR(256) NOT NULL,
  LastUse    TIMESTAMP    NOT NULL,
  Status     VARCHAR(10)  NOT NULL,
  Size       BIGINT,
  UsingNowCount INT,
  PRIMARY KEY (Identifier)
);