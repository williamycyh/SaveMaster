package com.savemaster.moton

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils
import com.savemaster.savefromfb.BuildConfig


open class AdBigBannerAdCenter: AdCenter() {
    val TAG = "BigBannerAdCenter"

    var admobId = ""
    var mopubId = ""

    var mIsBig = false

    var container:FrameLayout? = null
    var context: Context? = null
    var activity: Activity? = null

    init {
    }

    interface BannerAdInterface{
        fun onNativeAdLoaded(adType: Int)
        fun onNativeLoadFail(adType: Int, errorCode: Int)
    }

//    var adInterface: BannerAdInterface? = null

    //是否限制展示
    fun loadBannerAds(activity: Activity,
                      container: FrameLayout, admobId: String, mopubId: String){
        if(!isAdShow()){
            return
        }
        this.activity = activity
        this.container = container

        this.admobId = admobId
        this.mopubId = mopubId
//        this.mIsBig = isBig
        initQueue()

        loadByOrder(activity)

    }

    override fun initQueue() {
        if(BuildConfig.DEBUG){
            return
        }
        queue.offer(Ad_Type_Mopub)
    }


    fun loadByOrder(context: Activity){
        var type = queue.poll()

        if (type == Ad_Type_Admob){
            Log.d(TAG, "loadByOrder Ad_Type_Admob")
//            loadAdmobBanner(context, admobId)
        } else if(type == Ad_Type_Facebook){
            Log.d(TAG, "loadByOrder Ad_Type_Facebook")
//            loadFbBanner(context, fbId)
        } else if(type == Ad_Type_Mopub){
            Log.d(TAG, "loadByOrder Ad_Type_Mopub")
            loadMopubDelay(context, mopubId)
        } else if(type == Ad_Type_IRONSOURCE){
            Log.d(TAG, "loadByOrder Ad_Type_IRONSOURCE")
//            loadIronAd()
        }
    }


//    fun loadAdmobBanner(context: Context, admobId: String){
//
//        val adView = com.google.android.gms.ads.AdView(context)
//        adView.adSize = com.google.android.gms.ads.AdSize.BANNER
//        adView.adUnitId = admobId
//        adView.adListener = object : com.google.android.gms.ads.AdListener(){
//
//            override fun onAdLoaded() {
//                super.onAdLoaded()
//                container?.removeAllViews()
//                container?.addView(adView)
//                container?.visibility = View.VISIBLE
//
//                Log.d(TAG, "banner onAdLoaded")
//            }
//
//            override fun onAdFailedToLoad(error: Int) {
//                super.onAdFailedToLoad(error)
//                Log.d(TAG, "banner onAdFailedToLoad")
//                loadByOrder(context)
//            }
//        }
//
//        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
//        adView.loadAd(adRequest)
//    }



    fun loadMopubDelay(context: Activity, mopubId: String){
        if(MoPubInited){
            loadMopub(context, mopubId)
        } else {
            android.os.Handler().postDelayed({
                loadMopubDelay(context, mopubId)
            },100)
        }
    }

    var listener: MaxAdViewAdListener = object : MaxAdViewAdListener{
        override fun onAdLoaded(ad: MaxAd) {
            if(adView?.parent != null) {
                (adView?.parent as ViewGroup).removeView(adView) // <- fix
            }
            container?.removeAllViews()

            val widthPx = AppLovinSdkUtils.dpToPx(activity, 300)
            val heightPx = AppLovinSdkUtils.dpToPx(activity, 250)

            adView!!.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
//            adView!!.setBackgroundColor(R.color.background_color)

            container?.addView(adView)
            container?.visibility = View.VISIBLE

            // Load the ad
            adView!!.startAutoRefresh()

            Log.d(TAG, "onAdLoaded")
        }

        override fun onAdDisplayed(ad: MaxAd) {
            Log.d(TAG, "onAdDisplayed")
        }

        override fun onAdHidden(ad: MaxAd) {
        }

        override fun onAdClicked(ad: MaxAd) {
        }

        override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
            loadByOrder(activity!!)
            Log.d(TAG, "onAdLoadFailed")
        }

