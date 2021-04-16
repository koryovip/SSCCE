package jp.co.syslinks.sscce.java.pop3smtp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
% telnet some.where.co.jp 25

Trying some.where.co.jp ...

Connected to some.where.co.jp.

Escape character is '^]'.

220 some.computer.com Sendmail 4.1/SMI-4.1 ready at Fri, 13 Nov 98 11:10:10 MDT

> HELO another.place.com

250

> MAIL FROM: jobs@some.computer.co.jp

250 ok

> RCPT TO: gates@some.software.co.jp

250 ok

> DATA

354 Enter mail, end with "." on a line by itself



> Dear Gates

> I would like to be grateful it if you could buy my company.

> Jobs



> .





> QUIT

250 ok

@see http://research.nii.ac.jp/~ichiro/syspro98/smtp.html
 */
public class SmtpServer extends SelectorServer {

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
        // channel.register(key.selector(), SelectionKey.OP_READ);
        channel.register(key.selector(), SelectionKey.OP_WRITE, new PipeData("220 some.computer.com Sendmail 4.1/SMI-4.1 ready at Fri, 13 Nov 98 11:10:10 MDT\n", false));
    }

    final private static class PipeData {
        final public String value;
        final public boolean close;

        public PipeData(String value, boolean close) {
            this.value = value;
            this.close = close;
        }
    }

    private boolean isMailBody = false;
    private StringBuilder mailBody = new StringBuilder();

    //2 再读取
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(64);
        StringBuilder builder = new StringBuilder();
        while (channel.read(buffer) > 0) {
            buffer.flip();
            builder.append(StandardCharsets.UTF_8.decode(buffer).toString());
            buffer.clear();
        }
        System.out.print(builder);
        //        String resp = RedisHandler.handleCmd(builder.toString()); //redis逻辑处理完成，然后进行返回给客户端
        String result = "250 OK\n";
        boolean close = false;
        if (isMailBody) {
            mailBody.append(builder);
            isMailBody = false;
        }
        if (builder.toString().startsWith("EHLO ")) {
        } else if (builder.toString().startsWith("MAIL FROM:")) {
        } else if (builder.toString().startsWith("RCPT TO:")) {
        } else if (builder.toString().equals("RSET\r\n")) {
        } else if (builder.toString().equals("DATA\r\n")) {
            result = "354 Enter mail, end with \".\" on a line by itself\n";
            isMailBody = true;
        } else if (builder.toString().equals("QUIT\r\n")) {
            System.out.println("--------------------------------");
            System.out.println(mailBody.toString());
            System.out.println("--------------------------------");
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
