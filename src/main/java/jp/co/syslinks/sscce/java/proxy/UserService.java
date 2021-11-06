package jp.co.syslinks.sscce.java.proxy;

/**
 * 业务接口实现类
 * @author ngcly
 * @version V1.0
 * @since 2021/8/20 12:30
 */
public class UserService implements IUserService{
    @Override
    public void add(String msg) {
        System.out.println("=============保存============="+msg);
    }

    @Override
    public void add() {
        System.out.println("=============保存=============");
    }

    @Override
    public String get() {
        System.out.println("=============获取=============");
        return "捉到啦";
    }
}