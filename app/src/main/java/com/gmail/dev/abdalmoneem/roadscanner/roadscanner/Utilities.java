package com.gmail.dev.abdalmoneem.roadscanner.roadscanner;

import android.*;
import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

/**
 * Created by Abd on 12/14/2017.
 */

public class Utilities {
    public static String getDeviceId(Context context) {
        String deviceId ="";
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED )
            deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (deviceId != null) {
            return deviceId;
        } else {
            return android.os.Build.SERIAL;
        }
    }
}
