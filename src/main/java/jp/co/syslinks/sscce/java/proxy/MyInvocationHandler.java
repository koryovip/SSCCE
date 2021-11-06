package jp.co.syslinks.sscce.java.proxy;

import java.lang.reflect.Method;

/**
 * 自定义动态代理方法规范接口
 * @author ngcly
 * @version V1.0
 * @since 2021/8/20 15:38
 */
public interface MyInvocationHandler {
    /**
     * 代理增强方法接口
     * 此处方法及参数设计参照了JDK的设计 和 JDK 的 InvocationHandler接口设计一致
     * 其中第一个参数 obj 代理对象这个参数 在这里其实没有什么用处 完全可以省略掉。
     * 而官方设计该参数的目的及作用如下：
     * 1. 可以使用反射获取代理对象的信息（也就是proxy.getClass().getName()）。
     * 2. 可以将代理对象返回以进行连续调用，这就是proxy存在的目的，因为this并不是代理对象。
     * @param obj 代理对象
     * @param method 代理方法
     * @param args 代理参数
     * @return 方法调用结果
     * @throws Throwable 抛出异常
     */
    Object invoke(Object obj, Method method, Object[] args) throws Throwable;
}