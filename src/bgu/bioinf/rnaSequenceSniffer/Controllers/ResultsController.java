package bgu.bioinf.rnaSequenceSniffer.Controllers;

import bgu.bioinf.rnaSequenceSniffer.Model.ResultsModel;
import bgu.bioinf.rnaSequenceSniffer.db.JobEntity;
import bgu.bioinf.rnaSequenceSniffer.db.JobResultEntity;
import bgu.bioinf.rnaSequenceSniffer.webInterface.JobRetriever;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matan on 10/12/14.
 */
public class ResultsController extends HttpServlet {
    private static final long MAX_RESULTS_IN_PAGE = 50;
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        JobRetriever jobRetriever = new JobRetriever();

        // Setup parameters
        String queryName = request.getParameter("qname");
        String id = request.getParameter("jid");

        boolean emailErr = ("1".equals(request.getParameter("emailErr")));
        Integer page = 0;
        try {
            page = Integer.valueOf(request.getParameter("page"));
        } catch (Exception ignore) {
        }
        String sortBy = request.getParameter("sortBy");
        if (sortBy == null) {
            sortBy = "startIndex_ASC";
        }

        List<Float> filters = new ArrayList<Float>();
        /*Float minGapsUsed = null;
        try {
            minGapsUsed = Integer.valueOf(request.getParameter("minGaps"));
        } catch (Exception ignore) {
            minGapsUsed = null;
        }
        filters.add(minGapsUsed);
        Float maxGapsUsed = null;
        try {
            maxGapsUsed = Integer.valueOf(request.getParameter("maxGaps"));
        } catch (Exception ignore) {
            maxGapsUsed = null;
        }
        filters.add(maxGapsUsed);*/
        Float maxMatrixCost = null;
        try {
            maxMatrixCost = Float.valueOf(request.getParameter("maxMatrix"));
        } catch (Exception ignore) {
            maxMatrixCost = null;
        }
        filters.add(maxMatrixCost);
        Float maxEnergyScore = null;
        try {
            maxEnergyScore = Float.valueOf(request.getParameter("maxEnergy"));
        } catch (Exception ignore) {
            maxEnergyScore = null;
        }
        filters.add(maxEnergyScore);

        if (id != null && !"".equals(id)) {
            ResultsModel resultsModel = null;
            if (jobRetriever.initJob(id)) {
                Map<Integer, List<JobResultEntity>> results = new HashMap<Integer, List<JobResultEntity>>();
                String error = null;
                // total number of results without filter
                long noOfResults;
                if ((noOfResults = jobRetriever.getNoOfResults(filters)) > 0)
                    results = jobRetriever.getAllResults(MAX_RESULTS_IN_PAGE, page, sortBy, filters);
                else
                    error = jobRetriever.getError();

                boolean readyForSecond = false;
                if (jobRetriever.getJobEntity().getTargetFileStatus() == null || "".equals(jobRetriever.getJobEntity().getTargetFileStatus())) {
                    File file = new File(jobRetriever.getJobEntity().getTargetFile());
                    readyForSecond = file.exists();
                }
                resultsModel = new ResultsModel(id, jobRetriever.getJobEntity(), results, error,
                        noOfResults, page, MAX_RESULTS_IN_PAGE, readyForSecond);
            } else if (!jobRetriever.isAccessError()) {
                resultsModel = new ResultsModel(id, null, null, null, 0, 0, MAX_RESULTS_IN_PAGE, false);
            }

            // Setup attribute and forward to view
            request.setAttribute("resultsModel", resultsModel);
            request.setAttribute("emailErr", emailErr);
            request.setAttribute("jid", id);
            request.setAttribute("sortBy", sortBy);
            request.setAttribute("maxMatrix", maxMatrixCost);
            request.setAttribute("maxEnergy", maxEnergyScore);
            RequestDispatcher view = request.getRequestDispatcher("ShowResults.jsp");
            view.forward(request, response);
            return;
        }


        List<JobEntity> jobList = new ArrayList<JobEntity>();
        if (queryName != null && !"".equals(queryName)) {
            jobList = jobRetriever.getAllJobsWithName(queryName);
        }

        request.setAttribute("jobList", jobList);
        request.setAttribute("qname", queryName);
        RequestDispatcher view = request.getRequestDispatcher("SearchQuery.jsp");
        view.forward(request, response);
    }
}
