package jp.co.syslinks.sscce.java.pop3smtp.client;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class SendMail {

    public static void main(String[] args) {
        System.out.println("start: main");
        try {
            // メール送信のプロパティ設定
            Properties props = new Properties();
            //props.put("mail.smtp.host", "smtp.mail.yahoo.co.jp");
            //props.put("mail.smtp.port", "587");
            props.put("mail.smtp.host", "127.0.0.1");
            props.put("mail.smtp.port", "25");
            props.put("mail.smtp.auth", "true");
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.ssl.trust", "*");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");

            // セッションを作成する
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("fromAddress", "password");
                }
            });

            // メールの送信先はYahooメール。送信元もYahooメール
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("fromAddress", "fromAddress"));
            message.setReplyTo(new Address[] { new InternetAddress("toAddress") });
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("toAddress"));
            message.setSubject("テスト");
            message.setSentDate(new Date());

            // ７：メールに、本文・添付１・添付２の３つを添付
            Multipart multipart = new MimeMultipart();
            // メッセージ本文
            {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText("テストメール。");
                multipart.addBodyPart(messageBodyPart);
            }
            {
                // １：添付ファイル１を添付するボディパートを取得
                MimeBodyPart attachedFilePart1 = new MimeBodyPart();
                // ２：添付ファイル１のデータソースを取得
                FileDataSource fs1 = new FileDataSource("_mail/attach/attach1.zip");
                // ３：ボディパート１に添付ファイル１を添付
                attachedFilePart1.setDataHandler(new DataHandler(fs1));
                attachedFilePart1.setFileName(MimeUtility.encodeWord(fs1.getName()));
                multipart.addBodyPart(attachedFilePart1);
            }
            {
                // ４：添付ファイル２を添付するボディパートを取得
                MimeBodyPart attachedFilePart2 = new MimeBodyPart();
                // ５：添付ファイル２のデータソースを取得
                FileDataSource fs2 = new FileDataSource("_mail/attach/添付２.txt");
                // ６：ボディパート２に添付ファイル２を添付
                attachedFilePart2.setDataHandler(new DataHandler(fs2));
                attachedFilePart2.setFileName(MimeUtility.encodeWord(fs2.getName()));
                multipart.addBodyPart(attachedFilePart2);
            }
            message.setHeader("Content-Transfer-Encoding", "base64");

            // ８：メールを送信する
            message.setContent(multipart);
            Transport.send(message);
        } catch (Exception e) {
            System.out.println("例外が発生！");
            e.printStackTrace();
        } finally {
        }
        System.out.println("end: main");
    }

}
