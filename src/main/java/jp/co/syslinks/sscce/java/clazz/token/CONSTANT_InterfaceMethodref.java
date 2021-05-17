package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_InterfaceMethodref extends ClassPartToken {

    public CONSTANT_InterfaceMethodref(int index) {
        super(index);
    }

    @Override
    public String value() {
        return "11";
    }

    @Override
    protected void out1() {
        System.out.println();
    }

}
