package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_Fieldref extends ClassPartToken {

    public final int index;
    public final int val1;
    public final int val2;

    public CONSTANT_Fieldref(int index, int val1, int val2) {
        this.index = index;
        this.val1 = val1;
        this.val2 = val2;
    }

    @Override
    public String value() {
        return "9";
    }

    @Override
    public void out() {
        System.out.print(String.format("% 5d | %s : ", index, value()));
        System.out.println("#" + this.val1 + "," + this.val2);
    }

}
