package jp.co.syslinks.sscce.java.jsoup;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.syslinks.sscce.java.jsoup.FoojayIOBean.data;

public class FoojayIO {
    static class JavaReleaseInfo {
        final String version;
        final String quarter;

        public JavaReleaseInfo(String version, String quarter) {
            this.version = version;
            this.quarter = quarter;
        }
    }

    static List<JavaReleaseInfo> list = new ArrayList<>();
    static {
        list.add(new JavaReleaseInfo("17", "092021"));
        list.add(new JavaReleaseInfo("17.0.1", "102021"));
        list.add(new JavaReleaseInfo("17.0.2", "012022"));
        list.add(new JavaReleaseInfo("17.0.3", "042022"));
        list.add(new JavaReleaseInfo("17.0.4", "072022"));
        list.add(new JavaReleaseInfo("17.0.5", "102022"));
        list.add(new JavaReleaseInfo("17.0.6", "012023"));
        list.add(new JavaReleaseInfo("18", "032022"));
        list.add(new JavaReleaseInfo("18.0.1", "042022"));
        list.add(new JavaReleaseInfo("18.0.2", "072022"));
        list.add(new JavaReleaseInfo("19", "092022"));
        list.add(new JavaReleaseInfo("19.0.1", "102022"));
        list.add(new JavaReleaseInfo("19.0.2", "012023"));
        list.add(new JavaReleaseInfo("20", "032023"));
    }

    public static void main(String[] args) throws Exception {
        //        for (JavaReleaseInfo info : list) {
        //            save(info);
        //            TimeUnit.SECONDS.sleep(1);
        //        }
        for (JavaReleaseInfo info : list) {
            parse(info);
            //            System.out.println();
        }
    }

    public static void parse(JavaReleaseInfo info) throws Exception {
        File file = new File("json/" + info.version + ".json");
        FoojayIOBean diff = objectMapper.readValue(file, FoojayIOBean.class);
        for (data data : diff.data) {
            System.out.println(info.version + "\t" + data.id + "\t" + data.component + "\t" + data.title.replaceAll("\t+", " ") + "\t" + data.priority + "\t" + data.issue);
        }
    }

    public static void save(JavaReleaseInfo info) throws Exception {
        try (FileWriter fw = new FileWriter(new File(info.version + ".json"));) {
            fw.write(query(info));
        }
    }

    static private final ObjectMapper objectMapper = new ObjectMapper() //
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static String query(JavaReleaseInfo info) throws Exception {
        int perPage = 10000;
        String url = "https://foojay.io/wp-json/foojay/v1/java-updates/component?quarter=" + info.quarter + "&version=" + info.version + "&page=1&per_page=" + perPage + "&show_additional=true&order=ASC";
        System.out.println(url);
        return Jsoup.connect(url) //
                .ignoreContentType(true) // 大事
                .ignoreHttpErrors(false) // 大事
                .userAgent("chrome") // ここの値は適当
                .timeout(30_100) // timeout 大事。長くすること
                .maxBodySize(0) // 大事。0にすること
                .method(org.jsoup.Connection.Method.GET) //
                .execute() //
                .body();
    }

}
