package jp.co.syslinks.sscce.java.pop3smtp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
>>> Connecting to "127.0.0.1" [2021/04/16 00:42:41] <<<
+OK POP3 server ready
USER koryo
+OK
PASS ********
+OK
STAT
+OK
LIST
+OK 2 messages:
1 391
2 462
.
UIDL
+OK
1 10c0035cb84ff36f078b26fe89050b6c
2 1504786cef65214303beb3426b51fd3f
.
QUIT
+OK
 */
public class Pop3Server extends SelectorServer {

    @Override
    protected void handle(SelectionKey key) throws IOException {
        //根据不同事件处理
        if (key.isAcceptable()) {
            handleAccept(key);
        } else if (key.isReadable()) {
            handleRead(key);
        } else if (key.isWritable()) {
            handleWrite(key);
        }
    }

    //1 先连接
    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        // channel.write(ByteBuffer.wrap("+OK POP3 server ready".getBytes(StandardCharsets.UTF_8)));
        // channel.register(key.selector(), SelectionKey.OP_READ);
        channel.register(key.selector(), SelectionKey.OP_WRITE, new PipeData("+OK POP3 server ready\n", false));
    }

    final private static class PipeData {
        final public String value;
        final public boolean close;

        public PipeData(String value, boolean close) {
            this.value = value;
            this.close = close;
        }
    }

    //2 再读取
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        StringBuilder builder = new StringBuilder();
        while (channel.read(buffer) > 0) {
            buffer.flip();
            builder.append(StandardCharsets.UTF_8.decode(buffer).toString());
        }
        System.out.println(builder);
        //        String resp = RedisHandler.handleCmd(builder.toString()); //redis逻辑处理完成，然后进行返回给客户端
        String result = "+OK\n";
        boolean close = false;
        if (builder.toString().startsWith("USER ")) {

        } else if (builder.toString().startsWith("PASS ")) {

        } else if (builder.toString().equals("STAT\r\n")) {

        } else if (builder.toString().equals("LIST\r\n")) {
            result += ".\n";
        } else if (builder.toString().equals("UIDL\r\n")) {
            result += ".\n";
        } else if (builder.toString().equals("QUIT\r\n")) {
            close = true;
        }
        channel.register(key.selector(), SelectionKey.OP_WRITE, new PipeData(result, close)); //结果放到attachment
    }

    //3 最后写
    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.configureBlocking(false);
        PipeData data = (PipeData) key.attachment(); //获取到结果数据
        channel.write(ByteBuffer.wrap(data.value.getBytes(StandardCharsets.UTF_8)));
        if (data.close) {
            channel.close(); //写入并关闭channel
        } else {
            channel.register(key.selector(), SelectionKey.OP_READ);
        }
    }

}
