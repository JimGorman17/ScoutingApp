package com.jimg.scoutingapp.helpers;

import android.util.Log;

import com.jimg.scoutingapp.R;

/**
 * Created by Jim on 2/9/14.
 */
public class LogHelpers {
    public static void ProcessAndThreadId(String label) {
        String logMessage = String.format("%s, Process ID:%d, Thread ID:%d", label, android.os.Process.myTid(), Thread.currentThread().getId());
        Log.i(String.valueOf(R.string.app_name), logMessage);
    }

    public static void LogError(String errorMessage, String stackTrace) {
        String logMessage = String.format("%s, Process ID:%d, Thread ID:%d\n%s", errorMessage, android.os.Process.myTid(), Thread.currentThread().getId(), stackTrace);
        Log.e(String.valueOf(R.string.app_name), logMessage); // TODO: Post this to the server.
    }
}
