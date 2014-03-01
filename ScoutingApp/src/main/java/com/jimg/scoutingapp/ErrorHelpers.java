package com.jimg.scoutingapp;

import android.os.Bundle;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Jim on 3/1/14.
 */
public class ErrorHelpers {
    public static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static void handleErrorMessage(MainActivity context, Bundle reply, String errorMessage, String displayMessage) {
        new DisplayToast(context, displayMessage, Toast.LENGTH_LONG).run();
        LogHelper.LogError(errorMessage, reply.getString(Constants.stackTraceExtra));
    }
}
