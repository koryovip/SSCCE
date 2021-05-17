package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_Class extends ClassPartToken {

    public final int index;
    public final int val;

    public CONSTANT_Class(int index, int val) {
        this.index = index;
        this.val = val;
    }

    @Override
    public String value() {
        return "7";
    }

    @Override
    public void out() {
        System.out.print(String.format("% 5d | %s : ", index, value()));
        System.out.println("#" + this.val);
    }

}
