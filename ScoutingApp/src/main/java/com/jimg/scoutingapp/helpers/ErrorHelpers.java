package com.jimg.scoutingapp.helpers;

import android.widget.Toast;

import com.jimg.scoutingapp.MainActivity;

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

    public static void handleError(MainActivity context, String displayMessage, String errorMessage, String stackTrace) {
        new DisplayToast(context, displayMessage, Toast.LENGTH_LONG).run();
        LogHelpers.LogError(errorMessage, stackTrace);
    }
}
