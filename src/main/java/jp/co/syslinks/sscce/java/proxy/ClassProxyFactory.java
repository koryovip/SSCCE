package jp.co.syslinks.sscce.java.proxy;

import java.lang.reflect.Method;

/**
 * 自定义动态代理业务方法实现
 * @author ngcly
 * @version V1.0
 * @since 2021/8/20 17:28
 */
public class ClassProxyFactory<T> implements MyInvocationHandler {
    private T target;

    public ClassProxyFactory(T t) {
        this.target = t;
    }

    public T getProxyInstance() {
        //此处采用自定义类加载器
        MyClassLoader classLoader = new MyClassLoader();
        Class[] classes;
        /**判断目标被代理对象是否有接口，若无接口则直接用代理对象*/
        if (target.getClass().getInterfaces().length > 0) {
            classes = target.getClass().getInterfaces();
        } else {
            classes = new Class[] { target.getClass() };
        }
        return (T) MyProxy.newProxyInstance(classLoader, classes, this);
    }

    /**
     * 代理增强方法具体实现
     *
     * 此处方法及参数设计参照了JDK的设计 和 JDK 的 InvocationHandler接口设计一致
     * 其中第一个参数 obj 代理对象这个参数 在这里其实没有什么用处 完全可以省略掉。
     * 而官方设计该参数的目的及作用如下：
     * 1. 可以使用反射获取代理对象的信息（也就是proxy.getClass().getName()）。
     * 2. 可以将代理对象返回以进行连续调用，这就是proxy存在的目的，因为this并不是代理对象。
     * @param obj 代理对象
     * @param method 代理方法
     * @param args 代理参数
     * @return 方法返回结果
     * @throws Throwable
     */
    @Override
    public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
        System.out.println("**********开始事务**********");
        Object value = method.invoke(target, args);
        System.out.println("调用目标方法返回：" + value);
        System.out.println("**********结束事务**********");
        return value;
    }
}