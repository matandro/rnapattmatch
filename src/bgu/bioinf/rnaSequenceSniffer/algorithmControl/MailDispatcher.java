package bgu.bioinf.rnaSequenceSniffer.algorithmControl;

import bgu.bioinf.brlab.credentials.EmailCredentials;
import bgu.bioinf.rnaSequenceSniffer.Model.JobInformation;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Map;
import java.util.Properties;

/**
 * Created by matan on 27/12/14.
 */
public class MailDispatcher {
    private static final String FROM_RNAPATTMATCH = EmailCredentials.USER_NAME;
    private static final String SMTP_ADDRESS = EmailCredentials.SMTP_ADDRESS;

    private static final String PASSWORD_RNAPATTMATCH = EmailCredentials.PASSWORD;


    private static boolean sendMail(String bodyText, String subject, String recipient) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", SMTP_ADDRESS);
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_RNAPATTMATCH, PASSWORD_RNAPATTMATCH);
                    }
                });
        boolean success = false;
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_RNAPATTMATCH));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipient)
            );
            message.setSubject(subject);
            message.setText(bodyText);
            Transport.send(message);
            success = true;
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }


    public static boolean submissionMail(JobInformation jobInformation) {
        if (jobInformation.getEmail() == null || "".equals(jobInformation.getEmail())) {
            return false;
        }

        StringBuilder message = new StringBuilder();
        message.append("Hello ");
        message.append(jobInformation.getEmail());
        message.append(",\n");
        message.append("Thank you for using our RNAPattMatch web server application.\n");
        if (jobInformation.getQueryName() != null && !"".equals(jobInformation.getQueryName())) {
            message.append("The search was started for query: ");
            message.append(jobInformation.getQueryName());
            message.append(". Your job id is: ");
        } else {
            message.append("The search was started. Your job id is: ");
        }
        message.append(jobInformation.getJobId());
        message.append("\nYour results will be available in the following link:\n");
        message.append("http://www.cs.bgu.ac.il/rnapattmatch/GetResults.jsp?jid=");
        message.append(jobInformation.getJobId());
        message.append("\nResults will be removed after a week.\n\nThis is an automatic e-mail, " +
                "replays are ignored.\nHave a good day!\n");
        return sendMail(message.toString(), "RNAPattMatch confirmed submission, Query name: "
                + jobInformation.getQueryName(), jobInformation.getEmail());
    }

    public static boolean calculationCompleteMail(JobRunner jobRunner) {
        JobInformation jobInformation = jobRunner.getJobInformation();
        if (jobInformation.getEmail() == null || "".equals(jobInformation.getEmail())) {
            return false;
        }

        StringBuilder message = new StringBuilder();
        message.append("Hello ");
        message.append(jobInformation.getEmail());
        message.append(",\nYour search is done, ");
        switch (jobRunner.getJobState()) {
            case ERROR:
                message.append("an error occurred.\n");
                break;
            case DONE_NO_RESULTS:
                message.append("no results were found.\n");
                break;
            case DONE_RESULTS:
                Map<Integer, Integer> targetResults = jobRunner.getTargetResults();
                message.append(jobRunner.getTotalResults());
                message.append(" results were found for ");
                message.append(targetResults.size());
                message.append(" targets.\nResults per target (order as in file):\n");
                for (Map.Entry<Integer, Integer> entry : targetResults.entrySet()) {
                    message.append("Target ");
                    message.append(entry.getKey());
                    message.append(": ");
                    message.append(entry.getValue());
                    message.append(" results.\n");
                }
                break;
            default:
                message.append("Unknown search replay... try again, if the error persist contact the email " +
                        "noted in the site\n");
                break;
        }
        message.append("\nReminder! Your job id is: ");
        message.append(jobInformation.getJobId());
        message.append("\nYou can view the results in the following link:\n");
        message.append("http://www.cs.bgu.ac.il/rnapattmatch/GetResults.jsp?jid=");
        message.append(jobInformation.getJobId());
        message.append("\nResults will be removed after a week.\n\nThis is an automatic e-mail, " +
                "replays are ignored.\nHave a good day!\n");

        return sendMail(message.toString(), "RNAPattMatch results are ready!, Query name: "
                + jobInformation.getQueryName(), jobInformation.getEmail());
    }
}
