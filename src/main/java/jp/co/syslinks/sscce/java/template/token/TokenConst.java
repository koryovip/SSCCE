package jp.co.syslinks.sscce.java.template.token;

public class TokenConst extends TokenBase {

    public final String value;

    public TokenConst(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TokenConst [value=" + value + "]";
    }

}
