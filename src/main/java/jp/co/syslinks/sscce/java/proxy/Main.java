package jp.co.syslinks.sscce.java.proxy;

/**
 * https://www.jianshu.com/p/7a39b4a536d2
 *
 * 代理类测试
 * @author ngcly
 * @version V1.0
 * @since 2021/8/20 12:31
 */
public class Main {
    public static void main(String[] args) {
        //静态代理
        IUserService staticProxy = new UserServiceProxy(new UserService());
        //自定义接口类动态代理
        IUserService diyProxy = new ClassProxyFactory<>(new UserService()).getProxyInstance();
        //自定义 非接口类动态代理
        NoInterfaceService diyNoProxy = new ClassProxyFactory<>(new NoInterfaceService()).getProxyInstance();

        staticProxy.add("静态代理");
        diyProxy.add("自定义动态代理");
        diyProxy.add();
        diyNoProxy.save("非接口自定义代理");
        System.out.println(diyProxy.get());
    }

}