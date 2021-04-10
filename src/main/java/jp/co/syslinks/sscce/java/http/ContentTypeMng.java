package jp.co.syslinks.sscce.java.http;

import java.io.File;

/**
 * https://developer.mozilla.org/ja/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
 * https://qiita.com/AkihiroTakamura/items/b93fbe511465f52bffaa
 */
public enum ContentTypeMng {
    me;

    public String get(File file) {
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public String get(String key) {
        switch (key) {
        case "htm":
        case "html":
            return "text/html";
        case "css":
            return "text/css";
        case "js":
            return "text/javascript";
        case "json":
            return "application/json";
        case "zip":
            return "application/zip";
        default:
            return "application/octet-stream";
        }
    }
}
