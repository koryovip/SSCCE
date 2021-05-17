package jp.co.syslinks.sscce.java.clazz.token;

abstract public class ClassPartToken {

    public final int index;

    public ClassPartToken(int index) {
        this.index = index;
    }

    abstract public String value();

    final public void out() {
        System.out.print(String.format("% 5d | %2s : ", index, value()));
        out1();
    }

    abstract protected void out1();

}
