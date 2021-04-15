package jp.co.syslinks.sscce.java.pop3smtp;

public class Pop3ServerStarter {

    public static void main(String[] args) {
        new Pop3Server().start(110);
    }

}
