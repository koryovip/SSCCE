package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_MethodHandle extends ClassPartToken {
    public CONSTANT_MethodHandle(int index) {
        super(index);
    }

    @Override
    public String value() {
        return "15";
    }

    @Override
    protected void out1() {
        System.out.println();
    }

}
