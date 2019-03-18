package bgu.bioinf.rnaSequenceSniffer.Controllers;

import bgu.bioinf.rnaSequenceSniffer.Model.JobInformation;
import bgu.bioinf.rnaSequenceSniffer.webInterface.JobSubmitter;
import bgu.bioinf.rnaSequenceSniffer.webInterface.WebappContextListener;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by matan on 30/12/14.
 */
public class SubmitJobController extends HttpServlet {
    public static final String TEMP_LOCATION = WebappContextListener.BASE_LOCATION + "temp/";
    public static final String FILE_IN_TEMP = "Uploads/";
    public static final String CACHE_IN_TEMP = "Cache/";
    // 100 MB per file, if they have the patients...
    public static final int MAX_FILE_SIZE = 100 * 1024 * 1024;
    public static final int MAX_MEM_SIZE = 150 * 1024 * 1024;

    private static final int MAX_RESULTS_IN_PAGE = 50;
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        JobInformation jobInformation = new JobInformation();
        File file;
        String error = null;

        // Verify the content type
        String contentType = request.getContentType();
        if ((contentType.contains("multipart/form-data"))) {
            try {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                // maximum size that will be stored in memory
                factory.setSizeThreshold(MAX_MEM_SIZE);
                // Location to save data that is larger than maxMemSize.
                factory.setRepository(new File(TEMP_LOCATION));
                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);
                // maximum file size to be uploaded.
                upload.setSizeMax(MAX_FILE_SIZE);
                List fileItems = null;
                // Parse the request to get file items.
                try {
                    fileItems = upload.parseRequest(request);
                } catch (FileUploadBase.SizeLimitExceededException e) {
                    jobInformation.addError("File size too big, server supports up to 100mb.");
                }

                if (fileItems != null) {
                    // Process the uploaded file items
                    Iterator i = fileItems.iterator();

                    while (i.hasNext()) {
                        FileItem fi = (FileItem) i.next();
                        // A normal parameter
                        if (fi.isFormField()) {
                            String value = fi.getString();
                            if ("query_name".equals(fi.getFieldName())) {
                                jobInformation.setQueryName(value);
                            } else if ("email".equals(fi.getFieldName())) {
                                jobInformation.setEmail(value);
                            } else if ("query_sequence".equals(fi.getFieldName())) {
                                jobInformation.setQuerySequence(value);
                            } else if ("query_structure".equals(fi.getFieldName())) {
                                jobInformation.setQueryStructure(value);
                            } else if ("ACCorr".equals(fi.getFieldName())) {
                                jobInformation.setBasePairValue("AC", fi.getString());
                            } else if ("AGCorr".equals(fi.getFieldName())) {
                                jobInformation.setBasePairValue("AG", fi.getString());
                            } else if ("AUCorr".equals(fi.getFieldName())) {
                                jobInformation.setBasePairValue("AU", fi.getString());
                            } else if ("CGCorr".equals(fi.getFieldName())) {
                                jobInformation.setBasePairValue("CG", fi.getString());
                            } else if ("CUCorr".equals(fi.getFieldName())) {
                                jobInformation.setBasePairValue("CU", fi.getString());
                            } else if ("GUCorr".equals(fi.getFieldName())) {
                                jobInformation.setBasePairValue("GU", fi.getString());
                            } else if ("actualFile".equals(fi.getFieldName())) {
                                jobInformation.testTargetType(fi.getString());
                            }
                        }
                    }
                    // once we reviewed all the data we can go over file
                    if (jobInformation.getTargetType() == JobInformation.TargetType.DOWNLOADED) {
                        i = fileItems.iterator();
                        while (i.hasNext()) {
                            FileItem fi = (FileItem) i.next();
                            if (!fi.isFormField()) {
                                jobInformation.uploadFile(fi);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                jobInformation.addError("problem while reading information.");
            }

            if ("".equals(jobInformation.getError()))
                jobInformation.validateQueryBP();

            // Everything ok, call job submitter
            if ("".equals(jobInformation.getError())) {
                JobSubmitter jobSubmitter = new JobSubmitter(jobInformation);
                boolean submitted = jobSubmitter.submit();
                if (!submitted) {
                    error = "Failed to submit job.\n" + jobSubmitter.getError();
                } //submitted, work started
                else {
                    // if we got an email error, tell the use to remember his info
                    boolean emailSent = jobSubmitter.sendEmail();
                    String urlStr = "GetResults.jsp?jid=" + jobInformation.getJobId();
                    if (!emailSent) {
                        urlStr += "&emailErr=1";
                    }
                    String encodedURL = response.encodeRedirectURL(urlStr);
                    response.sendRedirect(encodedURL);
                    return;
                }
            } else {// We found an error while going through the input
                error = jobInformation.getError();
            }
        } else {
            error = "Please use the proper form to submit jobs!";
        }
        request.setAttribute("error", error);
        RequestDispatcher view = request.getRequestDispatcher("SubmissionError.jsp");
        view.forward(request, response);
    }
}
