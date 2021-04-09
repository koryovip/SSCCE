package jp.co.syslinks.sscce.java.jna;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;

/**
 * @see https://angelpinpoint.seesaa.net/article/462885452.html
 * @see https://docs.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-setthreadexecutionstate
 */
public class SetThreadExecutionState {

    /**
     * ES_SYSTEM_REQUIRED   : 0x00000001
     * ES_DISPLAY_REQUIRED  : 0x00000002
     * ES_USER_PRESENT      : 0x00000004
     * ES_AWAYMODE_REQUIRED : 0x00000040
     * ES_CONTINUOUS        : 0x80000000
     */
    public static void main(String[] args) {
        // Television recording is beginning. Enable away mode and prevent
        // the sleep idle time-out.
        //
        Kernel32.INSTANCE.SetThreadExecutionState(WinBase.ES_CONTINUOUS | WinBase.ES_SYSTEM_REQUIRED | WinBase.ES_AWAYMODE_REQUIRED);

        //
        // Wait until recording is complete...
        //

        //
        // Clear EXECUTION_STATE flags to disable away mode and allow the system to idle to sleep normally.
        //
        Kernel32.INSTANCE.SetThreadExecutionState(WinBase.ES_CONTINUOUS);
    }

}
