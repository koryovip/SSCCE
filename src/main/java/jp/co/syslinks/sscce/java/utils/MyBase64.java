package jp.co.syslinks.sscce.java.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class MyBase64 {

    public static void main(String[] args) throws IOException {
        encodeToString();
        // decodeToFile();
    }

    private static void encodeToString() throws IOException {
        try (Writer ww = new FileWriter(new File("C:/Users/xxx/Desktop/base64/activerecord-5.0.4-sources.jar.7z.txt"))) {
            ww.write(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get("C:/Users/xxx/Desktop/base64/activerecord-5.0.4-sources.jar.7z"))));
        }
    }

    private static void decodeToFile() throws IOException {
        try (FileOutputStream ww = new FileOutputStream(new File("C:/Users/xxx/Desktop/test.myapp.7z.txt"))) {
            ww.write(Base64.getDecoder().decode(Files.readAllBytes(Paths.get("C:/Users/xxx/Desktop/test.myapp.7z"))));
        }
    }

}
