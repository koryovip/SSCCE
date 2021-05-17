package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_MethodType extends ClassPartToken {
    public CONSTANT_MethodType(int index) {
        super(index);
    }

    @Override
    public String value() {
        return "16";
    }

    @Override
    protected void out1() {
        System.out.println();
    }

}
