package jp.co.syslinks.sscce.java.template.token;

public class TokenEndFor extends TokenBase {

    public int forIndex = -1;

    public TokenEndFor() {
    }

    @Override
    public String toString() {
        return "TokenEndFor [forIndex=" + forIndex + "]";
    }

}
