package org.bultreebank.labpipe.utils;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author javapractices.com
 */
public final class Emailer {
    
    private static final Logger log = Logger.getLogger(Emailer.class.getName()); 
    private Properties OPTIONS;
    
    public Emailer(Properties options) {
        OPTIONS = options;
    }
    
    public static void main(String... aArguments) {
        Emailer emailer = new Emailer(new Properties());
        //the domains of these email addresses should be valid,
        //or the example will fail:
        emailer.sendEmail(
                "savkov@bultreebank.org", "savkov@bultreebank.org",
                "Testing 1-2-3", "blah blah blah");
    }

    /**
     * Send a single email.
     */
    public void sendEmail(
            String aFromEmailAddr, String aToEmailAddr,
            String aSubject, String aBody) {
        //Here, no Authenticator argument is used (it is null).
        //Authenticators are used to prompt the user for user
        //name and password.
        Session session = Session.getDefaultInstance(OPTIONS, null);
        MimeMessage message = new MimeMessage(session);
        try {
            //the "from" address may be set in code, or set in the
            //config file under "mail.from" ; here, the latter style is used
            message.setFrom(new InternetAddress(OPTIONS.get("mail.from").toString()));
            message.addRecipient(
                    Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
            message.setSubject(aSubject);
            message.setText(aBody);
            Transport.send(message);
        } catch (MessagingException ex) {
            log.log(Level.SEVERE, "Cannot send email. ", ex);
        }
    }
    
}
