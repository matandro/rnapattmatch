package bgu.bioinf.rnaSequenceSniffer.algorithmControl;

import bgu.bioinf.rnaSequenceSniffer.Model.ResultAnalysisModel;

import java.io.*;

/**
 * Created by matan on 09/01/15.
 */
public class MinEnergyProducer {
    private static final String RNA_MIN_SHAPIRO_LOCATION = JobRunner.ALG_LOCATION + "RNAinv/RNAfbinv/RNAshapiroSeq";

    public static boolean generateMinEnergyData(ResultAnalysisModel resultAnalysisModel) {
        boolean success = false;

        Process p = null;
        OutputStream stdin = null;
        InputStream stdout = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            String runningLine = RNA_MIN_SHAPIRO_LOCATION + " " + resultAnalysisModel.getStructure();
            p = Runtime.getRuntime().exec(runningLine);
            stdin = p.getOutputStream();
            stdout = p.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stdout));
            writer = new BufferedWriter(new OutputStreamWriter(stdin));

            writer.write(resultAnalysisModel.getSequence() + "\n");
            writer.flush();
            String line;
            while ((line = reader.readLine()) != null) {
                int answerIndex = line.lastIndexOf("=");
                if (answerIndex > 0) {
                    // push over the "= "
                    answerIndex += 2;
                } else {
                    continue;
                }

                if (line.startsWith("shapiro clean")) {
                    resultAnalysisModel.setMinEnergyShapiroStructure(line.substring(answerIndex));
                } else if (line.startsWith("other shapiro clean")) {
                    resultAnalysisModel.setShapiroStructure(line.substring(answerIndex));
                } else if (line.startsWith("Fold")) {
                    resultAnalysisModel.setMinEnergyStructure(line.substring(answerIndex));
                } else if (line.startsWith("energy score")) {
                    resultAnalysisModel.setMinEnergyScore(Double.valueOf(line.substring(answerIndex)));
                } else if (line.startsWith("shapiro distance")) {
                    resultAnalysisModel.setShapiroDistance(Integer.valueOf(line.substring(answerIndex)));
                } else if (line.startsWith("bp distance")) {
                    resultAnalysisModel.setBasePairDistance(Integer.valueOf(line.substring(answerIndex)));
                }
            }
            p.waitFor();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignore) {
                }
            }
            if (stdout != null) {
                try {
                    stdout.close();
                } catch (IOException ignore) {
                }
            }
            if (stdin != null) {
                try {
                    stdin.close();
                } catch (IOException ignore) {
                }
            }
        }

        return success;
    }
}
