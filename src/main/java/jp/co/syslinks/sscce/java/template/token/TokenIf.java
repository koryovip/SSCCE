package jp.co.syslinks.sscce.java.template.token;

public class TokenIf extends TokenBase {

    public int startIndex = -1;
    public int endIndex = -1;

    public String exp;

    public TokenIf(String exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        return "TokenIf [exp=" + exp + ", startIndex=" + startIndex + ", endIndex=" + endIndex + "]";
    }

}
