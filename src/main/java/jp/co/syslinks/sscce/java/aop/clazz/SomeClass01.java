package jp.co.syslinks.sscce.java.aop.clazz;

import java.util.HashMap;
import java.util.Map;

public class SomeClass01 {

    public int method01() {
        return 0;
    }

    @MyCache(key = "cacheKey", duration = 30)
    public Map<String, Integer> method01(int val1) {
        return new HashMap<>();
    }

    public int method01(int val1, String val2) {
        return 2;
    }

}
