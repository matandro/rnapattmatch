package bgu.bioinf.rnaSequenceSniffer.Controllers;

import bgu.bioinf.rnaSequenceSniffer.Model.ResultAnalysisModel;
import bgu.bioinf.rnaSequenceSniffer.algorithmControl.MinEnergyProducer;
import bgu.bioinf.rnaSequenceSniffer.webInterface.ResultRetriever;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * Created by matan on 08/01/15.
 */
public class ResultAnalysisController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String requestedFile = request.getPathInfo();
        String error = null;
        String pathString = URLDecoder.decode(requestedFile, "UTF-8");
        pathString = pathString.substring(pathString.lastIndexOf('/') + 1, pathString.lastIndexOf(".jsp"));
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

        ResultAnalysisModel resultAnalysisModel = new ResultAnalysisModel();
        resultAnalysisModel.setJobId(jobId);
        resultAnalysisModel.setTargetNo(targetNo);
        resultAnalysisModel.setResultNo(resultNo);

        ResultRetriever resultRetriever = new ResultRetriever(jobId, targetNo, resultNo);
        if (resultRetriever.init()) {
            resultAnalysisModel.setSequence(resultRetriever.getSequence());
            resultAnalysisModel.setEnergyScore(resultRetriever.getEnergyScore());
            resultAnalysisModel.setStructure(resultRetriever.getAlignedStructure());
            resultAnalysisModel.setTargetIndex(resultRetriever.getStartIndex());
            resultAnalysisModel.setGaps(resultRetriever.getGapString());
            if (MinEnergyProducer.generateMinEnergyData(resultAnalysisModel)) {
                request.setAttribute("resultAnalysisModel", resultAnalysisModel);
                RequestDispatcher view = request.getRequestDispatcher("/ResultAnalysis.jsp");
                view.forward(request, response);
            } else {
                error = "Failed to generate minimum energy information, please try again later.";
            }
        } else {
            error = "Could not initialize data for job id / target No / Results No combination.";
        }

        if (error != null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, error);
        }

        return;
    }
}
