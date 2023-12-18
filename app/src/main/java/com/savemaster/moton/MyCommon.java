package com.savemaster.moton;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class MyCommon {


    public static AdFullScreenAd adFullScreenAd = new AdFullScreenAd();
    public static void loadFullScreen(Activity activity){
        String mopubId = AdUnit.ADUNIT_FULLSCREEN;

        adFullScreenAd.loadFullScreen(activity, mopubId);
    }

    public static boolean showFullScreen(Activity activity){
        boolean showed = false;
        if(adFullScreenAd != null){
            showed = adFullScreenAd.showAd();
        }
        return showed;
    }


//    public boolean showFullScreenAdOrReward(Activity activity){
//        if(showRewardAd()){
//            return true;
//        }
//        boolean showed = false;
//        if(adFullScreenAd != null){
//            showed = adFullScreenAd.showAd();
//            if(showed){
//                loadFullScreen(activity);
//            }
//        }
//        return showed;
//    }

    /////////////////////////////
//    public AdFullScreenAd adFullScreenAdAction = new AdFullScreenAd();
    public static void loadFullScreenAction(Activity activity){
//        String mopubId = AdUnit.ADUNIT_FULLSCREEN_ACTION;
//        adFullScreenAdAction.loadFullScreen(activity, mopubId);
        if(!adFullScreenAd.isReady()){
            loadFullScreen(activity);
        }
    }

    public static boolean showFullScreenAction(Activity activity){
//        boolean showed = false;
//        if(adFullScreenAdAction != null){
//            showed = adFullScreenAdAction.showAd();
//            if(showed){
//                loadFullScreenAction(activity);
//            }
//        }
        return showFullScreen(activity);
    }
    ///////////////////////////


    /////////////////////////////
//    public AdFullScreenAd adFullScreenAdDetail = new AdFullScreenAd();
//    public void loadFullScreenDetail(Activity activity){
//        String mopubId = AdUnit.ADUNIT_FULLSCREEN_DETAIL;
//
//        adFullScreenAdDetail.loadFullScreen(activity, mopubId);
//    }

    public static boolean showFullScreenDetail(Activity activity){
//        boolean showed = false;
//        if(adFullScreenAdDetail != null){
//            showed = adFullScreenAdDetail.showAd();
//            if(showed){
//                loadFullScreenDetail(activity);
//            }
//        }
        return showFullScreen(activity);
    }
    ///////////////////////////


//    AdAliRewardAd rewardedAd = new AdAliRewardAd();
//    public void loadReward(Activity activity){
//        String aduint = AdUnit.ADUNIT_Reward;
//        rewardedAd.loadRewarded(activity, aduint);
//    }
//
//    public boolean showRewardAd(){
//        if(rewardedAd != null && rewardedAd.isReady()){
//            rewardedAd.showAd();
//            return true;
//        }
//        return false;
//    }


    AdNativeAdCenter minNativeCenter = new AdNativeAdCenter();

    public void loadBigNative(Activity activity, FrameLayout containner){
        if(activity.isFinishing() || containner == null){
            return;
        }
//        boolean adRandom = new java.util.Random().nextBoolean();
        String mopubId;
//        if(adRandom){
        mopubId = AdUnit.ADUNIT_NATIVE;
//        } else {
//            mopubId = AdUnit.ADUNIT_NATIVE_02;
//        }

        minNativeCenter.setNativeLoadResult(new AdNativeAdCenter.NativeLoadResult() {
            @Override
            public void onResult(boolean success) {
                if(!success){
//                    loadBigBanner(activity, containner);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            containner.setVisibility(View.GONE);
                        }
                    });
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            containner.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
        minNativeCenter.loadNativeAds(activity, containner, mopubId, 0L, false);
    }

    public void loadMinNative(Activity activity, FrameLayout containner){
        if(activity.isFinishing()){
            return;
        }
        String mopubId = AdUnit.ADUNIT_MIN_NATIVE;

        minNativeCenter.setNativeLoadResult(new AdNativeAdCenter.NativeLoadResult() {
            @Override
            public void onResult(boolean success) {
                if(!success){
                    loadBanner(activity, containner);
                }
            }
        });
        minNativeCenter.loadNativeAds(activity, containner, mopubId, 0L, true);
    }

    AdBannerAdCenter adBigBannerAdCenter = new AdBannerAdCenter();
//    public void loadBigBanner(Activity activity, FrameLayout containner){
//        if(activity.isFinishing() || containner == null){
//            return;
//        }
//        String admobUnitId = "";
//
//        String mopubId = AdUnit.ADUNIT_banner_Big;
//
//        adBigBannerAdCenter.loadBannerAds(activity, containner, admobUnitId,
//                mopubId);
//    }


    AdBannerAdCenter adBannerAdCenter = new AdBannerAdCenter();
    public void loadBanner(Activity activity, FrameLayout containner){
//        if(activity.isFinishing() || containner == null){
//            return;
//        }
//        String admobUnitId = "";
//
//        String mopubId = AdUnit.ADUNIT_banner;
//
//        adBannerAdCenter.loadBannerAds(activity, containner, admobUnitId,
//                mopubId);
    }


    public static void googleRate(Activity activity){

        ReviewManager manager = ReviewManagerFactory.create(activity);
        com.google.android.play.core.tasks.Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                com.google.android.play.core.tasks.Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                flow.addOnCompleteListener(flowtask -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    if(flowtask != null){
                        if(flowtask.isSuccessful()){
                            Toast.makeText(activity, "Success", Toast.LENGTH_LONG).show();
                        } else {
                            Exception exception = flowtask.getException();
                            if(exception != null){
                                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            } else {
                // There was some problem, log or handle the error code.
                Exception exception =  task.getException();
                if(exception != null){
                    Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
