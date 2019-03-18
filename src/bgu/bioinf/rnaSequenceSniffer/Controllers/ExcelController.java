package bgu.bioinf.rnaSequenceSniffer.Controllers;

import bgu.bioinf.rnaSequenceSniffer.algorithmControl.ExcelProducer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * Created by matan on 16/12/14.
 */
public class ExcelController extends HttpServlet {
    private static final int READ_SIZE = 128000; // 128 kb buffer
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String requestedFile = request.getPathInfo();

        if (requestedFile == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Excel file name missing (<jobId>.xlsx)");
            return;
        }

        String error = null;
        // Setup parameters
        String id;
        try {
            String idString = URLDecoder.decode(requestedFile, "UTF-8");
            idString = idString.substring(idString.lastIndexOf('/') + 1, idString.lastIndexOf('.'));
            id = idString;
        } catch (Exception ignore) {
            id = null;
        }

        ExcelProducer ep = new ExcelProducer(id);
        if (ep.init()) {
            String filePath = ep.writeData();
            if (filePath != null) {
                FileInputStream fileIn = null;
                ServletOutputStream out;
                File file = new File(filePath);
                try {
                    fileIn = new FileInputStream(file);
                    response.reset();
                    response.setHeader("Content-Length", String.valueOf(file.length()));
                    response.setContentType(getServletContext().getMimeType(file.getName()));
                    response.setHeader("Content-Disposition",
                            "attachment; filename=\"" + ep.getOutputFileName() + "\"");
                    out = response.getOutputStream();
                    byte[] outputByte = new byte[READ_SIZE];
                    while (fileIn.read(outputByte, 0, READ_SIZE) != -1) {
                        out.write(outputByte);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.reset();
                } finally {
                    try {
                        // Delete local copy of excel file
                        file.delete();
                    } catch (Exception ignore) {
                    }
                    if (fileIn != null) {
                        try {
                            fileIn.close();
                        } catch (Exception ignore) {

                        }
                    }
                }
            } else {
                error = "failed to send the file, Try again later.";
            }
        } else {
            error = "failed to load job id: " + id;
        }

        if (error != null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not create excel file, " + error);
        }

        return;

    }

}
