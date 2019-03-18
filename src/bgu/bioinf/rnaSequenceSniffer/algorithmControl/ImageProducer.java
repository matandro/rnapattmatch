package bgu.bioinf.rnaSequenceSniffer.algorithmControl;

import bgu.bioinf.rnaSequenceSniffer.Controllers.SubmitJobController;
import bgu.bioinf.rnaSequenceSniffer.db.JobResultEntity;
import bgu.bioinf.rnaSequenceSniffer.webInterface.JobRetriever;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by matan on 16/12/14.
 */
public class ImageProducer {
    private static final String MFOLD_SIG_GRAPH = JobRunner.ALG_LOCATION + "mfold/bin/sir_graph";
    private JobRetriever jobRetriever;
    private JobResultEntity jobResultEntity;
    private String sequence;
    private String alignedStructure;
    private String topic;
    private int startIndex;

    public ImageProducer(String sequence, String alignedStructure, String topic, int startIndex) {
        this.sequence = sequence;
        this.alignedStructure = alignedStructure;
        this.topic = topic;
        this.startIndex = startIndex;
    }


    public String getImage() {
        String imageName = null;
        File tempCTFile = null;
        String tempFileName = null;
        Process p;
        try {
            tempCTFile = generateCTfile();
            tempFileName = SubmitJobController.TEMP_LOCATION
                    + tempCTFile.getName().substring(0, tempCTFile.getName().lastIndexOf('.'));
            String fullRunningLine = MFOLD_SIG_GRAPH + " -p -o "
                    + tempFileName + " " + tempCTFile.getPath();
            p = Runtime.getRuntime().exec(fullRunningLine);
            System.out.println("Image producer, executing: " + fullRunningLine);
            int exitVal = p.waitFor();
            if (exitVal < 0)
                throw new Exception();
            p = Runtime.getRuntime().exec("convert " + tempFileName + ".ps " + tempFileName + ".jpg");
            exitVal = p.waitFor();
            if (exitVal < 0)
                throw new Exception();
            imageName = tempFileName + ".jpg";
        } catch (Exception ignore) {
            imageName = null;
        } finally {
            if (tempCTFile != null) {
                try {
                    tempCTFile.delete();
                } catch (Exception ignore) {
                }
            }
            if (tempFileName != null) {
                File file = new File(tempFileName + ".ps");
                try {
                    file.delete();
                } catch (Exception ignore) {
                }
            }
        }
        return imageName;
    }

    public File generateCTfile() throws IOException {
        File ctFile = null;
        BufferedWriter bufferedWriter = null;
        try {
            ctFile = File.createTempFile("CT_", ".ct");

            bufferedWriter = new BufferedWriter(new FileWriter(ctFile));
            bufferedWriter.write(sequence.length() + "\t" + topic);
            bufferedWriter.newLine();

            Map<Integer, Integer> complementaryMap = new HashMap<Integer, Integer>();
            Stack<Integer> closing = new Stack<Integer>();
            for (int i = 0; i < alignedStructure.length(); ++i) {
                if (alignedStructure.charAt(i) == '(') {
                    closing.push(i);
                } else if (alignedStructure.charAt(i) == ')') {
                    Integer close = closing.pop();
                    complementaryMap.put(close, i);
                    complementaryMap.put(i, close);
                }
            }

            for (int i = 0; i < alignedStructure.length(); ++i) {
                String line = (i + 1) + "\t" + sequence.charAt(i) + "\t" + i + "\t";
                Integer next = i + 2;
                if (next > alignedStructure.length()) {
                    next = 0;
                }
                line += next + "\t";
                Integer complementary = complementaryMap.get(i);
                if (complementary == null) {
                    complementary = 0;
                } else {
                    complementary += 1;
                }
                line += complementary + "\t" + (startIndex + i);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        } finally {
            bufferedWriter.close();
        }
        return ctFile;
    }

}
