package bgu.bioinf.rnaSequenceSniffer.algorithmControl;

import bgu.bioinf.rnaSequenceSniffer.Controllers.SubmitJobController;
import bgu.bioinf.rnaSequenceSniffer.Model.JobInformation;
import bgu.bioinf.rnaSequenceSniffer.db.*;
import bgu.bioinf.rnaSequenceSniffer.webInterface.WebappContextListener;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by matan on 27/11/14.
 * 1) Running the algorithm
 * 2) Reading results
 * 3) calculation matrix score an energy score
 * 4) Writes the results back to the DB
 */
public class JobRunner implements Runnable {
    public static final String ALG_LOCATION = WebappContextListener.BASE_LOCATION + "algorithm/"; // "/home/matan/Dropbox/Thesis/Workspace/"; // "/home/matan/Dropbox/Thesis/Workspace/"; // "/home/alex/";
    private static final String SEARCH_LOCATION = ALG_LOCATION + "RNASequenceSniffer/bin/RNAPattMatch";
    private static final String FOLD_COST_LOCATION = ALG_LOCATION + "RNAinv/RNAfbinv/RNAcost";
    private static final String RESULT_MARKER = "Results: ";
    private static final String COST_MARKER = "RNAcost:";
    private static final int MAX_RESULTS_PER_TARGET = 1000;
    public static final String CACHEJOBS_STATUS_WORKING = "WORKING";
    public static final String CACHEJOBS_STATUS_READY = "READY";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy ss:mm:HH");

    private Map<Integer, Integer> targetResults;
    private JobState jobState;
    private JobInformation jobInformation;
    private int totalResults;

    public JobRunner(JobInformation jobInformation) {
        this.jobInformation = jobInformation;
        targetResults = new TreeMap<Integer, Integer>();
        jobState = JobState.INIT;
    }

