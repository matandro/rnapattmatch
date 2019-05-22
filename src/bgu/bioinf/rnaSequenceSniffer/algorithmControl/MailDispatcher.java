package bgu.bioinf.rnaSequenceSniffer.algorithmControl;

import bgu.bioinf.rnaSequenceSniffer.Model.JobInformation;
import sun.net.smtp.SmtpClient;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 * Created by matan on 27/12/14.
 */
public class MailDispatcher {
    private static final String FROM_RNAPATTMATCH = "rnapattmatch@cs.bgu.ac.il";
    private static final String SMTP_ADDRESS = "smtp.bgu.ac.il";


    public static boolean submissionMail(JobInformation jobInformation) {
        SmtpClient client = null;
        if (jobInformation.getEmail() == null || "".equals(jobInformation.getEmail())) {
            return false;
        }

        boolean success = false;
        try {
            client = new SmtpClient(SMTP_ADDRESS);
            client.from(FROM_RNAPATTMATCH);
            client.to(jobInformation.getEmail());
            PrintStream message = client.startMessage();
            message.println("From: RNA Pattern Matcher");
            message.println("To: " + jobInformation.getEmail());
            message.println("Subject: Results for query " + jobInformation.getQueryName());
            message.print("Hello ");
            message.println(jobInformation.getEmail() + ",");
            message.println("Thank you for using our RNAPattMatch web server application.");
            message.println("The search was started. Your job id is: " + jobInformation.getJobId());
            message.println("Your results will be available in the following link:");
            message.println();
            message.println("http://www.cs.bgu.ac.il/rnapattmatch/GetResults.jsp?jid=" + jobInformation.getJobId());
            message.println();
            message.println("Results will be removed after a week.");
            message.println("Have a good day!");
            message.println();
            message.flush();
            message.close();
            client.closeServer();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.closeServer();
                } catch (IOException ignore) {
                }
            }
        }
        return success;
    }

    public static boolean calculationCompleteMail(JobRunner jobRunner) {
        SmtpClient client = null;
        JobInformation jobInformation = jobRunner.getJobInformation();
        if (jobInformation.getEmail() == null || "".equals(jobInformation.getEmail())) {
            return false;
        }

        boolean success = false;
        try {
            client = new SmtpClient(SMTP_ADDRESS);
            client.from(FROM_RNAPATTMATCH);
            client.to(jobInformation.getEmail());
            PrintStream message = client.startMessage();
            message.println("From: RNA Pattern Matcher");
            message.println("To: " + jobInformation.getEmail());
            message.println("Subject: Query \"" + jobInformation.getQueryName() + "\" is ready");
            message.print("Hello ");
            message.println(jobInformation.getEmail() + ",");
            message.print("Your search is done, ");

            switch (jobRunner.getJobState()) {
                case ERROR:
                    message.println("an error occurred.");
                    break;
                case DONE_NO_RESULTS:
                    message.println("no results were found.");
                    break;
                case DONE_RESULTS:
                    Map<Integer, Integer> targetResults = jobRunner.getTargetResults();
                    message.println(jobRunner.getTotalResults() + " results were found for " +
                            targetResults.size() + " targets.");
                    message.println("Results per target (order as in file):");
                    for (Map.Entry<Integer, Integer> entry : targetResults.entrySet()) {
                        message.println("Target " + entry.getKey() + ": " + entry.getValue() + " results.");
                    }
                    break;
                default:
                    message.println("Unknown search replay...");
                    break;
            }
            message.println();
            message.println("Reminder! Your job id is: " + jobInformation.getJobId());
            message.println("Additional information on the results is available in the following link:");
            message.println();
            message.println("http://www.cs.bgu.ac.il/rnapattmatch/GetResults.jsp?jid=" + jobInformation.getJobId());
            message.println();
            message.println("Results will be removed after a week.");
            message.println("Have a good day!");
            message.println();
            message.flush();
            message.close();
            client.closeServer();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.closeServer();
                } catch (IOException ignore) {
                }
            }
        }
        return success;
    }
}
