package jp.co.syslinks.sscce.java.pop3smtp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import jp.co.syslinks.sscce.java.utils.SnowFlake;

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
250 ok
> QUIT
250 ok

@see http://research.nii.ac.jp/~ichiro/syspro98/smtp.html
 */
public class SmtpServer extends SelectorServer {

    @Override
    protected void handle(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            handleAccept(key);
        } else if (key.isReadable()) {
            handleRead(key);
        } else if (key.isWritable()) {
            handleWrite(key);
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        SnowFlake snowFlake = new SnowFlake(2, 3);
        String id = Long.toString(snowFlake.nextId());
        PipeData data = new PipeData(new BufferedOutputStream(new FileOutputStream(new File("_mail/data/" + id), true)));
        data.command = "220 ok\n";
        channel.register(key.selector(), SelectionKey.OP_WRITE, data);
    }

    private enum SmtpStatus {
        BeforeMailBody, MailBody, AfterMailBody;
    }

    private static final class PipeData {
        final public OutputStream os;
        public byte[] last4Bytes = new byte[4];
        public SmtpStatus status = SmtpStatus.BeforeMailBody;
        public String command;
        public boolean close = false;

        public PipeData(OutputStream os) {
            this.os = os;
        }

        public final void write(byte[] b, int off, int len) throws IOException {
            this.os.write(b, off, len);
        }

        public final void close() throws IOException {
            this.os.flush();
            this.os.close();
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
            if (data.status == SmtpStatus.MailBody) {
                /*if (read < 4) {
                    System.err.println(read);
                }
                System.out.print(new String(buffArray, 0, read));
                */
                data.write(buffArray, 0, read);
                if (read >= 4) {
                    System.arraycopy(buffArray, read - 4, data.last4Bytes, 0, 4);
                } else if (read == 3) {
                    data.last4Bytes[0] = data.last4Bytes[3];
                    data.last4Bytes[1] = buffArray[0];
                    data.last4Bytes[2] = buffArray[1];
                    data.last4Bytes[3] = buffArray[2];
                } else if (read == 2) {
                    data.last4Bytes[0] = data.last4Bytes[2];
                    data.last4Bytes[1] = data.last4Bytes[3];
                    data.last4Bytes[2] = buffArray[0];
                    data.last4Bytes[3] = buffArray[1];
                } else if (read == 1) {
                    data.last4Bytes[0] = data.last4Bytes[2];
                    data.last4Bytes[1] = data.last4Bytes[3];
                    data.last4Bytes[2] = data.last4Bytes[4];
                    data.last4Bytes[3] = buffArray[0];
                } else {
                    throw new RuntimeException("read =0");
                }
                if (data.last4Bytes[0] == '\n' && data.last4Bytes[1] == '.') {
                    data.status = SmtpStatus.AfterMailBody;
                    data.command = "250 OK\n";
                }
            } else {
                for (int ii = 0; ii < read; ii++) {
                    byte bb = buffArray[ii];
                    if (bb == '\r') { // 13
                        continue;
                    }
                    if (bb == '\n') { // 10
                        data.command = "250 OK\n";
                        final String commnad = builder.toString();
                        System.out.println("> " + commnad);
                        if (commnad.startsWith("EHLO ")) {
                        } else if (commnad.startsWith("MAIL FROM:")) {
                        } else if (commnad.startsWith("RCPT TO:")) {
                        } else if (commnad.equals("RSET")) {
                        } else if (commnad.equals("DATA")) {
                            data.command = "354 Enter mail, end with \".\" on a line by itself\n";
                            data.status = SmtpStatus.MailBody;
                        } else if (commnad.equals("QUIT")) {
                            data.close = true;
                        }
                        builder.setLength(0);
                        continue;
                    }
                    builder.append((char) bb);
                }
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
