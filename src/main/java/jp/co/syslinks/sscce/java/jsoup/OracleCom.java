package jp.co.syslinks.sscce.java.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OracleCom {

    public static void main(String[] args) throws Exception {
        //String url = "https://www.oracle.com/java/technologies/javase/17-relnote-issues.html"; // 17
        //String url = "https://www.oracle.com/java/technologies/javase/17-0-1-relnotes.html"; // 17.0.1
        //String url = "https://www.oracle.com/java/technologies/javase/17-0-2-relnotes.html"; // 17.0.2
        //String url = "https://www.oracle.com/java/technologies/javase/17-0-3-relnotes.html"; // 17.0.3
        //String url = "https://www.oracle.com/java/technologies/javase/17-0-3-relnotes.html"; // 17.0.3.1
        //String url = "https://www.oracle.com/java/technologies/javase/17-0-4-relnotes.html"; // 17.0.4
        //String url = "https://www.oracle.com/java/technologies/javase/17-0-4-relnotes.html"; // 17.0.4.1
        //String url = "https://www.oracle.com/java/technologies/javase/17-0-5-relnotes.html"; // 17.0.5
        //String url = "https://www.oracle.com/java/technologies/javase/17-0-6-relnotes.html"; // 17.0.6
        String url = "https://www.oracle.com/java/technologies/javase/17-0-7-relnotes.html"; // 17.0.7
        System.out.println(url);
        System.out.println("-----------------------------------------------------------------------------------------------");
        Document doc = query(url);
        Elements select = doc.select("div.release-note");
        select.forEach(div -> {
            System.out.println(div.text());
            System.out.println("-----------------------------------------------------------------------------------------------");
        });
    }

    public static Document query(String url) throws Exception {
        int perPage = 10000;
        return Jsoup.connect(url) //
                .ignoreContentType(true) // 大事
                .ignoreHttpErrors(false) // 大事
                .userAgent("chrome") // ここの値は適当
                .timeout(30_100) // timeout 大事。長くすること
                .maxBodySize(0) // 大事。0にすること
                .method(org.jsoup.Connection.Method.GET) //
                .execute() //
                .parse();
    }

}
