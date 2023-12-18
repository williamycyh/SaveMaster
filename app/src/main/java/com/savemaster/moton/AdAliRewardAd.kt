package com.savemaster.moton

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.savemaster.savefromfb.App.appCon
import com.savemaster.savefromfb.BuildConfig

class AdAliRewardAd: AdCenter() {
    val TAG = "Rewarded"
    var activity: Activity? = null

    override fun initQueue() {
        if(BuildConfig.DEBUG){
            return
        }
        queue.offer(Ad_Type_Mopub)
    }
    var admobclicked = false

    var mopubId = ""
    var admobId = ""
    var fbId = ""

    fun loadRewarded(activity: Activity, mopubId: String){
        if(!isAdShow()){
            return
        }
        initQueue()
        this.mopubId = mopubId
        this.activity = activity
        loadByOrder()
    }

    fun loadByOrder(){
        var type = queue.poll()

        if(type == Ad_Type_Mopub && !TextUtils.isEmpty(mopubId)){
            Log.d(TAG, "loadByOrder Ad_Type_Mopub")
            loadMopubDelay()
        } else if(type == Ad_Type_IRONSOURCE){
            Log.d(TAG, "loadByOrder Ad_Type_IRONSOURCE")
//            loadIronAd()
        }
    }

//    fun showAd(): Boolean{
//        if(mInterstitial?.isReady == true){
//            mInterstitial?.showAd()
//            mInterstitial = null
//            return true
//        }
//        return false
//    }

    fun isReady(): Boolean{
        if (rewardedAd?.isReady() == true )
        {
            return true
        }
        return false
    }

    fun showAd(){
        if(appCon.v_version == BuildConfig.VERSION_CODE){
            return
        }
        if(rewardedAd != null ){
            rewardedAd?.showAd()
        }
    }

    ///////////////////////mopub///////////////////
    fun loadMopubDelay(){
        if(MoPubInited){
            loadMopub()
        } else {
            android.os.Handler().postDelayed({
                loadMopubDelay()
            }, 100)
        }
    }

    private var rewardedAd: MaxRewardedAd? = null
    private var retryAttempt = 0.0
    fun loadMopub(){
        rewardedAd = MaxRewardedAd.getInstance(mopubId, activity )
        rewardedAd?.setListener(listener)

        rewardedAd?.loadAd()
    }

    var listener: MaxRewardedAdListener = object : MaxRewardedAdListener {
        override fun onAdLoaded(ad: MaxAd) {
            retryAttempt = 0.0
            Log.d(TAG, "onAdLoaded")
        }

        override fun onAdDisplayed(ad: MaxAd) {
            Log.d(TAG, "onAdDisplayed")
        }

        override fun onAdHidden(ad: MaxAd) {
            Log.d(TAG, "onAdHidden")
            if(activity != null && activity?.isFinishing == false){
                loadMopubDelay();
            }
        }

        override fun onAdClicked(ad: MaxAd) {
        }

        override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
            Log.d(TAG, "onAdLoadFailed:" + error?.message)
        }

        override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
            Log.d(TAG, "onAdDisplayFailed")
        }

        override fun onRewardedVideoStarted(ad: MaxAd) {
        }

        override fun onRewardedVideoCompleted(ad: MaxAd) {
        }

        override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
            Log.d(TAG, "onUserRewarded")
            //start download
            if(activity != null && activity?.isFinishing == false){
//                Toast.makeText(activity, "Start download", Toast.LENGTH_SHORT).show()

            }
        }
    }

}