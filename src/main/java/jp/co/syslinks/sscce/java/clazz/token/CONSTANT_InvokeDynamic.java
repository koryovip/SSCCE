package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_InvokeDynamic extends ClassPartToken {

    public CONSTANT_InvokeDynamic(int index) {
        super(index);
    }

    @Override
    public String value() {
        return "18";
    }

    @Override
    protected void out1() {
        System.out.println();
    }

}
