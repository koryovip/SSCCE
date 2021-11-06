package jp.co.syslinks.sscce.java.proxy;

/**
 * 无接口的普通类
 * @author ngcly
 * @version V1.0
 * @since 2021/8/20 23:19
 */
public class NoInterfaceService {
    public void save(String msg) {
        System.out.println("=============保存=============" + msg);
    }
}