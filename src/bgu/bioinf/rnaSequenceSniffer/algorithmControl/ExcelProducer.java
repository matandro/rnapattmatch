package bgu.bioinf.rnaSequenceSniffer.algorithmControl;

import bgu.bioinf.rnaSequenceSniffer.db.JobBpMatrixEntity;
import bgu.bioinf.rnaSequenceSniffer.db.JobEntity;
import bgu.bioinf.rnaSequenceSniffer.db.JobResultEntity;
import bgu.bioinf.rnaSequenceSniffer.webInterface.JobRetriever;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by matan on 15/12/14.
 */
public class ExcelProducer {
    private Workbook excelWorkbook;
    private String jobId;
    private JobRetriever jobRetriever;

    public ExcelProducer(String jobId) {
        this.jobId = jobId;
    }

    public boolean init() {
        boolean result = false;
        try {
            jobRetriever = new JobRetriever();
            result = jobRetriever.initJob(jobId);
        } catch (Exception ignore) {
        }
        return result;
    }

    private String getShortName() {
        String shortName = jobRetriever.getJobEntity().getQueryName();
        if (shortName == null || "".equals(shortName)) {
            shortName = jobRetriever.getJobEntity().getJobId();
        } else {
            shortName = jobRetriever.getJobEntity().getJobId() + "_" + shortName.trim().replace(' ', '_');
        }
        if (shortName.length() > 15) {
            shortName = shortName.substring(0, 15);
        }
        return shortName;
    }

    public String getOutputFileName() {
        return getShortName() + ".xlsx";
    }

