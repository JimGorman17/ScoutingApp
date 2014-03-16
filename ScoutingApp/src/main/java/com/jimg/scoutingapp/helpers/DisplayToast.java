package com.jimg.scoutingapp.helpers;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Jim on 2/9/14.
 */
public class DisplayToast implements Runnable {
    private final Context mContext;
    String mText;
    private int mToastDuration;

    public DisplayToast(Context mContext, String text, Integer toastDuration) {
        this.mContext = mContext;
        mText = text;
        mToastDuration = toastDuration;
    }

    @Override
    public void run() {
        mToastDuration = Toast.LENGTH_LONG;
        Toast.makeText(mContext, mText, mToastDuration).show();
    }
}
