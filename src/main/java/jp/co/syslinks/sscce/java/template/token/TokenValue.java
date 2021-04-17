package jp.co.syslinks.sscce.java.template.token;

public class TokenValue extends TokenBase {

    public final String key;
    public final String key2;

    public TokenValue(String key) {
        this(key, null);
    }

    public TokenValue(String key, String key2) {
        this.key = key;
        this.key2 = key2;
    }

    @Override
    public String toString() {
        return "TokenValue [key=" + key + ", key2=" + key2 + "]";
    }

}
