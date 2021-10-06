package jp.co.syslinks.sscce.java.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kou
 * <pre>
 * @SuppressWarnings をなくすため
 * </pre>
 * @see https://qiita.com/shojit/items/e6a3ac1b3400b3dc6352
 */
public enum Cast {
    me;

    @SuppressWarnings("unchecked")
    public <T> T cast(Class<?> clazz, Object obj) {
        if (obj != null && !(clazz.isInstance(obj)))
            throw new ClassCastException("...");
        return (T) obj;
    }

    public static void main(String[] args) {
        Map<String, String> hoge = new HashMap<>();
        hoge.put("key", "value");
        test1(hoge);
        test2(hoge);
    }

    @SuppressWarnings("unchecked")
    public static void test1(Object obj) {
        Map<String, String> hoge = (Map<String, String>) obj;
        System.out.println(hoge.size());
    }

    public static void test2(Object obj) {
        // No Warning
        Map<String, String> hoge = Cast.me.cast(Map.class, obj);
        System.out.println(hoge.size());
    }

}
