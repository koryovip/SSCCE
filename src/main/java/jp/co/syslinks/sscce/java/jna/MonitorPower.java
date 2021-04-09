package jp.co.syslinks.sscce.java.jna;

import java.util.concurrent.TimeUnit;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;

/**
 * @see https://angelpinpoint.seesaa.net/article/462885452.html
 * @see https://docs.microsoft.com/en-us/windows/win32/menurc/wm-syscommand
 * NOT work at mulit-monitor
 */
public class MonitorPower {

    public static void main(String[] args) throws InterruptedException {
        final int WM_SYSCOMMAND = 0x0112;
        final int SC_MONITORPOWER = 0xF170;
        final WPARAM wParam = new WPARAM(SC_MONITORPOWER);
        /**
         * -1 (the display is powering on)
         * 1  (the display is going to low power)
         * 2  (the display is being shut off)
         */
        {
            final LPARAM lParam = new LPARAM(2);
            // An application should return zero if it processes this message.
            LRESULT result = User32.INSTANCE.SendMessage(WinUser.HWND_BROADCAST, WM_SYSCOMMAND, wParam, lParam);
            System.out.println(result.intValue());
        }
        TimeUnit.SECONDS.sleep(5);
        {
            final LPARAM lParam = new LPARAM(-1);
            // An application should return zero if it processes this message.
            LRESULT result = User32.INSTANCE.SendMessage(WinUser.HWND_BROADCAST, WM_SYSCOMMAND, wParam, lParam);
            System.out.println(result.intValue());
        }
    }

}
