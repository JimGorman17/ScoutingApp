package com.jimg.scoutingapp.helpers;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.UUID;

/**
 * Created by Jim on 4/5/2014.
 */
public class DeviceIdentifierHelpers {
    public static String GetUniqueId(Activity activity) {
        // http://stackoverflow.com/a/2853253/109941

        String deviceId = "";

        try {
            final TelephonyManager tm = (TelephonyManager) activity.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

            final String tmDevice, tmSerial, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            deviceId = deviceUuid.toString();
        }
        catch (Exception ex) {
        }

        return deviceId;
    }
}
