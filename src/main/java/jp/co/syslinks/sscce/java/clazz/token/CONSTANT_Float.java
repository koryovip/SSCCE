package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_Float extends ClassPartToken {

    public final byte b1;
    public final byte b2;
    public final byte b3;
    public final byte b4;

    public CONSTANT_Float(int index, byte b1, byte b2, byte b3, byte b4) {
        super(index);
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
    protected void out1() {
        System.out.println(String.format("%02X %02X %02X %02X", b1, b2, b3, b4));
    }
}
