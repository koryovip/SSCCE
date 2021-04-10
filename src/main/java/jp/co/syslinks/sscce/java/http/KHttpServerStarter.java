package jp.co.syslinks.sscce.java.http;

import java.io.IOException;
import java.util.Scanner;

public class KHttpServerStarter {

    public static void main(String[] args) throws IOException {
        try (KHttpServer server = new KHttpServer()) {
            server.start(8888);

            // press any key to exit.
            try (Scanner scanner = new Scanner(System.in)) {
                scanner.nextLine();
            }
        }
    }

}
