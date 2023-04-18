package jp.co.syslinks.sscce.java.jsoup;

import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Javaalmanac {
    static private final ObjectMapper objectMapper = new ObjectMapper() //
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws Exception {

        String json = Jsoup.connect("https://data.javaalmanac.io/v1/jdk/versions/21/apidiffs/17") //
                .ignoreContentType(true) // 大事
                .ignoreHttpErrors(false) // 大事
                .userAgent("chrome") // ここの値は適当
                .timeout(10_100) // timeout 大事。長くすること
                .maxBodySize(0) // 大事。0にすること
                .method(org.jsoup.Connection.Method.GET) //
                .execute().body();
        JavaAPIDiff diff = objectMapper.readValue(json, JavaAPIDiff.class);
        diff.deltas.forEach(delta1 -> {
            delta1.deltas.forEach(delta2 -> { // package
                if (delta2.deltas == null) {
                    if ("modified".equals(delta2.status)) {
                        System.out.println(delta2.type + "\t" + delta2.name + "\t" + delta2.status + "\t" + dddd(delta2.addedTags) + "\t" + dddd(delta2.removedTags));
                    } else {
                        System.out.println(delta2.type + "\t" + delta2.name + "\t" + delta2.status);
                    }
                } else {
                    delta2.deltas.forEach(delta3 -> { // class
                        if ("modified".equals(delta2.status)) {
                            System.out.println(delta2.type + "\t" + delta2.name + "\t" + delta2.status + "\t" + dddd(delta2.addedTags) + "\t" + dddd(delta2.removedTags));
                        }
                        if (delta3.deltas == null) {
                            if ("modified".equals(delta3.status)) {
                                System.out.println(delta3.type + "\t" + delta2.name + "." + delta3.name + "\t" + delta3.status + "\t" + dddd(delta3.addedTags) + "\t" + dddd(delta3.removedTags));
                            } else {
                                System.out.println(delta3.type + "\t" + delta2.name + "." + delta3.name + "\t" + delta3.status);
                            }
                        } else {
                            if ("modified".equals(delta3.status)) {
                                System.out.println(delta3.type + "\t" + delta2.name + "." + delta3.name + "\t" + delta3.status + "\t" + dddd(delta3.addedTags) + "\t" + dddd(delta3.removedTags));
                            }
                            delta3.deltas.forEach(delta4 -> { // method
                                if ("modified".equals(delta4.status)) {
                                    System.out.println(delta4.type + "\t" + delta2.name + "." + delta3.name + "." + delta4.name + "\t" + delta4.status + "\t" + dddd(delta4.addedTags) + "\t" + dddd(delta4.removedTags));
                                } else {
                                    System.out.println(delta4.type + "\t" + delta2.name + "." + delta3.name + "." + delta4.name + "\t" + delta4.status);
                                }
                            });
                        }
                    });
                }
            });
        });
    }

    private static String dddd(List<String> tags) {
        if (tags == null) {
            return "";
        }
        return String.join(",", tags);
    }

    public static void main2(String[] args) throws Exception {
        final Response response = Jsoup.connect("https://javaalmanac.io/jdk/18/apidiff/17/") //
                .ignoreContentType(true) // 大事
                .ignoreHttpErrors(false) // 大事
                .userAgent("chrome") // ここの値は適当
                .timeout(10_100) // timeout 大事。長くすること
                .maxBodySize(0) // 大事。0にすること
                .method(org.jsoup.Connection.Method.GET) //
                .execute();
        Document doc = response.parse();
        Elements elements = doc.select("ul.tag");
        for (Element element : elements) {
            if (element.text().isEmpty()) {
                continue;
            }
            Element child = element.parent().previousElementSibling().child(0).child(0);
            if (child.tagName().equals("a")) {
                System.out.println(child.attr("href"));
            } else {
                System.out.println(child.text());
            }
        }
    }

}
