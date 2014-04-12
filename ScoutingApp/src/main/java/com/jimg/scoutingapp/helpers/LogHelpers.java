package com.jimg.scoutingapp.helpers;

import android.util.Log;

import com.google.gson.JsonObject;
import com.jimg.scoutingapp.Constants;
import com.jimg.scoutingapp.MainActivity;
import com.jimg.scoutingapp.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

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
            JsonObject json = new JsonObject();
            json.addProperty(Constants.applicationExtra, mainActivity.getString(R.string.app_name));
            json.addProperty(Constants.phoneIdExtra, DeviceIdentifierHelpers.GetUniqueId(mainActivity));
            json.addProperty(Constants.errorMessageExtra, errorMessage);
            json.addProperty(Constants.stackTraceExtra, stackTrace);
            json.addProperty(Constants.authTokenExtra, mainActivity.mAuthToken);

            Ion.with(mainActivity, Constants.restServiceUrlBase + "ErrorLog/Create?" + Constants.getJson)
                    .setJsonObjectBody(json)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                LogHelpers.LogError(e.getMessage(), ErrorHelpers.getStackTraceAsString(e), null);
                            }
                        }
                    });
        }
    }
}
