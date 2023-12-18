package com.savemaster.savefromfb.ads;

import android.app.Activity;
import android.content.Context;

import com.annimon.stream.function.Consumer;

import java.util.Date;

import com.savemaster.savefromfb.util.SharedPrefsHelper;

public class AdUtils {

    public static boolean isReadyToShowAds(Context context) {
        // Last time show interstitial ads
        long lastTimeShowAds = SharedPrefsHelper.getLongPrefs(context, SharedPrefsHelper.Key.INTERSTITIAL_CAP_TIME.name());
        Date lastTimeDate = new Date(lastTimeShowAds);
        return new Date().getTime() - lastTimeDate.getTime() >= 10 * 60 * 1000; // 10 minutes
    }

    public static void updateTimeForNextAds(Context context) {
        SharedPrefsHelper.setLongPrefs(context, SharedPrefsHelper.Key.INTERSTITIAL_CAP_TIME.name(), new Date().getTime());
    }

    public static void fetchShowAdsFromRemoteConfig(Activity activity) {
        /*// create FirebaseRemoteConfig
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        // init firebase remote config
        remoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build());
        // fetch data from FirebaseRemoteConfig
        remoteConfig.fetchAndActivate().addOnSuccessListener(activity, success -> {
            boolean showAd = remoteConfig.getBoolean("show_ads");
            SharedPrefsHelper.setBooleanPrefs(activity, SharedPrefsHelper.Key.DISPLAY_ADS.name(), showAd);
        });*/

        SharedPrefsHelper.setBooleanPrefs(activity, SharedPrefsHelper.Key.DISPLAY_ADS.name(), true);
    }

    public static void displayAds(Context context, Consumer<Boolean> callback) {
        boolean showAd = SharedPrefsHelper.getBooleanPrefs(context, SharedPrefsHelper.Key.DISPLAY_ADS.name());
        callback.accept(showAd);
    }

    public static boolean isShowAds(Context context) {
        return SharedPrefsHelper.getBooleanPrefs(context, SharedPrefsHelper.Key.DISPLAY_ADS.name());
    }

    public static String getNativeAdId(Context context) {
//        String[] array = {context.getString(R.string.native_1), context.getString(R.string.native_2), context.getString(R.string.native_3)};
//        return array[new Random().nextInt(array.length)];
        return "";
    }

//    public static String getInterstitialAdId(Context context) {
//        String[] array = {context.getString(R.string.interstitial_1), context.getString(R.string.interstitial_2), context.getString(R.string.interstitial_3)};
//        return array[new Random().nextInt(array.length)];
//    }
}
