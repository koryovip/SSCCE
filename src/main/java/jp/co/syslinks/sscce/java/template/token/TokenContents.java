package jp.co.syslinks.sscce.java.template.token;

public class TokenContents extends TokenBase {

    public final String contents;

    public TokenContents(String contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "TokenContents [contents=" + contents + "]";
    }

}
