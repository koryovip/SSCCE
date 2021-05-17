package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_Utf8 extends ClassPartToken {

    public final byte[] str;

    public CONSTANT_Utf8(int index, byte[] str) {
        super(index);
        this.str = str;
    }

    @Override
    public String value() {
        return "1";
    }

    @Override
    protected void out1() {
        System.out.println(this.str == null ? "" : new String(this.str));
    }

}
