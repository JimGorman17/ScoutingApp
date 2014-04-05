package com.jimg.scoutingapp.helpers;

import android.content.Intent;
import android.util.Log;

import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.jimg.scoutingapp.intentservices.PostErrorLogIntentService;

/**
 * Created by Jim on 2/9/14.
 */
public class LogHelpers {
    public static void ProcessAndThreadId(String label) {
        String logMessage = String.format("%s, Process ID:%d, Thread ID:%d", label, android.os.Process.myTid(), Thread.currentThread().getId());
        Log.i(String.valueOf(R.string.app_name), logMessage);
    }

    public static void LogError(String errorMessage, String stackTrace, MainActivity mainActivity) {
        LogHelpers.ProcessAndThreadId("LogHelpers.LogError");

        String logMessage = String.format("%s, Process ID:%d, Thread ID:%d\n%s", errorMessage, android.os.Process.myTid(), Thread.currentThread().getId(), stackTrace);
        Log.e(String.valueOf(R.string.app_name), logMessage);

        if (mainActivity != null) {
            Intent serviceIntent = new Intent(mainActivity, PostErrorLogIntentService.class);

            serviceIntent.putExtra(Constants.applicationExtra, String.valueOf(R.string.app_name));
            serviceIntent.putExtra(Constants.phoneIdExtra, DeviceIdentifierHelpers.GetUniqueId(mainActivity));
            serviceIntent.putExtra(Constants.errorMessageExtra, errorMessage);
            serviceIntent.putExtra(Constants.stackTraceExtra, stackTrace);
            serviceIntent.putExtra(Constants.authTokenExtra, mainActivity.mAuthToken);

            mainActivity.startService(serviceIntent);
        }
    }
}
