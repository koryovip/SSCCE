package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_Class extends ClassPartToken {

    public final int val;

    public CONSTANT_Class(int index, int val) {
        super(index);
        this.val = val;
    }

    @Override
    public String value() {
        return "7";
    }

    @Override
    protected void out1() {
        System.out.println("#" + this.val);
    }

}
