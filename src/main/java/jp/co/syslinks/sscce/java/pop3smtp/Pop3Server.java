package jp.co.syslinks.sscce.java.pop3smtp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
        PipeData data = new PipeData();
        data.command = "+OK POP3 server ready\n";
        channel.register(key.selector(), SelectionKey.OP_WRITE, data);
    }

    final private static class PipeData {
        public String command;
        public boolean close = false;

        public PipeData() {
        }

        public final void close() throws IOException {

        }
    }

    private static final int buffSize = 10 * 1024;

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.configureBlocking(false);
        PipeData data = (PipeData) key.attachment();
        data.command = null;
        data.close = false;

        final ByteBuffer buffer = ByteBuffer.allocate(buffSize);
        final StringBuilder builder = new StringBuilder();
        final byte[] buffArray = new byte[buffSize];
        int read = -1;
        while ((read = channel.read(buffer)) > 0) {
            buffer.flip();
            buffer.get(buffArray, 0, read);
            for (int ii = 0; ii < read; ii++) {
                byte bb = buffArray[ii];
                if (bb == '\r') { // 13
                    continue;
                }
                if (bb == '\n') { // 10
                    data.command = "+OK\n";
                    final String command = builder.toString();
                    System.out.println("> " + command);

                    if (command.startsWith("USER ")) {
                    } else if (command.startsWith("PASS ")) {
                    } else if (command.equals("NOOP")) {
                    } else if (command.equals("STAT")) {
                        int mailCount = 0;
                        long mailLength = 0;
                        for (File file : new File("_mail/data").listFiles()) {
                            mailCount++;
                            mailLength += file.length();
                        }
                        data.command = "+OK " + mailCount + " " + mailLength + "\n";
                    } else if (command.equals("CAPA")) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("+OK Capability list follows\r\n");
                        //sb.append("TOP\r\n");
                        sb.append("USER\r\n");
                        //sb.append("SASL CRAM-MD5 KERBEROS_V4\r\n");
                        //sb.append("RESP-CODES\r\n");
                        sb.append("LOGIN-DELAY 900\r\n");
                        sb.append("EXPIRE 60\r\n");
                        sb.append("UIDL\r\n");
                        //sb.append("IMPLEMENTATION Shlemazle-Plotz-v302\r\n");
                        sb.append(".\r\n");
                        data.command = sb.toString();
                    } else if (command.startsWith("TOP ")) {
                        throw new RuntimeException("NOT support Command:" + command);
                    } else if (command.equals("LIST")) {
                        StringBuilder sb = new StringBuilder();
                        int index = 1;
                        for (File file : new File("_mail/data").listFiles()) {
                            sb.append(index++).append(" ").append(file.length()).append("\n");
                        }
                        sb.append(".\n");
                        data.command += sb.toString();
                    } else if (command.equals("UIDL")) {
                        StringBuilder sb = new StringBuilder();
                        int index = 1;
                        for (File file : new File("_mail/data").listFiles()) {
                            sb.append(index++).append(" ").append(file.getName()).append("\n");
                        }
                        sb.append(".\n");
                        data.command += sb.toString();
                    } else if (command.startsWith("RETR ")) {
                        int index = Integer.parseInt(command.substring("RETR ".length()));
                        channel.write(ByteBuffer.wrap("+OK\n".getBytes(StandardCharsets.UTF_8)));
                        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File("_mail/data").listFiles()[index - 1]))) {
                            int flen = 1024;
                            byte[] fileBuff = new byte[flen];
                            int rd = -1;
                            while ((rd = bis.read(fileBuff, 0, flen)) > 0) {
                                channel.write(ByteBuffer.wrap(fileBuff, 0, rd));
                            }
                        }
                    } else if (command.equals("QUIT")) {
                        data.close = true;
                    }
                    builder.setLength(0);
                    continue;
                }
                builder.append((char) bb);
            }
            buffer.clear();
        }
        if (data.command == null) {
            channel.register(key.selector(), SelectionKey.OP_READ, data);
        } else {
            channel.register(key.selector(), SelectionKey.OP_WRITE, data);
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.configureBlocking(false);
        PipeData data = (PipeData) key.attachment();
        channel.write(ByteBuffer.wrap(data.command.getBytes(StandardCharsets.UTF_8)));
        if (data.close) {
            data.close();
            channel.close();
        } else {
            channel.register(key.selector(), SelectionKey.OP_READ, data);
        }
    }

}
