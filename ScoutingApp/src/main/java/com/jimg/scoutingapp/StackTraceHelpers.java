package com.jimg.scoutingapp;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Jim on 3/1/14.
 */
public class StackTraceHelpers {
    public static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
