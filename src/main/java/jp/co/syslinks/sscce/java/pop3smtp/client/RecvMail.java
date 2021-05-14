package jp.co.syslinks.sscce.java.pop3smtp.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class RecvMail {

    public static void main(String[] args) {
        System.out.println("start: main");
        try {
            // メール受信のプロパティ設定
            Properties props = new Properties();
            // props.put("mail.pop3.host", "pop.mail.yahoo.co.jp");
            props.put("mail.pop3.host", "127.0.0.1");
            props.put("mail.pop3.port", "110");
            //props.put("mail.mime.encodefilename", "true");
            //props.put("mail.mime.charset", "UTF-8");

            // メール受信フォルダをオープン
            Session session = Session.getDefaultInstance(props);
            try (Store store = session.getStore("pop3");) {
                store.connect("address", "password");
                try (Folder folderInbox = store.getFolder("INBOX");) {
                    folderInbox.open(Folder.READ_ONLY);
                    // メッセージ一覧を取得
                    Message[] arrayMessages = folderInbox.getMessages();
                    for (int lc = 0; lc < arrayMessages.length; lc++) {
                        // メッセージの取得
                        Message message = arrayMessages[lc];
                        // 件名の取得と表示
                        String subject = message.getSubject();
                        System.out.println("件名：" + subject);
                        // 受信日時を表示
                        String sentDate = message.getSentDate().toString();
                        System.out.println("受信日時：" + sentDate);
                        // 本文の取得と表示
                        MimeMultipart mltp = (MimeMultipart) message.getContent();
                        for (int ii = 0, len = mltp.getCount(); ii < len; ii++) {
                            BodyPart body = mltp.getBodyPart(ii);
                            final String contentType = body.getContentType();
                            if (contentType.indexOf("name=") < 0) { // text/plain; charset=UTF-8
                                // メール本体
                                System.out.println("本文：" + body.getContent().toString());
                            } else {
                                // application/octet-stream; name=attach1.zip
                                // text/plain; charset=UTF-8; name=attach2.txt
                                // 添付ファイル
                                String fileName = MimeUtility.decodeText(body.getFileName());
                                System.out.println("添付ファイル名：" + fileName);
                                try (InputStream is = body.getInputStream(); //
                                        OutputStream os = new FileOutputStream(new File("_mail/recv", fileName)); //
                                ) {
                                    int read = -1;
                                    int buffSize = 1024;
                                    byte[] buff = new byte[buffSize];
                                    while ((read = is.read(buff, 0, buffSize)) != -1) {
                                        os.write(buff, 0, read);
                                    }
                                }
                            }
                        }
                        // 取得の最大件数は１０件
                        if (lc >= 10) {
                            break;
                        }
                        System.out.println();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("例外が発生！");
            e.printStackTrace();
        } finally {
        }
        System.out.println("end: main");
    }
}
