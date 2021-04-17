package jp.co.syslinks.sscce.java.template.token;

public class TokenFor extends TokenBase {

    public final String item;
    public final String items;
    public int endIndex = -1;

    public TokenFor(String item, String items) {
        this.item = item;
        this.items = items;
    }

    @Override
    public String toString() {
        return "TokenFor [item=" + item + ", items=" + items + ", endIndex=" + endIndex + "]";
    }

}
