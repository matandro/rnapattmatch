package bgu.bioinf.rnaSequenceSniffer.Controllers;

import bgu.bioinf.rnaSequenceSniffer.webInterface.JobRetriever;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by matan on 28/12/14.
 * <p/>
 * Used to check if job is ready on result page without reloading it unnecessarily
 * Can be improved to return the job state if i add the attribute to the DB
 */
public class CheckJobReadyController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Boolean isReady = false;
        String error = null;
        String jobId = request.getParameter("jobId");

        if (jobId == null) {
            error = "Could not read job id in query.";
        } else {
            JobRetriever jobRetriever = new JobRetriever();
            if (jobRetriever.initJob(jobId)) {
                isReady = jobRetriever.isReady();
            } else if (jobRetriever.isAccessError()) {
                error = "Failed to access database, try refreshing later";
            } else {
                error = "Couldn't not find job " + jobId;
            }
        }

        if (error != null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(error);
        } else {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            JSONObject result = new JSONObject();
            result.put("isReady", isReady);
            result.writeJSONString(response.getWriter());
        }
    }
}