    public Map<Integer, Integer> getTargetResults() {
        return targetResults;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public JobState getJobState() {
        return jobState;
    }

    private String createRunningLine(String outputFileName, boolean isCached) {
        String runningLine = "";
        // Add complementary matrix information
        String validBasePairs = jobInformation.getValidBasePairs();
        if (validBasePairs != null && !"".equals(validBasePairs))
            runningLine += "-m " + validBasePairs;

        runningLine += " -f " + outputFileName;

        if (isCached) {
            File file = new File(jobInformation.getTargetFile());
            runningLine += " -l " + SubmitJobController.TEMP_LOCATION + SubmitJobController.CACHE_IN_TEMP + file.getName();
        }

        runningLine += " " + jobInformation.getQuerySequence();
        runningLine += " '" + jobInformation.getQueryStructure() + "'";
        runningLine += " " + jobInformation.getTargetFile();
        return runningLine;
    }

    /**
     * calculates an approximation of the cache size
     *
     * @param fileSize the size of the actual target file
     * @return an approximation of the size of all the cache files generated
     */
    private static long getCacheMulti(long fileSize) {
        long result = fileSize * 4 * 2; // size of afkl file
        result += fileSize * 4 * 5 * 2;// size of 2 sa files
        return result;
    }

    /**
     * Check if cache exists, if not, starts a caching job.
     *
     * @param em ready entity manager
     * @return true if cache is ready for use
     */
    public boolean checkCache(EntityManager em, EntityTransaction et) {
        System.out.println(simpleDateFormat.format(new Date()) + " INFO Checking Cache " + jobInformation.getJobId());
        boolean isCache = false;
        CachedJobsEntity cache = null;

        synchronized (WebappContextListener.cacheLock) {
            cache = em.find(CachedJobsEntity.class, jobInformation.getTargetFile());
            if (cache != null) {
                if (CACHEJOBS_STATUS_READY.equals(cache.getStatus())) {
                    isCache = true;
                    cache.setUsingNowCount(cache.getUsingNowCount() + 1);
                }
            }
            if (cache == null) {
                cache = new CachedJobsEntity();
                cache.setIdentifier(jobInformation.getTargetFile());
                cache.setStatus(CACHEJOBS_STATUS_WORKING);
                cache.setSize(getCacheMulti(new File(jobInformation.getTargetFile()).length()));
                cache.setUsingNowCount(0);
                WebappContextListener.cacheExecutor.submit(new DSBuilder(jobInformation));
            }
            cache.setLastUse(new Timestamp(new Date().getTime()));
            if (!et.isActive())
                et.begin();
            em.persist(cache);
            et.commit();
        }

        return isCache;
    }

    private void cleanUsingCache(EntityManager em, EntityTransaction et) {
        CachedJobsEntity cache = null;
        synchronized (WebappContextListener.cacheLock) {
            try {
                cache = em.find(CachedJobsEntity.class, jobInformation.getTargetFile());
                if (cache != null) {
                    if (et == null) {
                        et = em.getTransaction();
                    }
                    if (!et.isActive()) {
                        et.begin();
                    }
                    cache.setUsingNowCount(cache.getUsingNowCount() - 1);
                    em.persist(cache);
                    et.commit();
                } else {
                    throw new Exception("Failed to find cache");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("CleanUsingCache, search ID: " + jobInformation.getJobId() +
                        " failed to drop counter for file " + jobInformation.getTargetFile());
            }
        }
    }

    @Override
    public void run() {
        System.out.println(simpleDateFormat.format(new Date()) + "INFO Starting Job: " + jobInformation.getJobId());
        EntityManager em = null;
        EntityTransaction et = null;
        File tempFile = null;
        String fullRunningLine;
        boolean readResults = false;
        boolean isCached = false;
        try {
            em = DBConnector.getEntityManager();
            et = em.getTransaction();
            if (!et.isActive())
                et.begin();
            JobEntity jobsEntity = em.find(JobEntity.class, jobInformation.getJobId());
            tempFile = File.createTempFile("SearchOutput", "");
            isCached = checkCache(em, et);
            if (!et.isActive())
                et.begin();
            fullRunningLine = SEARCH_LOCATION + " "
                    + createRunningLine(tempFile.getPath(), isCached);
            System.out.println(simpleDateFormat.format(new Date()) + "Running search: " + fullRunningLine);
            Process p = Runtime.getRuntime().exec(fullRunningLine);
            jobState = JobState.RUNNING;
            // Prepare to get error
            InputStream errStream = p.getErrorStream();
            int exitVal = p.waitFor();
            // if ended with error
            if (exitVal != 0) {
                JobErrorEntity errorsEntity = new JobErrorEntity();
                String errorStr = "";
                int read;
                while ((read = errStream.read()) != -1) {
                    errorStr += (char) read;
                }
                errorsEntity.setErrorStr(errorStr);
                errorsEntity.setJobId(jobInformation.getJobId());
                em.persist(errorsEntity);
                jobState = JobState.ERROR;
            } // otherwise
            else {
                readResults = true;
                readResults(em, et, tempFile);
                // fix the case that we had to commit a smaller batch
                if (!et.isActive())
                    et.begin();
            }
            Date date = new Date();
            jobsEntity.setEndTime(new Timestamp(date.getTime()));
            em.persist(jobsEntity);
            et.commit();
            if (totalResults > 0) {
                jobState = JobState.DONE_RESULTS;
            } else {
                jobState = JobState.DONE_NO_RESULTS;
            }
        } catch (Exception e) {
            if (et != null && et.isActive()) {
                try {
                    et.rollback();
                } catch (Exception ignore) {
                }
                try {
                    if (!et.isActive())
                        et.begin();
                    JobErrorEntity errorsEntity = new JobErrorEntity();
                    String errorStr = "";
                    if (readResults) {
                        errorStr = "Failed to commit results";
                    } else {
                        errorStr = "Server had a problem running the algorithm";
                    }
                    errorStr += " ,please try again later.\nIf the problem consists contact the mail on the bottom.";
                    errorsEntity.setErrorStr(errorStr);
                    errorsEntity.setJobId(jobInformation.getJobId());
                    em.persist(errorsEntity);
                    JobEntity jobsEntity = em.find(JobEntity.class, jobInformation.getJobId());
                    Date date = new Date();
                    jobsEntity.setEndTime(new Timestamp(date.getTime()));
                    em.persist(jobsEntity);
                    et.commit();
                } catch (Exception ignore) {
                    // failed to write error to DB, this job is F***ED
                    System.err.println(simpleDateFormat.format(new Date()) + "ERROR FAILED TO CLOSE ERROR JOB" + jobInformation.getJobId());
                }
                jobState = JobState.ERROR;
            }
            jobState = JobState.ERROR;
        } finally {
            if (isCached) {
                try {
                    cleanUsingCache(em, et);
                } catch (Exception ignore) {
                }
            }
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception ignore) {
                }
            }
        }

        MailDispatcher.calculationCompleteMail(this);
    }

