package jp.co.syslinks.sscce.java.api.github;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Github_releases {

    public static void main(String[] args) throws Exception {
        List<Github_releases_bean> list = new ArrayList<Github_releases_bean>(210);
        Gson gson = new Gson();
        for (int page = 1; page <= 3; page++) {
            // https://docs.github.com/en/rest/reference/repos#releases
            final Document document = Jsoup.connect("https://api.github.com/repos/spring-projects/spring-framework/releases") //
                    .header("Accept", "application/vnd.github.v3+json") //
                    .data("per_page", "100") // Results per page (max 100).
                    .data("page", Integer.toString(page)) // Page number of the results to fetch.
                    .ignoreContentType(true) //
                    .ignoreHttpErrors(false) //
                    .get();
            list.addAll(gson.fromJson(document.text(), TypeToken.getParameterized(ArrayList.class, Github_releases_bean.class).getType()));
        }

        Pattern version = Pattern.compile("v(\\d+)\\.(\\d+)\\.(\\d+)"); // v5.1.10.RELEASE
        Collections.sort(list, (o1, o2) -> {
            final Matcher matcher1 = version.matcher(o1.tag_name);
            final Matcher matcher2 = version.matcher(o2.tag_name);
            if (matcher1.find() && matcher2.find()) {
                int v11 = Integer.parseInt(matcher1.group(1));
                int v12 = Integer.parseInt(matcher1.group(2));
                int v13 = Integer.parseInt(matcher1.group(3));
                int v21 = Integer.parseInt(matcher2.group(1));
                int v22 = Integer.parseInt(matcher2.group(2));
                int v23 = Integer.parseInt(matcher2.group(3));
                if (v11 > v21) {
                    return 1;
                }
                if (v11 < v21) {
                    return -1;
                }
                if (v12 > v22) {
                    return 1;
                }
                if (v12 < v22) {
                    return -1;
                }
                if (v13 > v23) {
                    return 1;
                }
                if (v13 < v23) {
                    return -1;
                }
                return o1.tag_name.compareTo(o2.tag_name);
            } else {
                throw new RuntimeException(o1.tag_name + " : " + o2.tag_name);
            }
        });
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        for (Github_releases_bean bean : list) {
            System.out.println(String.format("======== %-15s  %s ========================", bean.tag_name, dtf.format(LocalDateTime.ofInstant(bean.published_at.toInstant(), ZoneId.systemDefault()))));
            System.out.println(bean.body);
        }
    }

    public static class Github_releases_bean {
        public String url;
        public String assets_url;
        public String upload_url;
        public String html_url;
        public int id;
        public String tag_name;
        public String target_commitish;
        public String name;
        public boolean prerelease;
        public Date created_at;
        public Date published_at;
        public String body;
    }
}
