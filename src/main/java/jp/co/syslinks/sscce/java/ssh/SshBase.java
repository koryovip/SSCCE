package jp.co.syslinks.sscce.java.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshBase {

    public static void main(String[] args) throws Exception {
        Queue<String> queue = new ArrayDeque<>();
        queue.add("export PS1='[${PIPESTATUS[@]}] \\$ ' ; export HISTCONTROL=ignoreboth ; history -d `history | tail -n 1 | awk '{print $1}'`");
        queue.add(" ls -al");
        queue.add(" aaaaaaaaaaaaa1");
        queue.add(" bbbbbbbbbbbbbbb2");
        queue.add(" cccccccccccccccc3");
        queue.add(" exit");
        Pattern PS1 = Pattern.compile("\\[(\\d+)(?:[ ]+(\\d+))?(?:[ ]+(\\d+))?(?:[ ]+(\\d+))?(?:[ ]+(\\d+))?(?:[ ]+(\\d+))?(?:[ ]+(\\d+))?(?:[ ]+(\\d+))?(?:[ ]+(\\d+))?(?:[ ]+(\\d+))?\\] # ");

        JSch jsch = new JSch();
        Session session = jsch.getSession("root", "192.168.0.150", 22);
        session.setPassword("root");
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(30000);
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setAgentForwarding(true);
        channel.setInputStream(null/*System.in*/);
        channel.setOutputStream(null/*System.out*/);
        channel.setPtyType("vt102", 100, 10, 1024, 1024); // color-type, col, row, w, h
        channel.setEnv("LANG", "ja_JP.UTF-8");
        try (InputStream in = channel.getInputStream(); OutputStream out = channel.getOutputStream();) {
            channel.connect(3 * 1000);

            final int len = 10 * 1024;
            final ByteBuffer lineCutter = ByteBuffer.allocate(len);
            final byte[] buff = new byte[len];
            for (;;) {
                while (in.available() > 0) {
                    int read = in.read(buff, 0, len);
                    if (read == -1) {
                        break;
                    }
                    for (int ii = 0; ii < read; ii++) {
                        byte bb = buff[ii];
                        if (bb == 13) { // CR
                            continue;
                        }
                        if (bb == 10) { // LF
                            lineCutter.flip();
                            System.out.println(StandardCharsets.UTF_8.decode(lineCutter).toString());
                            lineCutter.clear();
                            continue;
                        }
                        lineCutter.put(bb);
                    }
                }
                lineCutter.flip();
                String ps1 = StandardCharsets.UTF_8.decode(lineCutter).toString();
                System.out.print(ps1);
                lineCutter.clear();
                if ("[root@localhost ~]# ".equals(ps1)) {
                    out.write(queue.poll().getBytes());
                    out.write(10); // LF
                    out.flush();
                    continue;
                }
                final Matcher matcher = PS1.matcher(ps1);
                if (matcher.matches()) {
                    out.write(queue.poll().getBytes());
                    out.write(10); // LF
                    out.flush();
                    continue;
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(80);
                } catch (InterruptedException ee) {
                }
            }
        } finally {
            channel.disconnect();
            session.disconnect();
            jsch = null;
        }
    }

}
