package jp.co.syslinks.sscce.java.template.token;

public class TokenEnd extends TokenBase {

    public int forIndex = -1;

    public TokenEnd() {
    }

    @Override
    public String toString() {
        return "TokenEnd [forIndex=" + forIndex + "]";
    }

}
