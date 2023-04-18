package jp.co.syslinks.sscce.java.jsoup;

import java.util.List;

public class FoojayIOBean {
    public meta meta;
    public List<data> data;

    static class meta {
        public int found_posts;
    }

    static class data {
        public int id;
        public String issue;
        public String component;
        public String priority;
        public String title;
    }

}
