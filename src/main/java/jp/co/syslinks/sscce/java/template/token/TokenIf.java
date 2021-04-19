package jp.co.syslinks.sscce.java.template.token;

public class TokenIf extends TokenBase {

    public int startIndex = -1;
    public int endIndex = -1;

    final public String left1;
    final public String left2;
    final public String cond;
    final public String right1;
    final public String right2;

    public TokenIf(String left1, String left2, String cond, String right1, String right2) {
        this.left1 = left1;
        this.left2 = left2;
        this.cond = cond;
        this.right1 = right1;
        this.right2 = right2;
    }

    @Override
    public String toString() {
        return "TokenIf [startIndex=" + startIndex + ", endIndex=" + endIndex + ", left1=" + left1 + ", left2=" + left2 + ", cond=" + cond + ", right1=" + right1 + ", right2=" + right2 + "]";
    }

}
