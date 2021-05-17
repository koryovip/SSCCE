package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_Utf8 extends ClassPartToken {

    public final int index;
    public final byte[] str;

    public CONSTANT_Utf8(int index, byte[] str) {
        this.index = index;
        this.str = str;
    }

    @Override
    public String value() {
        return "1";
    }

    @Override
    public void out() {
        System.out.print(String.format("% 5d | %s : ", index, value()));
        System.out.println(this.str == null ? "" : new String(this.str));
    }

}
