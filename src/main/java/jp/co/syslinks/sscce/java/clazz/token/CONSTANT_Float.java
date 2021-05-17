package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_Float extends ClassPartToken {

    public final int index;
    public final byte b1;
    public final byte b2;
    public final byte b3;
    public final byte b4;

    public CONSTANT_Float(int index, byte b1, byte b2, byte b3, byte b4) {
        this.index = index;
        this.b1 = b1;
        this.b2 = b2;
        this.b3 = b3;
        this.b4 = b4;
    }

    @Override
    public String value() {
        return "4";
    }

    @Override
    public void out() {
        System.out.print(String.format("% 5d | %s : ", index, value()));
        System.out.println(String.format("%02X %02X %02X %02X", b1, b2, b3, b4));
    }
}
