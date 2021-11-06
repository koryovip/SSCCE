package jp.co.syslinks.sscce.java.proxy;

/**
 * 静态代理
 * @author ngcly
 * @version V1.0
 * @since 2021/8/20 12:33
 */
public class UserServiceProxy implements IUserService{
    /**代理目标类*/
    private IUserService target;

    public UserServiceProxy(IUserService userService){
        this.target = userService;
    }

    @Override
    public void add(String msg) {
        System.out.println("----------事务开始----------");
        target.add(msg);
        System.out.println("----------事务结束----------");
    }

    @Override
    public void add() {
        System.out.println("***********事务开始***********");
        target.add();
        System.out.println("***********事务结束***********");
    }

    @Override
    public String get() {
        System.out.println("############get事务开始############");
        String result = target.get();
        System.out.println("############get事务结束############");
        return result;
    }
}