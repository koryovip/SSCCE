package jp.co.syslinks.sscce.java.pop3smtp;

public class SmtpServerStarter {

    public static void main(String[] args) {
        new SmtpServer().start(25);
    }

}
