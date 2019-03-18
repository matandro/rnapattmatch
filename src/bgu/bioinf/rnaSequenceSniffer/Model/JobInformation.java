package bgu.bioinf.rnaSequenceSniffer.Model;

import bgu.bioinf.rnaSequenceSniffer.Controllers.SubmitJobController;
import bgu.bioinf.rnaSequenceSniffer.db.DBConnector;
import bgu.bioinf.rnaSequenceSniffer.db.JobEntity;
import org.apache.commons.fileupload.FileItem;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by matan on 26/11/14.
 */
public class JobInformation {
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String QUERY_SEQUENCE_PATTERN =
            "([ACGURYKMSWBDHVN]+(\\[(0|[1-9][0-9]*)\\])?)*[ACGURYKMSWBDHVN]";
    private static final String QUERY_STRUCTURE_PATTERN =
            "([\\(\\.\\)]+(\\[(0|[1-9][0-9]*)\\])?)*[\\(\\.\\)]";

    public enum TargetType {
        DOWNLOADED,
        CACHED,
        EXAMPLE,
        AN
    }

    private String queryName;
    private String email;
    private String querySequence;
    private String queryStructure;
    private String targetFile;
    private Map<String, Float> basePairMatrix;
    private String localError;
    private String jobId;
    private String cacheId;

    public TargetType getTargetType() {
        return targetType;
    }

    private TargetType targetType;
    private static Map<Character, String> fastaMap = null;

    private static String getFastaMeaning(char fastaChar) {
        if (fastaMap == null) {
            fastaMap = new HashMap<Character, String>();
            fastaMap.put('A', "A");
            fastaMap.put('C', "C");
            fastaMap.put('G', "G");
            fastaMap.put('T', "U");
            fastaMap.put('U', "U");
            fastaMap.put('R', "AG");
            fastaMap.put('Y', "CTU");
            fastaMap.put('K', "GTU");
            fastaMap.put('M', "AC");
            fastaMap.put('S', "CG");
            fastaMap.put('W', "ATU");
            fastaMap.put('B', "CGTU");
            fastaMap.put('D', "AGTU");
            fastaMap.put('H', "ACTU");
            fastaMap.put('V', "ACG");
            fastaMap.put('N', "ACGTU");
        }
        return fastaMap.get(Character.toUpperCase(fastaChar));
    }

    public JobInformation() {
        targetType = TargetType.DOWNLOADED;
        localError = "";
        basePairMatrix = new HashMap<String, Float>();
    }

    public void addError(String error) {
        if ("".equals(localError)) {
            localError = "The following errors appear in the input:";
        }
        localError += "\n" + error;
    }

    public String getError() {
        return localError;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || "".equals(email)) {
            // We allow for empty mail, Optional
            return;
        }

