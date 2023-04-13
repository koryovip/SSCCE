package jp.co.syslinks.sscce.java.jsoup;

import java.util.List;

public class JavaAPIDiff {
    public List<JavaAPIDiff$deltas> deltas;

    public static class JavaAPIDiff$deltas {
        public String type;
        public String name;
        public String status;
        public List<JavaAPIDiff$deltas$deltas> deltas;
    }

    public static class JavaAPIDiff$deltas$deltas { // class
        public String type;
        public String name;
        public String status;
        public List<String> addedTags;
        public List<String> removedTags;
        public List<JavaAPIDiff$deltas$deltas$deltas> deltas;
    }

    public static class JavaAPIDiff$deltas$deltas$deltas { // method
        public String type;
        public String name;
        public String status;
        public List<String> addedTags;
        public List<String> removedTags;
        public List<JavaAPIDiff$deltas$deltas$deltas$deltas> deltas;
    }

    public static class JavaAPIDiff$deltas$deltas$deltas$deltas {
        public String type;
        public String name;
        public String status;
        public String csr;
        public List<String> addedTags;
        public List<String> removedTags;
    }
}