        override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
        }

        override fun onAdExpanded(ad: MaxAd) {
        }

        override fun onAdCollapsed(ad: MaxAd) {
        }

    }

    var adView: MaxAdView? = null
    fun loadMopub(activity: Activity, mopubId: String){

        adView = MaxAdView(mopubId, MaxAdFormat.MREC, activity)
        adView!!.setListener(listener)

        // Stretch to the width of the screen for banners to be fully functional
//        val width = ViewGroup.LayoutParams.MATCH_PARENT
//
//        // Banner height on phones and tablets is 50 and 90, respectively
//        val heightPx = activity.resources.getDimensionPixelSize(R.dimen.banner_height)
//
//        adView!!.layoutParams = FrameLayout.LayoutParams(width, heightPx)

        // Set background or background color for banners to be fully functional
//        adView!!.setBackgroundColor(R.color.background_color)

//        val rootView = findViewById<ViewGroup>(android.R.id.content)
//        container?.addView(adView)
//        container?.visibility = View.VISIBLE
        // Load the ad
        adView!!.loadAd()

    }

    //        var moPubView = MoPubView(activity)//rootView.findViewById(R.id.moupuView);
//        moPubView.setAdUnitId(mopubId)
//        moPubView.loadAd()//MoPubView.MoPubAdSize.HEIGHT_50
//        moPubView.bannerAdListener = object : MoPubView.BannerAdListener {
//            override fun onBannerLoaded(banner: MoPubView) {
//                if (banner.parent != null) {
//                    (banner.parent as ViewGroup).removeView(banner) // <- fix
//                }
//                container?.removeAllViews()
//                container?.addView(banner)
//                container?.visibility = View.VISIBLE
//
//            }
//
//            override fun onBannerFailed(banner: MoPubView, errorCode: MoPubErrorCode) {
//                loadByOrder(activity)
//            }
//
//            override fun onBannerClicked(banner: MoPubView) {
//
//            }
//
//            override fun onBannerExpanded(banner: MoPubView) {
//
//            }
//
//            override fun onBannerCollapsed(banner: MoPubView) {
//
//            }
//        }


    private fun toPixelUnits(dipUnit: Int, context: Context): Int {
        val density = context.getResources().getDisplayMetrics().density
        return Math.round(dipUnit * density)
    }




    //iron
//    var placeName = "DefaultBanner"
//    fun setOronBannerPlaceName(name: String){
//        placeName = name
//    }
//    fun loadIronAd(){
//        IronSource.init(activity, "d819cd89", IronSource.AD_UNIT.BANNER)
//
//        var banner: IronSourceBannerLayout? = null
//        if(mIsBig){
//            banner = IronSource.createBanner(activity, ISBannerSize.RECTANGLE)
//        } else {
//            banner = IronSource.createBanner(activity, ISBannerSize.SMART)
//        }
//
//        Log.d(TAG, "loadIronAd")
//        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT)
//        container?.visibility = View.VISIBLE
//        container?.addView(banner,layoutParams)
//
//        banner.bannerListener = object : BannerListener {
//            override fun onBannerAdLoaded() {
//                // Called after a banner ad has been successfully loaded
//                activity?.runOnUiThread(Runnable { container?.visibility = View.VISIBLE })
//
//                Log.d(TAG, "loadIronAd onBannerAdLoaded")
//            }
//
//            override fun onBannerAdLoadFailed(error: IronSourceError) {
//                // Called after a banner has attempted to load an ad but failed.
//                Log.d(TAG, "loadIronAd onBannerAdLoadFailed")
//                activity?.runOnUiThread(Runnable {
//                    container?.visibility = View.GONE
//                    container?.removeAllViews() })
//                loadByOrder(activity!!)
//            }
//
//            override fun onBannerAdClicked() {
//                // Called after a banner has been clicked.
//            }
//
//            override fun onBannerAdScreenPresented() {
//                // Called when a banner is about to present a full screen content.
//            }
//
//            override fun onBannerAdScreenDismissed() {
//                // Called after a full screen content has been dismissed
//            }
//
//            override fun onBannerAdLeftApplication() {
//                // Called when a user would be taken out of the application context.
//            }
//        }
//
//        IronSource.loadBanner(banner, placeName)
//    }

}