    //format <index>;<gaps>;<sequence>
    private int readResults(EntityManager em, EntityTransaction et, File logFile) {
        // set for patching or repeating results error
        totalResults = 0;
        Set<String> patchRemoveRepeat = new HashSet<String>();
        int targetLimit = MAX_RESULTS_PER_TARGET;
        int totalCommited = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(logFile));
            String line;
            int currentTarget = 0;
            int count = 0;
            int amount = 0;
            boolean inResults = false;
            //EntityTransaction et = em.getTransaction();
            //et.begin();
            while ((line = br.readLine()) != null) {
                if (line.contains(RESULT_MARKER)) {
                    addNewTarget(em, line.substring(line.indexOf(RESULT_MARKER) + RESULT_MARKER.length()), currentTarget++);
                    targetLimit = MAX_RESULTS_PER_TARGET;
                    inResults = true;
                    count = 0;
                    patchRemoveRepeat.clear();
                    targetResults.put(currentTarget, 0);
                } else if (line.startsWith(">")) {
                    inResults = false;
                } else if (inResults) {
                    String[] data = line.split(";");
                    if (data.length < 3 || targetLimit == 0)
                        continue;
                    int index = Integer.valueOf(data[0]);
                    String patchKey = index + "@" + data[1];
                    if (patchRemoveRepeat.contains(patchKey)) {
                        continue;
                    }
                    patchRemoveRepeat.add(patchKey);
                    JobResultEntity jobResultEntity = new JobResultEntity();
                    jobResultEntity.setTargetNo(currentTarget - 1);
                    jobResultEntity.setJobId(jobInformation.getJobId());
                    jobResultEntity.setStartIndex(index);
                    jobResultEntity.setGapStr(data[1]);
                    jobResultEntity.setResultSequence(data[2]);
                    jobResultEntity.setResultNo(count++);
                    Float score = getStructureCost(jobResultEntity);
                    jobResultEntity.setEnergyScore(score);
                    score = getMatrixScore(jobResultEntity);
                    jobResultEntity.setMatrixScore(score);
                    em.persist(jobResultEntity);
                    Integer targetCount = targetResults.get(currentTarget);
                    targetResults.put(currentTarget, targetCount + 1);
                    amount++;
                    totalResults++;
                    if (amount >= 1000) {
                        if (!et.isActive()) {
                            et.begin();
                        }
                        et.commit();
                        totalCommited += amount;
                        amount = 0;
                    }
                    --targetLimit;
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return totalCommited;
    }

    private Float getMatrixScore(JobResultEntity jobResultEntity) {
        Float score = 0F;
        Stack<Integer> openBracket = new Stack<Integer>();
        String alignedStructure = jobResultEntity.getStructureAlignment(jobInformation.getQueryStructure());
        String resultSequence = jobResultEntity.getResultSequence();
        for (int i = 0; i < alignedStructure.length(); ++i) {
            if (alignedStructure.charAt(i) == '(') {
                openBracket.push(i);
            } else if (alignedStructure.charAt(i) == ')') {
                Integer openBracketIndex = openBracket.pop();
                String basePair = resultSequence.charAt(openBracketIndex) + "";
                if (resultSequence.charAt(openBracketIndex) < resultSequence.charAt(i)) {
                    basePair += resultSequence.charAt(i);
                } else {
                    basePair = resultSequence.charAt(i) + basePair;
                }
                Float scoreMod = jobInformation.getBasePairValue(basePair);
                if (scoreMod == -1) {
                    System.out.println("ERROR: algorithm calculated illegal base pair!!! " + "JobId="
                            + jobInformation.getJobId() + ", BasePair=" + basePair);
                    score = null;
                    break;
                }
                score += scoreMod;
            }
        }

        return score;
    }

    private void addNewTarget(EntityManager em, String name, int number) {
        JobTargetEntity jobTargetEntity = new JobTargetEntity();
        jobTargetEntity.setJobId(jobInformation.getJobId());
        jobTargetEntity.setTargetNo(number);
        jobTargetEntity.setTargetName(name);
        em.persist(jobTargetEntity);
    }

    /**
     * Run a calculation to check energy minimization on the found sequence / structure
     *
     * @param jobResultEntity the result to test
     * @return the energy cost for the sequence to fold to that structure
     */
    private Float getStructureCost(JobResultEntity jobResultEntity) {
        Float result = null;
        File temp = null;
        BufferedReader br = null;
        try {
            temp = File.createTempFile("FOLD_", "_COST");
            String runningLine = FOLD_COST_LOCATION + " " + temp.getAbsolutePath() + " " +
                    jobResultEntity.getResultSequence() + " " +
                    jobResultEntity.getStructureAlignment(jobInformation.getQueryStructure()) + "";
            Process p = Runtime.getRuntime().exec(runningLine);
            p.waitFor();
            br = new BufferedReader(new FileReader(temp));
            String line;
            while ((line = br.readLine()) != null) {
                int location = line.indexOf(COST_MARKER);
                if (location >= 0) {
                    result = Float.valueOf(line.substring(location + COST_MARKER.length()));
                    break;
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        } finally {
            if (temp != null) {
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception ignore) {
                    }
                }
                try {
                    temp.delete();
                } catch (Exception ignore) {
                }
            }
        }
        return result;
    }

    public JobInformation getJobInformation() {
        return jobInformation;
    }

    public enum JobState {
        INIT,
        RUNNING,
        ERROR,
        DONE_NO_RESULTS,
        DONE_RESULTS
    }
}
