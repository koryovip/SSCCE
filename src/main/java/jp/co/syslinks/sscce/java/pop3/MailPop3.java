package jp.co.syslinks.sscce.java.pop3;

import java.io.IOException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import jp.secret.YahooMailSecretKeys;

/**
 * https://try2explore.com/questions/jp/10671908
 * https://whatsnewmail.yahoo.co.jp/yahoo/20200824a.html
 * https://www.finddevguides.com/Javamail-api-pop3-servers
 */
public class MailPop3 {

    public static void main(String[] args) throws Exception {
        final String host = "pop.mail.yahoo.co.jp";
        final int port = 995;
        final String user = YahooMailSecretKeys.me.user();
        final String password = YahooMailSecretKeys.me.pass();

        Properties properties = System.getProperties();
        properties.put("mail.pop3.host", "pop.mail.yahoo.co.jp");
        properties.put("mail.pop3.port", "995");
        properties.put("mail.pop3.ssl.enable", "true");

        //properties.setProperty("mail.store.protocol", "pop");
        //properties.setProperty("mail.pop3.ssl.trust", "*");
        //properties.setProperty("mail.store.protocol", "imap");
        Session session = Session.getDefaultInstance(properties);
        session.setDebug(true);
        Store store = session.getStore("pop3");
        store.connect(host, port, user, password);
        Folder inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
        for (int i = messages.length - 3; i < messages.length; i++) {
            System.out.println("Message " + (i + 1));
            System.out.println("From : " + messages[i].getFrom()[0]);
            System.out.println("Subject : " + messages[i].getSubject());
            System.out.println("Sent Date : " + messages[i].getSentDate());
            System.out.println("Body : " + getTextFromMessage(messages[i]));
            System.out.println();
        }
        inbox.close(true);
        store.close();
    }

    static private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    static private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

}
