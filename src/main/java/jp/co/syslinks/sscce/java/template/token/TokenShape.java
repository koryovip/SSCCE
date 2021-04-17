package jp.co.syslinks.sscce.java.template.token;

public class TokenShape extends TokenBase {

    public final String value;

    public TokenShape(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TokenShape";
    }

}
