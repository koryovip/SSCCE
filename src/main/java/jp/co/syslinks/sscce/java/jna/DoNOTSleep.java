package jp.co.syslinks.sscce.java.jna;

import java.util.concurrent.TimeUnit;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinUser.INPUT;

public class DoNOTSleep {

    // https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-mouse_event
    private static final int MOUSEEVENTF_MOVE = 0x0001;

    public static void main(String[] args) throws Exception {
        INPUT input = new INPUT();
        input.input.mi.dwFlags = new DWORD(MOUSEEVENTF_MOVE);
        while (true) {
            // SendInput(1, input, 28);
            User32.INSTANCE.SendInput(new DWORD(1), (INPUT[]) input.toArray(1), input.size());
            TimeUnit.SECONDS.sleep(2);
            System.out.println("1");
        }
    }

}
