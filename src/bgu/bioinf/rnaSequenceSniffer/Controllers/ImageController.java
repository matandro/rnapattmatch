package bgu.bioinf.rnaSequenceSniffer.Controllers;

import bgu.bioinf.rnaSequenceSniffer.algorithmControl.ImageProducer;
import bgu.bioinf.rnaSequenceSniffer.webInterface.ResultRetriever;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.*;
import java.net.URLDecoder;
import java.util.Stack;

/**
 * Created by matan on 16/12/14.
 */
public class ImageController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String requestedFile = request.getPathInfo();

        if (requestedFile == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image file name missing (<JobId_TargetNo_ResultNo.png)");
            return;
        }

        String error = null;
        String pathString = URLDecoder.decode(requestedFile, "UTF-8");
        pathString = pathString.substring(pathString.lastIndexOf('/') + 1, pathString.lastIndexOf('.'));
        String[] idString = pathString.split("_");
        String jobId = idString[0];
        Integer targetNo;
        Integer resultNo;
        try {
            targetNo = Integer.valueOf(idString[1]);
            resultNo = Integer.valueOf(idString[2]);
        } catch (Exception e) {
            targetNo = -1;
            resultNo = -1;
        }

        ResultRetriever resultRetriever = new ResultRetriever(jobId, targetNo, resultNo);
        if (resultRetriever.init()) {
            // First check if we got alternative structure, otherwise get one from result
            String structure = request.getParameter("altStruct");
            String topic = resultRetriever.getTopic();
            if (structure == null || "".equals(structure)) {
                structure = resultRetriever.getAlignedStructure();
            } else if ((structure.length() != resultRetriever.getSequence().length()) ||
                    !isLegalStructure(structure)) {
                error = "Alternative structure is illegal";
            } else {
                topic = "Minimal energy structure";
                String dGEnergy = request.getParameter("energy");
                if (dGEnergy != null && !"".equals(dGEnergy)) {
                    topic = "dG = " + dGEnergy + " - " + topic;
                }
            }

            if (error == null) {
                // all ok, Generate image
                ImageProducer ip = new ImageProducer(resultRetriever.getSequence(),
                        structure, topic, resultRetriever.getStartIndex());
                File imageFile = null;
                OutputStream out = null;
                try {
                    response.setContentType("image/jpg");
                    imageFile = new File(ip.getImage());
                    InputStream in = new FileInputStream(imageFile);
                    out = response.getOutputStream();
                    byte[] data = new byte[1024];
                    int read;
                    while ((read = in.read(data)) != -1) {
                        out.write(data, 0, read);
                    }
                    out.flush();
                } catch (Exception e) {
                    response.reset();
                    e.printStackTrace();
                    error = "Failed to retrieve image, Please try again later";
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Exception ignore) {

                        }
                    }
                    if (imageFile != null) {
                        try {
                            imageFile.delete();
                        } catch (Exception ignore) {

                        }
                    }
                }
            }
        } else {
            error = "Failed to load image for job id: " + jobId;
        }

        if (error != null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, error);
        }

        return;
    }

    private static boolean isLegalStructure(String structure) {
        boolean legal = true;
        Stack<Integer> bracket = new Stack<Integer>();

        for (int i = 0; i < structure.length(); ++i) {
            char currentChar = structure.charAt(i);
            if (currentChar == '(') {
                bracket.push(i);
            } else if (currentChar == ')') {
                if (bracket.isEmpty()) {
                    legal = false;
                    break;
                }
                bracket.pop();
            } else if (currentChar != '.') {
                legal = false;
                break;
            }
        }

        if (legal) {
            legal = bracket.isEmpty();
        }

        return legal;
    }
}
