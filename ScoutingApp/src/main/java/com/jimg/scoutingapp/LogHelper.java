package com.jimg.scoutingapp;

import android.util.Log;

/**
 * Created by Jim on 2/9/14.
 */
public class LogHelper {
    public static void ProcessAndThreadId(String label) {
        String logMessage = String.format("%s, Process ID:%d, Thread ID:%d", label, android.os.Process.myTid(), Thread.currentThread().getId());
        Log.i(String.valueOf(R.string.app_name), logMessage);
    }

    public static void LogError(String errorMessage, String stackTrace) {
        String logMessage = String.format("%s, Process ID:%d, Thread ID:%d\n%s", errorMessage, android.os.Process.myTid(), Thread.currentThread().getId(), stackTrace);
        Log.e(String.valueOf(R.string.app_name), logMessage); // TODO: Post this to the server.
    }
}
