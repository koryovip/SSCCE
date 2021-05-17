package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_Double extends ClassPartToken {

    public final int index;
    public final byte b1;
    public final byte b2;
    public final byte b3;
    public final byte b4;
    public final byte b5;
    public final byte b6;
    public final byte b7;
    public final byte b8;

    public CONSTANT_Double(int index, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        this.index = index;
        this.b1 = b1;
        this.b2 = b2;
        this.b3 = b3;
        this.b4 = b4;
        this.b5 = b5;
        this.b6 = b6;
        this.b7 = b7;
        this.b8 = b8;
    }

    @Override
    public String value() {
        return "6";
    }

    @Override
    public void out() {
        System.out.print(String.format("% 5d | %s : ", index, value()));
        System.out.println(String.format("%02X %02X %02X %02X %02X %02X %02X %02X", b1, b2, b3, b4, b5, b6, b7, b8));
    }

}
