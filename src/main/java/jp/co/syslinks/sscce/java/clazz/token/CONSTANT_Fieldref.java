package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_Fieldref extends ClassPartToken {

    public final int val1;
    public final int val2;

    public CONSTANT_Fieldref(int index, int val1, int val2) {
        super(index);
        this.val1 = val1;
        this.val2 = val2;
    }

    @Override
    public String value() {
        return "9";
    }

    @Override
    protected void out1() {
        System.out.println("#" + this.val1 + "," + this.val2);
    }

}
