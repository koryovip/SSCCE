package jp.co.syslinks.sscce.java.clazz.token;

public class CONSTANT_String extends ClassPartToken {

    public final int index;
    public final int val;

    public CONSTANT_String(int index, int val) {
        this.index = index;
        this.val = val;
    }

    @Override
    public String value() {
        return "8";
    }

    @Override
    public void out() {
        System.out.print(String.format("% 5d | %s : ", index, value()));
        System.out.println("#" + this.val);
    }

}
