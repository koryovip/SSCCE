package jp.co.syslinks.sscce.java.aop;

import java.util.HashMap;
import java.util.Random;

import jp.co.syslinks.sscce.java.aop.clazz.MyCache;
import jp.co.syslinks.sscce.java.aop.clazz.SomeClass01;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * cglib sample
 * @see https://www.baeldung.com/cglib
 */
public class TestSomeClass01 {

    public static void main(String[] args) throws Exception {
        SomeClass01 proxy = main1(SomeClass01.class);
        System.out.println(proxy.method01());
        for (int ii = 0; ii < 10; ii++) {
            System.out.println(proxy.method01(1));
        }
        System.out.println(proxy.method01(1, "2"));
    }

    private static final Random random = new Random();
    static {
        random.setSeed(System.currentTimeMillis());
    }

    public static <T> T main1(Class<T> clazz) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            // System.out.println(method);
            MyCache myCache = method.getAnnotation(MyCache.class);
            if (myCache != null) {
                if (random.nextInt(10) > 3) {
                    System.out.println("get value from cache by key : " + myCache.key());
                    return getValueFromCache(myCache.key());
                }
                Object retObj = proxy.invokeSuper(obj, args);
                if (retObj != null) {
                    setValueToCache(myCache.key(), retObj);
                }
                return retObj;
            } else {
                return proxy.invokeSuper(obj, args);
            }
        });
        return (T) enhancer.create();
    }

    private static Object getValueFromCache(String key) {
        return new HashMap<String, Integer>() {
            private static final long serialVersionUID = -8656763338730022990L;
            {
                put("Paul", 20);
                put("John", 30);
                put("Karen", 40);
            }
        };
    }

    private static boolean setValueToCache(String key, Object obj) {
        return true;
    }
}
