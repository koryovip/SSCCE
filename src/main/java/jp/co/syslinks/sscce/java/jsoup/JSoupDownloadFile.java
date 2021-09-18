package jp.co.syslinks.sscce.java.jsoup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class JSoupDownloadFile {

    /**
     * Jsoup を使ってファイルをダウンロードする
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final Response response = Jsoup.connect("https://github.com/koryovip/SSCCE/archive/refs/heads/main.zip") //
                .ignoreContentType(true) // 大事
                .ignoreHttpErrors(false) // 大事
                .header("key", "value") // ここの値は適当
                .userAgent("chrome") // ここの値は適当
                .timeout(10_100) // timeout 大事。長くすること
                .maxBodySize(0) // 大事。0にすること
                .method(org.jsoup.Connection.Method.GET) //
                .execute();
        try (BufferedInputStream bodyStream = response.bodyStream(); //
                BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(new File("r:/SSCCE.zip"))); //
        ) {
            int buffSize = 1024 * 8;
            byte[] buff = new byte[buffSize];
            int read;
            while ((read = bodyStream.read(buff, 0, buffSize)) != -1) {
                writer.write(buff, 0, read);
            }
            writer.flush();
        }
    }

}