        if (!email.matches(EMAIL_PATTERN))
            addError("invalid e-mail");
        this.email = email;
    }

    public String getQuerySequence() {
        return querySequence;
    }

    public void setQuerySequence(String querySequence) {
        if (querySequence == null || "".equals(querySequence)) {
            addError("query sequence is empty");
            return;
        }
        querySequence = querySequence.toUpperCase();
        querySequence = querySequence.replace('T', 'U');
        if (!querySequence.matches(QUERY_SEQUENCE_PATTERN))
            addError("invalid query sequence");
        this.querySequence = querySequence;
    }

    public String getQueryStructure() {
        return queryStructure;
    }

    public void setQueryStructure(String queryStructure) {
        if (queryStructure == null || "".equals(queryStructure)) {
            addError("query structure is empty");
            return;
        }
        if (!queryStructure.matches(QUERY_STRUCTURE_PATTERN))
            addError("invalid query structure");
        int countBrackets = 0;
        for (int i = 0; i < queryStructure.length(); ++i) {
            char c = queryStructure.charAt(i);
            if (c == '(')
                ++countBrackets;
            else if (c == ')') {
                if (--countBrackets < 0) {
                    break;
                }
            }
        }
        if (countBrackets != 0)
            addError("unbalanced brackets in query structure");
        this.queryStructure = queryStructure;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public void setBasePairValue(String basePair, String value) {
        try {
            Float realValue = Float.valueOf(value);
            if (realValue < 0 && realValue != -1)
                addError("invalid value on correlation " + basePair);
            basePairMatrix.put(basePair, realValue);
        } catch (Exception ignore) {
            addError("value on correlation " + basePair + " is not a number");
        }

    }

    public Float getBasePairValue(String basePair) {
        Float result = basePairMatrix.get(basePair);
        if (result == null)
            result = new Float(-1.0);
        return result;
    }

    @Override
    public String toString() {
        String result = "";
        result += "Query Name: " + queryName + "\n";
        result += "Query Sequence: " + querySequence + "\n";
        result += "Query Structure: " + queryStructure + "\n";
        result += "E-mail: " + email + "\n";
        result += "Query Name: " + queryName + "\n";
        result += "Target file path: " + targetFile + "\n";
        result += "Correlation Matrix [AC,AG,AU,CG,CU,GU]: " + "[" + basePairMatrix.get("AC") + ","
                + basePairMatrix.get("AG") + "," + basePairMatrix.get("AU") + ","
                + basePairMatrix.get("CG") + "," + basePairMatrix.get("CU") + ","
                + basePairMatrix.get("GU") + "]";
        return result;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getValidBasePairs() {
        String validBP = "";

        // cannot iterate because order matters, RNASequenceSniffer assumes order
        if (getBasePairValue("AC") != -1) {
            validBP += "AC,";
        }
        if (getBasePairValue("AG") != -1) {
            validBP += "AG,";
        }
        if (getBasePairValue("AU") != -1) {
            validBP += "AU,";
        }
        if (getBasePairValue("CG") != -1) {
            validBP += "CG,";
        }
        if (getBasePairValue("CU") != -1) {
            validBP += "CU,";
        }
        if (getBasePairValue("GU") != -1) {
            validBP += "GU";
        }

        if (validBP.endsWith(","))
            validBP = validBP.substring(0, validBP.length() - 1);

        return validBP;
    }

    private static final String EXAMPLE_PREFIX = "Example: ";
    private static final String CACHE_PREFIX = "Cache: ";
    private static final String AN_PREFIX = "AN: ";

    public String getCacheId() {
        return cacheId;
    }

    // This only refers to files that should be downloaded, example files will have cached data sets
    public void testTargetType(String fileString) throws IOException {
        String path = SubmitJobController.TEMP_LOCATION + SubmitJobController.FILE_IN_TEMP;
        if (fileString.startsWith(EXAMPLE_PREFIX)) {
            // Assume example files are always in upload folder
            targetType = TargetType.EXAMPLE;
            String fileName = fileString.substring(EXAMPLE_PREFIX.length()).trim();
            setTargetFile(path + fileName);
        } else if (fileString.startsWith(CACHE_PREFIX)) {
            // only marked as cached, need to load file from source but also need to lock file deletion meanwhile
            targetType = TargetType.CACHED;
            cacheId = fileString.substring(CACHE_PREFIX.length()).trim();
        } else if (fileString.startsWith(AN_PREFIX)) {
            // only mark for AN, we will download and lock on job creation
            targetType = TargetType.AN;
            setTargetFile(path + fileString.substring(AN_PREFIX.length()).trim());
        }
    }

    /**
     * Downloads file from user, isn't called under: AN, Cache or Example (Assume checked outside)
     *
     * @param fi The file item with the information
     * @return True - if downloaded file successfully
     */
    public boolean uploadFile(FileItem fi) {
        // if we found an error leave the file, waste of time and space
        if (!"".equals(this.getError()) ||
                "".equals(fi.getName()) || null == fi.getName()) {
            addError("Missing target file.");
            return false;
        }

        String path = SubmitJobController.TEMP_LOCATION + SubmitJobController.FILE_IN_TEMP;
        // Get the uploaded file parameters
        boolean success = false;
        String fileName = fi.getName();
        boolean isInMemory = fi.isInMemory();
        long sizeInBytes = fi.getSize();
        // Write the file
        String newFileName = "";
        if (fileName.lastIndexOf("\\") >= 0) {
            newFileName +=
                    fileName.substring(fileName.lastIndexOf("\\")).replaceAll(" ", "_").replaceAll("-", "_");
        } else {
            newFileName +=
                    fileName.substring(fileName.lastIndexOf("\\") + 1).replaceAll(" ", "_").replaceAll("-", "_");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss-");
        Date date = new Date();
        newFileName = path + dateFormat.format(date) + newFileName;
        File file = new File(newFileName);
        try {
            fi.write(file);
            success = true;
        } catch (Exception e) {
            addError("Failed to upload file.");
        }

        setTargetFile(file.getAbsolutePath());
        return success;
    }


    public boolean validateQueryBP() {
        if (queryStructure == null || "".equals(queryStructure)
                || querySequence == null || "".equals(querySequence)) {
            addError("Query sequence and query structure cannot be empty!");
            return false;
        } else if (querySequence.length() != queryStructure.length()) {
            addError("Query sequence and query structure length must fit!");
            return false;
        }

        Stack<Integer> openBracket = new Stack<Integer>();
        for (int i = 0; i < queryStructure.length(); ++i) {
            char structureChar = queryStructure.charAt(i);
            if (structureChar == '(')
                openBracket.push(i);
            else if (structureChar == ')') {
                Integer openIndex = openBracket.pop();
                char sequenceChar = Character.toUpperCase(querySequence.charAt(openIndex));
                char pairChar = Character.toUpperCase(querySequence.charAt(i));
                String sequenceMeaning = JobInformation.getFastaMeaning(sequenceChar);
                String pairMeaning = JobInformation.getFastaMeaning(pairChar);
                boolean possiblePair = false;
                for (int l = 0; l < sequenceMeaning.length(); ++l) {
                    for (int j = 0; j < pairMeaning.length(); ++j) {
                        char meaningChar = sequenceMeaning.charAt(l);
                        char pairMeaningChar = pairMeaning.charAt(j);
                        String pair = (meaningChar > pairMeaningChar) ? "" + pairMeaningChar + meaningChar : "" + meaningChar + pairMeaningChar;
                        if (getBasePairValue(pair) != -1) {
                            possiblePair = true;
                            break;
                        }
                    }
                    if (possiblePair)
                        break;
                }
                String pair = (sequenceChar > pairChar) ? "" + pairChar + sequenceChar : "" + sequenceChar + pairChar;
                if (!possiblePair) {
                    addError("Base Pair \"" + pair + "\" from query [" +
                            openIndex + ", " + i + "] cannot be matched by Base Pairing Matrix");
                    return false;
                }
            }
        }
        return true;
    }
}
