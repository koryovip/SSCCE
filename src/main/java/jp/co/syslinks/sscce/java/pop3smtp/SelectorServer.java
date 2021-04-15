package jp.co.syslinks.sscce.java.pop3smtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public abstract class SelectorServer {

    public void start(int port) {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open();) {
            serverChannel.bind(new InetSocketAddress("0.0.0.0", port)); //绑定ip和端口
            serverChannel.configureBlocking(false); //设置非阻塞,nio特性
            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT); //处理连接进入事件
            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); //触发的事件集合
                while (iterator.hasNext()) {
                    handle(iterator.next());
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    protected abstract void handle(SelectionKey key) throws IOException; //扩展类需要实现的方法。
}