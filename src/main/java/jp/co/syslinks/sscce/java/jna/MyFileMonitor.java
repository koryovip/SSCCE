package jp.co.syslinks.sscce.java.jna;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.sun.jna.platform.FileMonitor;
import com.sun.jna.platform.FileMonitor.FileEvent;
import com.sun.jna.platform.FileMonitor.FileListener;
import com.sun.jna.platform.win32.W32FileMonitor;

/**
 * @see https://www.programmersought.com/article/7043341129/
 */
public class MyFileMonitor {

    public static void main(String[] args) throws IOException {
        final FileListener listener = new FileListener() {
            @Override
            public void fileChanged(FileEvent e) {
                System.out.println(e.getType());
                System.out.println(e.getFile().getAbsolutePath());
            }
        };
        final File dir = new File("R:/TEMP");
        FileMonitor fileMon = W32FileMonitor.getInstance();

        { // clear
            fileMon.removeFileListener(listener);
            fileMon.removeWatch(dir);
        }
        {
            fileMon.addFileListener(listener);
            fileMon.addWatch(dir, FileMonitor.FILE_CREATED | FileMonitor.FILE_MODIFIED, false);
        }
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }

        { // clear before exit
            fileMon.removeFileListener(listener);
            fileMon.removeWatch(dir);
        }
    }

}
