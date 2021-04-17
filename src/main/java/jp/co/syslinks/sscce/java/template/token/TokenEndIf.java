package jp.co.syslinks.sscce.java.template.token;

public class TokenEndIf extends TokenBase {

    public int startIndex = -1;
    public int endIndex = -1;

    public TokenEndIf() {
    }

    @Override
    public String toString() {
        return "TokenEndIf [startIndex=" + startIndex + ", endIndex=" + endIndex + "]";
    }

}
