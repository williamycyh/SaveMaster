package com.savemaster.savefromfb.uiact;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.savemaster.smlib.ASharePreferenceUtils;
import com.savemaster.smlib.CommonVideoDownloader;
import com.savemaster.smlib.Downloader;
import com.savemaster.smlib.FileActivity;
import com.savemaster.smlib.FileItem;
import com.savemaster.smlib.MainLib;
import com.savemaster.smlib.MyTFloatTActivity;
import com.savemaster.savefromfb.R;
import com.savemaster.moton.AdCenter;
import com.savemaster.moton.AdFullScreenAd;
import com.savemaster.moton.MyCommon;
import com.savemaster.moton.Utils;
import com.savemaster.savefromfb.util.ThemeHelper;

import java.util.List;

import static com.savemaster.savefromfb.uiact.MainActivity.SHOWED_MAIN_FULL_AD;

public class MyWebFragmentActivity extends BaseActivity {
//    View support1;
//    View support2;

    public static void startMe(Activity activity){
        Intent intent = new Intent();
        intent.setClass(activity, MyWebFragmentActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeHelper.getSettingsThemeStyle(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savemasterdown_puremywebfragment);
        initView();
        loadAd();
        setDownloadListener();
        initLib();
    }

    void setDownloadListener(){
//        AIOSSUtils.onVideoDownloadInterface = new AIOSSUtils.OnVideoDownloadInterface() {
//            @Override
//            public void onVideoDownload() {
//                if(isFinishing()){
//                    return;
//                }
//                myCommon.showFullScreenAdOrReward(MainActivity.this);
//            }
//        };
        Downloader.getInstance(this).setOnStartDownloadListener(new Downloader.OnStartDownloadListener() {
            @Override
            public void onStart() {
                if(isFinishing()){
                    return;
                }
                if(Utils.canShowFullAd(MyWebFragmentActivity.this)){
                    MyCommon.showFullScreen(MyWebFragmentActivity.this);
                }
            }
        });
    }

    public String getClipboard(){
        try{
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData data = cm.getPrimaryClip();
            ClipData.Item item = data.getItemAt(0);
            return item.getText().toString();
        }catch (Exception e){

        }
        return "";
    }

    private void initLib(){
        MainLib.setOnBaseDownloadCall(new MainLib.OnBaseDownloadCall() {
            @Override
            public void showFullAd() {

                boolean rated = ASharePreferenceUtils.getBoolean(MyWebFragmentActivity.this, "rashowed", false);
                if(rated){
                    if(Utils.canShowFullAd(MyWebFragmentActivity.this)){
                        MyCommon.showFullScreen(MyWebFragmentActivity.this);
                    }
                }
                /*else {
                    showFirstDownload();
                }*/
            }

            @Override
            public void showVideoList(List<FileItem> files, Activity activity) {
                Utils.showListDialog(files, activity);
            }
        });
    }

//    MyCommon myCommon = new MyCommon();
    private void loadAd(){
        AdCenter.Companion.initInmobi(this);

        if(Utils.canShowFullAd(this)){
            MyCommon.adFullScreenAd.setFullScreenLoadResult(new AdFullScreenAd.FullScreenLoadResult() {
                @Override
                public void onAdLoaded() {
                    if(!SHOWED_MAIN_FULL_AD){
                        MyCommon.adFullScreenAd.showAd();
                    }
                    SHOWED_MAIN_FULL_AD = true;
                }

                @Override
                public void onAdDisplayed() {
                }

                @Override
                public void onAdHidden() {
                    MyCommon.loadFullScreen(MyWebFragmentActivity.this);
                }

                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdLoadFailed() {
                }

                @Override
                public void onAdDisplayFailed() {
                }
            });
            MyCommon.loadFullScreen(this);
        }

    }

    EditText eText;
    private void initView(){

        eText = findViewById(R.id.edit_input_url);

        findViewById(R.id.paste).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tt = getClipboard();
                if(!TextUtils.isEmpty(tt)){
                    eText.setText(tt);
                }
            }
        });

        findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(eText.getText())){
                    return;
                }
                String urlorkey = eText.getText().toString();
                if(!Utils.isneedshow(MyWebFragmentActivity.this)){
                    if(isYoutubeUrl(urlorkey)){
                        Toast.makeText(MyWebFragmentActivity.this, "Not supported", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if(urlorkey.startsWith("https") || urlorkey.startsWith("http") ){
//                    MyTFloatTActivity.startMeUrlNoDown(MyWebFragmentActivity.this, urlorkey, 1);
                   Utils.downloadNoTube(MyWebFragmentActivity.this, urlorkey);
                } else {
                    MyTFloatTActivity.startMeUrlNoDown(MyWebFragmentActivity.this, "https://m.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd="+urlorkey, 1);
                }
//                myCommon.showFullScreen(MyWebFragmentActivity.this);
            }
        });


        findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileActivity.startMe(MyWebFragmentActivity.this);
            }
        });
        findViewById(R.id.feedback_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mailMeToSupport(MyWebFragmentActivity.this);
            }
        });
    }

    public static void mailMeToSupport(Activity activity){
        if(activity == null){
            return;
        }

        Intent i = new Intent(Intent.ACTION_SENDTO);
//        i.setType("message/rfc822");
        i.setData(Uri.parse("mailto:"));
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"williamlnjxty@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "[Support]");

        StringBuilder feedStr = new StringBuilder();

        i.putExtra(Intent.EXTRA_TEXT, feedStr.toString());
        try {
            if (i.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(i);
            } else {
                activity.startActivity(Intent.createChooser(i, "Send mail..."));
            }
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isYoutubeUrl(String url){
        url = url.toLowerCase();
        return url.contains("youtube") || url.contains("googlevideo.com") || url.contains("youtu.be");
    }

}
