package jp.co.syslinks.sscce.java.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class InputStreamThread extends Thread {

    public enum OutType {
        std, err;
    }

    private final OutType type;
    private BufferedReader br;
    private List<String> list = new ArrayList<String>();

    public InputStreamThread(InputStream is, OutType type) {
        br = new BufferedReader(new InputStreamReader(is));
        this.type = type;
    }

    public InputStreamThread(InputStream is, OutType type, Charset charset) {
        br = new BufferedReader(new InputStreamReader(is, charset));
        this.type = type;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                String line = br.readLine();
                if (line == null)
                    break;
                // list.add(line);
                if (type == OutType.std) {
                    System.out.println(line);
                } else {
                    System.err.println(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getStringList() {
        return list;
    }

}
