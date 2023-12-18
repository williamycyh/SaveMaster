package com.savemaster.savefromfb.ads;

import android.app.Activity;

import com.savemaster.moton.MyCommon;

//import com.google.android.gms.ads.AdError;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AppInterstitialAd {

    private static AppInterstitialAd mInstance;
    private Object mInterstitialAd;
    private AdClosedListener mAdClosedListener;
    private boolean isReloaded = false;

    public interface AdClosedListener {
        void onAdClosed();
    }

    public static AppInterstitialAd getInstance() {
        if (mInstance == null) {
            mInstance = new AppInterstitialAd();
        }
        return mInstance;
    }

    public void init(Activity activity) {
        // fetch show ads from remote config
        AdUtils.fetchShowAdsFromRemoteConfig(activity);

        // load interstitial ads
        loadInterstitialAd(activity);
    }

//    MyCommon myCommon = new MyCommon();
    private void loadInterstitialAd(Activity activity) {
        MyCommon.loadFullScreenAction(activity);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        InterstitialAd.load(activity, AdUtils.getInterstitialAdId(activity), adRequest, new InterstitialAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                // The mInterstitialAd reference will be null until an ad is loaded.
//                mInterstitialAd = interstitialAd;
//                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//
//                    @Override
//                    public void onAdDismissedFullScreenContent() {
//                        // Called when fullscreen content is dismissed.
//                        // Make sure to set your reference to null so you don't show it a second time.
//                        if (mAdClosedListener != null) {
//                            mAdClosedListener.onAdClosed();
//                        }
//                        // load a new interstitial
//                        loadInterstitialAd(activity);
//                    }
//
//                    @Override
//                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
//                        // Called when fullscreen content failed to show.
//                        // Make sure to set your reference to null so you don't show it a second time.
//                        if (!isReloaded) {
//                            isReloaded = true;
//                            loadInterstitialAd(activity);
//                        }
//                    }
//
//                    @Override
//                    public void onAdShowedFullScreenContent() {
//                        // Called when fullscreen content is shown.
//                    }
//                });
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                // Handle the error
//                mInterstitialAd = null;
//            }
//        });
    }

    public void showInterstitialAd(Activity activity, AdClosedListener mAdClosedListener) {
        mAdClosedListener.onAdClosed();
//        myCommon.adFullScreenAdAction.setFullScreenLoadResult(new AdFullScreenAd.FullScreenLoadResult() {
//            @Override
//            public void onAdLoaded() {
//            }
//
//            @Override
//            public void onAdDisplayed() {
//            }
//
//            @Override
//            public void onAdHidden() {
////                mAdClosedListener.onAdClosed();
//            }
//
//            @Override
//            public void onAdClicked() {
//            }
//
//            @Override
//            public void onAdLoadFailed() {
////                mAdClosedListener.onAdClosed();
//            }
//
//            @Override
//            public void onAdDisplayFailed() {
////                mAdClosedListener.onAdClosed();
//            }
//        });
        MyCommon.showFullScreenAction(activity);

        // fetch show ads from remote config
//        AdUtils.fetchShowAdsFromRemoteConfig(activity);
//
//        // check if can show the ads
//        if (AdUtils.isShowAds(activity) && AdUtils.isReadyToShowAds(activity)) {
//            if (mInterstitialAd != null) {
//                isReloaded = false;
//                this.mAdClosedListener = mAdClosedListener;
//                // show ads
//                mInterstitialAd.show(activity);
//            } else {
//                // reload a new ad for next time
//                loadInterstitialAd(activity);
//                // call onAdClosed
//                mAdClosedListener.onAdClosed();
//            }
//
//            // update time for next ads
//            AdUtils.updateTimeForNextAds(activity);
//        } else {
//            // reload a new ad for next time
//            loadInterstitialAd(activity);
//            // call onAdClosed
//            mAdClosedListener.onAdClosed();
//        }
    }
}