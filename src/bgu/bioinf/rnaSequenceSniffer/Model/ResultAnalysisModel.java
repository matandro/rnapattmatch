package bgu.bioinf.rnaSequenceSniffer.Model;

import java.text.DecimalFormat;

/**
 * Created by matan on 08/01/15.
 */
public class ResultAnalysisModel {
    private String jobId;
    private int targetNo;
    private int resultNo;
    private String sequence;
    private String structure;
    private String minEnergyStructure;
    private double energyScore;
    private double minEnergyScore;
    private String shapiroStructure;
    private String minEnergyShapiroStructure;
    private int basePairDistance;
    private int shapiroDistance;
    private int targetIndex;
    private String gaps;

    public int getBasePairDistance() {
        return basePairDistance;
    }

    public void setBasePairDistance(int basePairDistance) {
        this.basePairDistance = basePairDistance;
    }

    public int getShapiroDistance() {
        return shapiroDistance;
    }

    public void setShapiroDistance(int shapiroDistance) {
        this.shapiroDistance = shapiroDistance;
    }

    public String getShapiroStructure() {
        return shapiroStructure;
    }

    public void setShapiroStructure(String shapiroStructure) {
        this.shapiroStructure = shapiroStructure;
    }

    public String getMinEnergyShapiroStructure() {
        return minEnergyShapiroStructure;
    }

    public void setMinEnergyShapiroStructure(String minEnergyShapiroStructure) {
        this.minEnergyShapiroStructure = minEnergyShapiroStructure;
    }

    public String getEnergyScore() {
        return new DecimalFormat("###.#").format(energyScore);
    }

    public void setEnergyScore(double energyScore) {
        this.energyScore = energyScore;
    }

    public String getMinEnergyScore() {
        return new DecimalFormat("###.#").format(minEnergyScore);
    }

    public void setMinEnergyScore(double minEnergyScore) {
        this.minEnergyScore = minEnergyScore;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setTargetNo(int targetNo) {
        this.targetNo = targetNo;
    }

    public void setResultNo(int resultNo) {
        this.resultNo = resultNo;
    }

    public String getMinEnergyStructure() {
        return minEnergyStructure;
    }

    public void setMinEnergyStructure(String minEnergyStructure) {
        this.minEnergyStructure = minEnergyStructure;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public String getFullResultId() {
        return jobId + "_" + targetNo + "_" + resultNo;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setGaps(String gaps) {
        this.gaps = gaps;
    }

    public String getGaps() {
        return gaps;
    }
}
