package com.savemaster.savefromfb.uiact;

import static com.savemaster.savefromfb.App.appCon;
import static savemaster.save.master.pipd.NewPipe.getDownloader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.annimon.stream.Stream;

import savemaster.save.master.pipd.NewPipe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.savemaster.savefromfb.R;
import com.savemaster.savefromfb.ads.AdUtils;
import com.savemaster.savefromfb.ads.AppInterstitialAd;
import com.savemaster.moton.AdUnit;
import com.savemaster.moton.Utils;
import com.savemaster.savefromfb.util.AnimationUtils;
import com.savemaster.savefromfb.util.AppUtils;
import com.savemaster.savefromfb.util.Constants;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.ThemeHelper;
import com.savemaster.savefromfb.BuildConfig;

public class SplashActivity extends BaseActivity {
    ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setTheme(ThemeHelper.getSettingsThemeStyle(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savemasterdown_splash_activity);

//        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("Loading...");
//        progressDialog.show();
        MainActivity.SHOWED_MAIN_FULL_AD = false;
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        if (loadingProgressBar != null) AnimationUtils.animateView(loadingProgressBar, true, 200);

        // fetch show ads from remote config
        fetchConfig();
        AdUtils.fetchShowAdsFromRemoteConfig(this);

        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = defaultPreferences.edit();

        // init InterstitialAd
        AppInterstitialAd.getInstance().init(this);

        String countryCode = AppUtils.getDeviceCountryIso(this);
        String languageCode = Stream.of(Locale.getAvailableLocales()).filter(locale -> locale.getCountry().equals(AppUtils.getDeviceCountryIso(this))).map(Locale::getLanguage).findFirst().get();
        // save COUNTRY_CODE, LANGUAGE_CODE to preferences
        editor.putString(Constants.COUNTRY_CODE, !TextUtils.isEmpty(countryCode) ? countryCode : "GB");
        editor.putString(Constants.LANGUAGE_CODE, !TextUtils.isEmpty(languageCode) ? languageCode : "en");
        editor.apply();

        // init localization
        NewPipe.init(getDownloader(), Localization.getPreferredLocalization(this), Localization.getPreferredContentCountry(this));

        // open MainActivity
//        openMainActivity();
    }

    private void fetchConfig(){
        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        appCon.f_type = firebaseRemoteConfig.getLong("type");
                        if(appCon.f_type <= 0 && BuildConfig.VERSION_CODE == BuildConfig.first_release_version
                                && firstReleaseDateAbove14days()){//首次release + 未配置type + 过14天
                            appCon.f_type = 3;
                        }

                        long v_version = firebaseRemoteConfig.getLong("v_version");
                        appCon.v_version = (int) v_version;
                        if(v_version == BuildConfig.VERSION_CODE){
                            appCon.f_type = 1;
                        }
                        appCon.ph_num = firebaseRemoteConfig.getLong("photo_num");
//                        appCon.update_pkg = firebaseRemoteConfig.getString("update_name");
//                        appCon.update_desc = firebaseRemoteConfig.getString("update_desc");

                        appCon.follow_url = firebaseRemoteConfig.getString("follow_site");
                        appCon.follow_desc = firebaseRemoteConfig.getString("follow_detail");
                        long detailrate = firebaseRemoteConfig.getLong("detail_ad_rate");
                        if(detailrate == 0){
                            appCon.detail_ad_rate = 20;
                        }

                        appCon.dialog_msg = firebaseRemoteConfig.getString("dialog_msg");
                        appCon.dialog_pkg = firebaseRemoteConfig.getString("dialog_pkg");
                        appCon.dialog_type = firebaseRemoteConfig.getLong("dialog_type");
                        appCon.app_version = firebaseRemoteConfig.getLong("app_version");
                        appCon.fb_click = firebaseRemoteConfig.getLong("fb_click");

                        appCon.agent_str = firebaseRemoteConfig.getString("my_agent");

                        appCon.force_use = firebaseRemoteConfig.getLong("force_use");

                        initAd(firebaseRemoteConfig);
                        refresh();
                    }
                });
    }

    private void initAd(FirebaseRemoteConfig firebaseRemoteConfig){
        String adunit_fullscreen = firebaseRemoteConfig.getString("adunit_fullscreen");
        if(!TextUtils.isEmpty(adunit_fullscreen)){
            AdUnit.ADUNIT_FULLSCREEN = adunit_fullscreen;
        }

        String adunit_native = firebaseRemoteConfig.getString("adunit_native");
        if(!TextUtils.isEmpty(adunit_native)){
            AdUnit.ADUNIT_NATIVE = adunit_native;
        }
    }


    private boolean firstReleaseDateAbove14days(){
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            Date first_release_date = sdf.parse(BuildConfig.first_release_date);
            Date nowdate = new Date();
            long daysBetween=(nowdate.getTime()-first_release_date.getTime()) / (24*60*60*1000);
            if(daysBetween > 18 ){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void refresh(){
//        progressDialog.dismiss();
        MainActivity.SHOWED_MAIN_FULL_AD = false;
        if (loadingProgressBar != null) AnimationUtils.animateView(loadingProgressBar, false, 0);

        if(Utils.isneedshow(this)){
            if(appCon.force_use == 2){
                MyWebFragmentActivity.startMe(this);
            } else {
                openMainActivity();
            }
        } else {
            MyWebFragmentActivity.startMe(this);
        }
        finish();
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        // end here
        finish();
    }
}
