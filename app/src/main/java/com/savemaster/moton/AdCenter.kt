package com.savemaster.moton

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdk.SdkInitializationListener
import com.inmobi.sdk.InMobiSdk
import com.savemaster.savefromfb.BuildConfig
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.util.*


abstract class AdCenter {
    var queue: Queue<Int> = LinkedList<Int>()
    companion object{
        val TAG = "AdCenter"

        val isShowAds = true

        fun isAdShow(): Boolean{
//            if (MyApp.getInstance().packageName.contains("pro", true)){
//                return false
//            }
            if(BuildConfig.DEBUG){
                return false
            }

            return true
        }


        val Ad_Type_Admob = 1
        val Ad_Type_Facebook = 2
        val Ad_Type_Mopub = 3
        val Ad_Type_IRONSOURCE = 4
        val Ad_Type_Inmobi = 5

        var MoPubInited = false
        var InmobiInited = false;

        fun initAds(context: Context){
            if(!isAdShow()){
                return
            }
//            AudienceNetworkAds.initialize(context)
        }



        fun initMobpuAd(activity: Activity){
            if(!isAdShow()){
                return
            }


////            var ironSourceSettings: HashMap<String, String> = HashMap()
////            ironSourceSettings.put("​applicationKey​", AdUnit.IRONSRC_APP_KEY)
//
//            var inMobiSettings: HashMap<String, String> = HashMap()
//            inMobiSettings.put("accountid", BaseCommon.decodeToString("YmE5YjFhMjRhMjVhNDQxMThjZGRkOWQwNzQ3ZmNhM2U=")) //"ba9b1a24a25a44118cddd9d0747fca3e"
//
//            //Mintegral
////            // Declare your Mintegral app ID and app key
////            val mintegralConfigs: MutableMap<String, String> = HashMap()
////            mintegralConfigs["appId"] =  AdUnit.MINTEGRAL_APP_ID
////            mintegralConfigs["appKey"] = BaseCommon.decodeToString("MGY3OWI3NmQxY2E0NDFlNzk5YmRhYmI3NjUzZDQ1ODA=")//"0f79b76d1ca441e799bdabb7653d4580"
//
//
//            val sdkConfiguration = SdkConfiguration.Builder(AdUnit.ADUNIT_banner)
////                    .withMediatedNetworkConfiguration(IronSourceAdapterConfiguration::class.java.name, ironSourceSettings)
//                    .withMediatedNetworkConfiguration(InMobiAdapterConfiguration::class.java.name, inMobiSettings)
////                    .withMediatedNetworkConfiguration(MintegralAdapterConfiguration::class.java.name, mintegralConfigs)
////                    .withLogLevel(MoPubLog.LogLevel.DEBUG)
//                    .build()
//
//            MoPub.initializeSdk(activity, sdkConfiguration, object : SdkInitializationListener {
//                override fun onInitializationFinished() {
//                    MoPubInited = true
//                }
//
//            })

            AppLovinSdk.getInstance(activity).mediationProvider = "max"
            AppLovinSdk.initializeSdk(activity, SdkInitializationListener {
                // AppLovin SDK is initialized, start loading ads
                MoPubInited = true
                android.util.Log.d("AdCenter", "MoPubInited")
//                AppLovinSdk.getInstance(activity).showMediationDebugger()
            })
        }

        fun initInmobi(activity: Activity){
            if(!isAdShow()){
                return
            }
            // OPTIONAL: Prepare InMobi GDPR Consent
            var consentObject = JSONObject()
            try {
                consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);

                // If you obtain your consent outside and before MoPub initialization sequence, and have it available at this time, you can include it here.
                consentObject.put("gdpr", "0"); // "0" or "1"
//                consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB, “ << consent in IAB format >> ”);
            } catch (e: Exception) {
                e.printStackTrace();
            }

            InMobiSdk.init(activity,
                Utils.MydecodeToString("aXh5X2JhOWIxYTI0YTI1YTQ0MTE4Y2RkZDlkMDc0N2ZjYTNl"), consentObject, object : com.inmobi.sdk.SdkInitializationListener {

                override fun onInitializationComplete(p0: java.lang.Error?) {
                    if (null != p0) {
                        Log.e(TAG, "InMobi Init failed -" + p0.message)
                    } else {
                        Log.d(TAG, "InMobi Init Successful")
                        InmobiInited = true
                    }
                    initMobpuAd(activity)
                }
            })
        }

        open fun decodeToString(str: String): String {
            try {
                return String(Base64.decode(str.toByteArray(charset("UTF-8")), Base64.DEFAULT))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return ""
        }
    }

    abstract fun initQueue()


}