    public String writeData() {
        String excelPath = null;
        FileOutputStream fileOutputStream = null;
        try {
            excelWorkbook = new SXSSFWorkbook(100);
            createGeneralInfoSheet();
            Sheet targetSheet = excelWorkbook.createSheet("Targets");
            Sheet currentSheet;
            int currentTargetRowNo = 1;
            // Initiate writing for target sheet
            Row row = targetSheet.createRow(0);
            row.createCell(0).setCellValue("Target No");
            row.createCell(1).setCellValue("Target Name");
            row.createCell(2).setCellValue("No. of results");
            Map<Integer, List<JobResultEntity>> results =
                    jobRetriever.getAllResults(jobRetriever.getNoOfResults(null), 0, "", null);
            for (List<JobResultEntity> targetResults : results.values()) {
                int targetNo = targetResults.get(0).getTargetNo();
                // Add new target to target sheet
                String targetName = targetResults.get(0).getJobTargetEntity().getTargetName();
                int cellNo = 0;
                row = targetSheet.createRow(currentTargetRowNo++);
                row.createCell(cellNo++).setCellValue(targetNo);
                row.createCell(cellNo++).setCellValue(targetName);
                row.createCell(cellNo++).setCellValue(targetResults.size());
                // Create sheet for new target and initiate first row
                currentSheet = excelWorkbook.createSheet("Target " + targetNo);
                cellNo = 0;
                row = currentSheet.createRow(0);
                row.createCell(cellNo++).setCellValue("Results No");
                row.createCell(cellNo++).setCellValue("Start index");
                row.createCell(cellNo++).setCellValue("Gaps");
                row.createCell(cellNo++).setCellValue("Sequence");
                row.createCell(cellNo++).setCellValue("Structure (inc. gaps)");
                row.createCell(cellNo++).setCellValue("Energy score (dG)");
                row.createCell(cellNo++).setCellValue("Matrix cost");
                // Write results to sheet
                int currentResultRowNo = 1;
                for (JobResultEntity jobResultEntity : targetResults) {
                    cellNo = 0;
                    row = currentSheet.createRow(currentResultRowNo);
                    row.createCell(cellNo++).setCellValue(currentResultRowNo++);
                    row.createCell(cellNo++).setCellValue(jobResultEntity.getStartIndex());
                    row.createCell(cellNo++).setCellValue(jobResultEntity.getGapStr());
                    row.createCell(cellNo++).setCellValue(jobResultEntity.getResultSequence());
                    row.createCell(cellNo++).setCellValue(jobResultEntity.getStructureAlignment(jobRetriever.getJobEntity().getQueryStructure()));
                    row.createCell(cellNo++).setCellValue(jobResultEntity.getEnergyScore());
                    row.createCell(cellNo++).setCellValue(jobResultEntity.getMatrixScore());
                }
            }

            File xls = File.createTempFile(getShortName(), ".xlsx");
            excelPath = xls.getAbsolutePath();
            fileOutputStream = new FileOutputStream(xls);
            excelWorkbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            excelPath = null;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return excelPath;
    }

    private void createGeneralInfoSheet() {
        JobBpMatrixEntity jobBpMatrixEntity = jobRetriever.getJobBpMatrixEntity();
        JobEntity jobEntity = jobRetriever.getJobEntity();
        int cellNo = 0;
        int rowNo = 0;
        CreationHelper createHelper = excelWorkbook.getCreationHelper();
        CellStyle dataCellStyle = excelWorkbook.createCellStyle();
        dataCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("MM/dd/yyyy HH:mm:ss"));
        Cell cell;
        Sheet generalInfo = excelWorkbook.createSheet("Query");
        // Write general job info
        Row row = generalInfo.createRow(rowNo++);
        row.createCell(cellNo++).setCellValue("Job ID");
        row.createCell(cellNo++).setCellValue("Query Name");
        row.createCell(cellNo++).setCellValue("Query Sequence");
        row.createCell(cellNo++).setCellValue("Query Structure");
        row.createCell(cellNo++).setCellValue("Target File");
        row.createCell(cellNo++).setCellValue("Submission time");
        row.createCell(cellNo++).setCellValue("Ready time");
        row = generalInfo.createRow(rowNo++);
        cellNo = 0;
        row.createCell(cellNo++).setCellValue(jobId);
        row.createCell(cellNo++).setCellValue(jobEntity.getQueryName());
        row.createCell(cellNo++).setCellValue(jobEntity.getQuerySequence());
        row.createCell(cellNo++).setCellValue(jobEntity.getQueryStructure());
        row.createCell(cellNo++).setCellValue(jobEntity.getCleanTargetFile());
        cell = row.createCell(cellNo++);
        cell.setCellStyle(dataCellStyle);
        cell.setCellValue(jobEntity.getStartTime());
        cell = row.createCell(cellNo++);
        cell.setCellStyle(dataCellStyle);
        cell.setCellValue(jobEntity.getEndTime());
        rowNo++;
        // Write BP matrix
        row = generalInfo.createRow(rowNo++);
        cellNo = 1;
        row.createCell(cellNo++).setCellValue("A");
        row.createCell(cellNo++).setCellValue("C");
        row.createCell(cellNo++).setCellValue("G");
        row.createCell(cellNo++).setCellValue("U");
        row = generalInfo.createRow(rowNo++);
        cellNo = 0;
        row.createCell(cellNo++).setCellValue("A");
        cellNo++;
        row.createCell(cellNo++).setCellValue(jobBpMatrixEntity.getAc());
        row.createCell(cellNo++).setCellValue(jobBpMatrixEntity.getAg());
        row.createCell(cellNo++).setCellValue(jobBpMatrixEntity.getAu());
        row = generalInfo.createRow(rowNo++);
        cellNo = 0;
        row.createCell(cellNo++).setCellValue("C");
        cellNo++;
        cellNo++;
        row.createCell(cellNo++).setCellValue(jobBpMatrixEntity.getCg());
        row.createCell(cellNo++).setCellValue(jobBpMatrixEntity.getCu());
        row = generalInfo.createRow(rowNo++);
        cellNo = 0;
        row.createCell(cellNo++).setCellValue("G");
        cellNo++;
        cellNo++;
        cellNo++;
        row.createCell(cellNo++).setCellValue(jobBpMatrixEntity.getGu());
        row = generalInfo.createRow(rowNo++);
        cellNo = 0;
        row.createCell(cellNo++).setCellValue("U");
    }
}
