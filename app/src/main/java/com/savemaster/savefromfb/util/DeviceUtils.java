package com.savemaster.savefromfb.util;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

public class DeviceUtils {

    public static boolean isTablet(@NonNull final Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isLandscape(@NonNull final Context context) {
        return context.getResources().getDisplayMetrics().heightPixels < context.getResources().getDisplayMetrics().widthPixels;
    }
}
