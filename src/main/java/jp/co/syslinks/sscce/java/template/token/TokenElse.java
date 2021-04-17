package jp.co.syslinks.sscce.java.template.token;

public class TokenElse extends TokenBase {

    public int startIndex = -1;
    public int endIndex = -1;

    public TokenElse() {
    }

    @Override
    public String toString() {
        return "TokenElse [startIndex=" + startIndex + ", endIndex=" + endIndex + "]";
    }

}
