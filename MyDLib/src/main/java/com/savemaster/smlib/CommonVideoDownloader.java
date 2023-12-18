package com.savemaster.smlib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.Toast;

//import com.infusiblecoder.allinonevideodownloader.tiktok_methods.FacebookDownloadCloudBypassWebview_method_1;
//import com.infusiblecoder.allinonevideodownloader.utils.Constants;
//import com.infusiblecoder.allinonevideodownloader.webservices.FbVideoDownloader;
//import com.mypacks.InsDownloadCloudBypassWebview_method_1;
//import com.mypacks.MyDownloadVideosMain;
//import com.mypacks.TubeDownloadCloudBypassWebview_method_1;
//import com.videodownloader.fb.Extractor;
//import com.videodownloader.fb.VideoMeta;
//import com.videodownloader.fb.YFile;
//import com.videodownloader.sharedPre.UIConfigManager;
//import com.videodownloader.utils.Commons;
//import com.videodownloader.utils.FileItem;
//import com.videodownloader.utils.MyUtils;
//import com.videodownloader.utils.TwitterUtils;

import com.savemaster.pklib.fbwebdownload;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommonVideoDownloader {
    public Context sContext;
    static String Title;
    public Dialog dialog;
    public ProgressDialog pd;
    public static final String TAG = "CommonVideoDownloader";
    public static final String CHROME_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36";
    public static final String WINDOWS_AGENT = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.47 Safari/537.36";
    public static final String FB_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";
    public static final String MOZILLA_AGENT = "Mozilla";
//    private MyDownloadVideosMain downloadCommon;

    public static final int TYPE_TU = 1;
    public static final int TYPE_FB = 2;
    public static final int TYPE_INS = 3;

    public static final int FAIL_TYPE_LOGIN = 1;
    public static final int FAIL_TYPE_OTHERS = 9;


    public interface OnDownloadListener{
        void onSuccess(List<FileItem> files, int videoType);
        void onFail(int videoType, int failType, String failDetail);
    }

    private OnDownloadListener onDownloadListener;

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    public boolean isProgressShowing(){
        if(pd != null){
            return pd.isShowing();
        }
        return false;
    }

    public void startDownload(Context context, String inputurl) {
        if(TextUtils.isEmpty(inputurl)){
            return;
        }
        if(!BaseCommon.checkNet(context)){
            Toast.makeText(context,"Network Error.", Toast.LENGTH_LONG).show();
            return;
        }

        sContext = context;
        boolean tu = BaseCommon.isYoutubeUrl(inputurl);

        if (!inputurl.startsWith("http://")) {
            if (!inputurl.startsWith("https://")) {
                StringBuilder sb = new StringBuilder();
                sb.append("http://");
                sb.append(inputurl);
                inputurl = sb.toString();
            }
        }
        String url = inputurl;

        if(context instanceof Activity){
            pd = new ProgressDialog(context);
            pd.setMessage("Generating download link");
            pd.setCancelable(false);
            pd.show();
        }

        Log.d(TAG, "input url:" + url);

//        downloadCommon = new MyDownloadVideosMain();
//        DownloadCommon.Companion.setDownloadCallback(new Function1<Integer, Unit>() {
//            @Override
//            public Unit invoke(Integer type) {
//                if(type == 1){//insta
//                    new GetInstagramVideo().execute(new String[]{url});
//                } else if(type == 2){
//                    new GetFacebookVideo().execute(new String[]{url});
//                }
//                return null;
//            }
//        });
        if(tu){
            showTuDialog(url);
        } else if (url.contains("instagram.com")) {
            showInsDialog(url);
        } else if(url.contains("facebook.com") || url.contains("fb.watch")){
            showFbDialog(url);
        } else {
            if(ServerDownloadUtils.isSupport(url)){
                ServerDownloadUtils.CalldlApisDataDataServerUrl((Activity) sContext, pd, url, false);
            } else {
                Toast.makeText(context,"Error.", Toast.LENGTH_LONG).show();
                if(pd != null){pd.dismiss();}
            }
        }

//        if (url.contains("tiktok.com")) {
//            new GetTikTokVideo().execute(new String[]{url});
//        } else if (url.contains("facebook.com")) {
//            new GetFacebookVideo().execute(new String[]{url});
//        } else if (url.contains("instagram.com")) {
//            new GetInstagramVideo().execute(new String[]{url});
//        } else if(url.contains("likee.video")){
//            new GetLikeeVideo().execute(new String[]{url});
//        } else if(url.contains("twitter.com")){
//            new GetTwitterVideo().execute(new String[]{url});
//        } else if(url.contains("pin.it") || url.contains("pinterest.com")){
////            new GetPinterest().execute(new String[]{url});
//        } else if(tu){
//            getVideo(url, context);
//        } else {
//            new GetOthersVideo().execute(new String[]{url});
//        }
    }

    public void startDownloadNoTube(Context context, String inputurl) {
        if(TextUtils.isEmpty(inputurl)){
            return;
        }
        if(!BaseCommon.checkNet(context)){
            Toast.makeText(context,"Network Error.", Toast.LENGTH_LONG).show();
            return;
        }

        sContext = context;
        boolean tu = BaseCommon.isYoutubeUrl(inputurl);

        if (!inputurl.startsWith("http://")) {
            if (!inputurl.startsWith("https://")) {
                StringBuilder sb = new StringBuilder();
                sb.append("http://");
                sb.append(inputurl);
                inputurl = sb.toString();
            }
        }
        String url = inputurl;

        if(context instanceof Activity){
            pd = new ProgressDialog(context);
            pd.setMessage("Generating download link");
            pd.setCancelable(false);
            pd.show();
        }

        Log.d(TAG, "input url:" + url);
        if (url.contains("instagram.com")) {
            showInsDialog(url);
        } else if(url.contains("facebook.com") || url.contains("fb.watch")){
            showFbDialog(url);
        } else {
            Toast.makeText(context,"Error.", Toast.LENGTH_LONG).show();
            if(pd != null){pd.dismiss();}
        }
    }

    private void showFbDialog(String url){
        Dialog dialog = new Dialog(sContext);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.savemasterdown_download_selectdi_alog);
        Button methode0 = dialog.findViewById(R.id.dig_btn_met0);
        Button methode1 = dialog.findViewById(R.id.dig_btn_met1);
        Button methode2 = dialog.findViewById(R.id.dig_btn_met2);
        methode0.setText(sContext.getString(R.string.alidownsavemasterdown_laodmethode_0) + "(" + sContext.getString(R.string.methosavemasterdown_d_local) + ")");
        methode1.setText(sContext.getString(R.string.savemasterdown_hode_1) + "(" + sContext.getString(R.string.methsavemasterdown_od_third) + ")");
        methode2.setText(sContext.getString(R.string.savemasterdown_)); //+ "(" + sContext.getString(R.string.method_server) + ")");
        methode2.setVisibility(View.GONE);
        Button dig_btn_cancel = dialog.findViewById(R.id.dig_btn_cancel);
        methode0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                new GetFacebookVideo().execute(url);
            }
        });

        methode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(sContext, fbwebdownload.class);
                intent.putExtra("myvidurl", url);
                sContext.startActivity(intent);
                if(pd != null){pd.dismiss();}

                dialog.dismiss();
            }
        });
//        methode2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//                FbVideoDownloader fbVideoDownloader = new FbVideoDownloader(sContext, url, 1);
//                fbVideoDownloader.DownloadVideo();
//            }
//        });
        dig_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(pd != null){pd.dismiss();}
            }
        });
        dialog.show();
    }

    private void showInsDialog(String url){
        Dialog dialog = new Dialog(sContext);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.savemasterdown_download_selectdi_alog);
        Button methode0 = dialog.findViewById(R.id.dig_btn_met0);
        Button methode1 = dialog.findViewById(R.id.dig_btn_met1);
        Button methode2 = dialog.findViewById(R.id.dig_btn_met2);
        methode0.setText(sContext.getString(R.string.alidownsavemasterdown_laodmethode_0) + "(" + sContext.getString(R.string.methosavemasterdown_d_local) + ")");
        methode1.setText(sContext.getString(R.string.savemasterdown_hode_1) + "(" + sContext.getString(R.string.methsavemasterdown_od_third) + ")");
        methode2.setText(sContext.getString(R.string.savemasterdown_)); //+ "(" + sContext.getString(R.string.method_server) + ")");
        Button dig_btn_cancel = dialog.findViewById(R.id.dig_btn_cancel);
        methode0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                new GetInstagramVideo().execute(url);
            }
        });

        methode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(sContext, inswebdownload.class);
                intent.putExtra("myvidurl", url);
                sContext.startActivity(intent);
                if(pd != null){pd.dismiss();}

            }
        });
//        methode2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//                FbVideoDownloader fbVideoDownloader = new FbVideoDownloader(sContext, url, 1);
//                fbVideoDownloader.DownloadVideo();
//            }
//        });
        dig_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(pd != null){pd.dismiss();}
            }
        });
        dialog.show();
    }


    private void showTuDialog(String url){
        Dialog dialog = new Dialog(sContext);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.savemasterdown_download_selectdi_alog);
        Button methode0 = dialog.findViewById(R.id.dig_btn_met0);
        Button methode1 = dialog.findViewById(R.id.dig_btn_met1);
        Button methode2 = dialog.findViewById(R.id.dig_btn_met2);
        Button methode3 = dialog.findViewById(R.id.dig_btn_met3);
        methode3.setVisibility(View.VISIBLE);
//        methode2.setVisibility(View.GONE);
        methode0.setText(sContext.getString(R.string.alidownsavemasterdown_laodmethode_0) + "(" + sContext.getString(R.string.methosavemasterdown_d_local) + ")");
        methode1.setText(sContext.getString(R.string.savemasterdown_hode_1) + "(" + sContext.getString(R.string.methodsavemasterdown__local_2) + ")");
        methode2.setText(sContext.getString(R.string.savemasterdown_) + "(" + sContext.getString(R.string.methsavemasterdown_od_third) + ")");
        methode3.setText(sContext.getString(R.string.metsavemasterdown_hode_3) + "(" + sContext.getString(R.string.methsavemasterdown_od_server) + ")");


        Button dig_btn_cancel = dialog.findViewById(R.id.dig_btn_cancel);
        methode0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                String agent = MyUtils.random_user_agent();
                getVideo(url, sContext, agent);
            }
        });

        methode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                String agent = ASharePreferenceUtils.getString(sContext, ASharePreferenceUtils.SERVER_AGENT, "");
                if(TextUtils.isEmpty(agent)){
                    agent = Extractor.USER_AGENT_WIN;
                }
                getVideo(url, sContext, agent);
            }
        });

        methode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(sContext, webdownloader.class);
                intent.putExtra("myvidurl", url);
                if(!(sContext instanceof Activity)){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                sContext.startActivity(intent);
                if(pd != null){pd.dismiss();}
            }
        });
        methode3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                String finalurl = url.replace("%2F", "/").replace("%26", "&").replace("%3F", "?").replace("%3D", "=").replace("%25", "%");
                if(sContext instanceof Activity){
                    ServerDownloadUtils.CalldlApisDataDataServerUrl((Activity) sContext, pd, finalurl, false);
                } else {
                    Toast.makeText(sContext.getApplicationContext(), "Not support on float mode", Toast.LENGTH_SHORT).show();
                }

            }
        });
        dig_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(pd != null){pd.dismiss();}
            }
        });
        if(!(sContext instanceof Activity)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
            } else {
                dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_PHONE));
            }
        }

        dialog.show();
    }


    @SuppressLint("StaticFieldLeak")
    private void getVideo(String url, Context context, String agent){
        Extractor extractor = new Extractor(context){
            @Override
            protected void onExtractionComplete(SparseArray<YFile> ytFiles, VideoMeta videoMeta, StringBuilder exceptionBuilder) {
                if(ytFiles == null || ytFiles.size() <= 0){
                    onDownloadFail(TYPE_TU, FAIL_TYPE_OTHERS, exceptionBuilder.toString());
                    return;
                }
                List<FileItem> fileList = new ArrayList<>();

                boolean has_1080_mp4 = false;
                boolean has_720_mp4 = false;
                YFile noVoice1080Mp4 = null;
                YFile noVoice720Mp4 = null;
                YFile voiceM4a = null;
                for(int i = 0; i <ytFiles.size(); i++ ){
                    int key = ytFiles.keyAt(i);
                    YFile yFile = ytFiles.get(key);
                    FileItem item = new FileItem();
                    item.setUrl(yFile.getUrl());
                    item.setText(videoMeta.getTitle());
                    item.setExtend(yFile.getFormat().getExt());
//                    item.setFname();
                    item.setyFile(yFile);
                    fileList.add(item);
                    if(yFile.getFormat().getHeight() == 720
                            && "mp4".equalsIgnoreCase(yFile.getFormat().getExt())){//720 mp4
                        if(yFile.getFormat().isDashContainer()){// no audio
                            noVoice720Mp4 = yFile;
                        } else {
                            has_720_mp4 = true;
                        }
                    }
                    if(yFile.getFormat().getHeight() == 1080
                            && "mp4".equalsIgnoreCase(yFile.getFormat().getExt())){
                        if(yFile.getFormat().isDashContainer()){// no audio
                            noVoice1080Mp4 = yFile;
                        } else {
                            has_1080_mp4 = true;
                        }
                    }

                    if("m4a".equalsIgnoreCase(yFile.getFormat().getExt())){
                        voiceM4a = yFile;
                    }
                }
                if(voiceM4a != null){
                    if(!has_720_mp4 && noVoice720Mp4 != null){
                        FileItem item = new FileItem();
                        item.setUrl(noVoice720Mp4.getUrl());
                        item.setText(videoMeta.getTitle());
                        item.setExtend(noVoice720Mp4.getFormat().getExt());
                        item.setyFile(noVoice720Mp4);
                        item.setVoiceFile(voiceM4a);
                        fileList.add(item);
                        Log.d("muxvideo","add 720Video");
                    }
                    if(!has_1080_mp4 && noVoice1080Mp4 != null){
                        FileItem item = new FileItem();
                        item.setUrl(noVoice1080Mp4.getUrl());
                        item.setText(videoMeta.getTitle());
                        item.setExtend(noVoice1080Mp4.getFormat().getExt());
                        item.setyFile(noVoice1080Mp4);
                        item.setVoiceFile(voiceM4a);
                        fileList.add(item);
                        Log.d("muxvideo","add 1080Video");
                    }
                }

                onDownloadSuccess(fileList, TYPE_TU);
                if(pd != null){pd.dismiss();}
            }
        };
        extractor.init();
        extractor.extract(url, true, agent);
    }


    private static final Pattern sd_no_pattern = Pattern.compile("sd_src_no_ratelimit:\"([^\"]+)\"");
    private static final Pattern sd_stc = Pattern.compile("sd_src:\"([^\"]+)\"");

    private static final Pattern hd_no_pattern = Pattern.compile("hd_src_no_ratelimit:\"([^\"]+)\"");
    private static final Pattern hd_stc = Pattern.compile("hd_src:\"([^\"]+)\"");
    private static final Pattern inline_video = Pattern.compile("\"([^\"]+)\" data-sigil=\"inlineVideo\"");
    private static final Pattern inline_pattern = Pattern.compile("\"src\":\"(.*?)\\\"");

    private class GetFacebookVideo
            extends AsyncTask<String, Void, Document> {
        Document doc;

        protected Document doInBackground(String... paramVarArgs) {
            try {
//                String url = "https://web.facebook.com/watch/?v=295685139061431";
//                Map<String,String> headers = new HashMap<>();
//                headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
//                headers.put("sec-fetch-mode", "navigate");
//                headers.put("cache-control", "max-age=0");
//                headers.put("accept-language", "en");
//                headers.put("Upgrade-Insecure-Requests", "0");

                this.doc = Jsoup.connect(paramVarArgs[0]).userAgent(MyUtils.random_user_agent())
//                        .header("cookie", SharePreferenceUtils.getString(sContext, SharePreferenceUtils.FBCOOKIES, ""))
                        .get();
            } catch (IOException exception) {
                Log.d(TAG, "GetFacebookVideo exception:" + exception.getMessage());
            }
            return this.doc;
        }

        protected void onPostExecute(Document paramDocument) {
            boolean needLoin = false;
            try {
//                String str = paramDocument.select("meta[property=\"og:video\"]").last().attr("content");
//                Log.d(TAG, "GetFacebookVideo url:" + str);
                if(paramDocument.location().contains("/login")){
                    needLoin = true;
                }
                List<FileItem> fileList = new ArrayList<>();

                Matcher mat = sd_no_pattern.matcher(paramDocument.toString());
                String sdUrl = "";
                if (mat.find()) {
                    sdUrl = mat.group(1);
                }
                if(TextUtils.isEmpty(sdUrl)){
                    mat = sd_stc.matcher(paramDocument.toString());
                    if (mat.find()) {
                        sdUrl = mat.group(1);
                    }
                }
                if(!TextUtils.isEmpty(sdUrl)){
                    FileItem sd = new FileItem();
                    sd.setUrl(sdUrl);
                    sd.setText(paramDocument.title());
                    sd.setExtend(".mp4");
                    sd.setXd("SD");
                    fileList.add(sd);
                }

                mat = hd_no_pattern.matcher(paramDocument.toString());
                String hdUrl = "";
                if (mat.find()) {
                    hdUrl = mat.group(1);
                }
                if(TextUtils.isEmpty(hdUrl)){
                    mat = hd_stc.matcher(paramDocument.toString());
                    if (mat.find()) {
                        hdUrl = mat.group(1);
                    }
                }
                if(!TextUtils.isEmpty(hdUrl)){
                    FileItem hd = new FileItem();
                    hd.setUrl(hdUrl);
                    hd.setText(paramDocument.title());
                    hd.setXd("HD");
                    hd.setExtend(".mp4");
                    fileList.add(hd);
                }

                if(fileList.size() <= 0){
                    Elements elements = paramDocument.select("meta[property=\"og:video\"]");
                    if(elements != null && elements.size() > 0){
                        Element velement = elements.last();
                        String str = velement.attr("content");
                        Log.d(TAG, "GetFacebookVideo url:" + str);
                        CommonVideoDownloader.Title = paramDocument.title();
                        FileItem item = new FileItem();
                        item.setUrl(str);
                        item.setText(paramDocument.title());
                        item.setExtend(".mp4");
                        fileList.add(item);
                    }
                }

                if(fileList.size() <= 0){
                    mat = inline_video.matcher(paramDocument.toString());
                    if (mat.find()) {
                        String childJson = mat.group(1);
                        if(!TextUtils.isEmpty(childJson)){
                            childJson = childJson.replace("&quot;","\"").replace("&amp;","&").replace("\\","");
                            mat = inline_pattern.matcher(childJson);
                            if(mat.find()){
                                String videoUrl = mat.group(1);
                                CommonVideoDownloader.Title = paramDocument.title();
                                FileItem item = new FileItem();
                                item.setUrl(videoUrl);
                                item.setText(paramDocument.title());
                                item.setExtend(".mp4");
                                fileList.add(item);
                            }
                        }
                    }
                }

                if(fileList.size() <= 0){//find img
                    Elements elements = paramDocument.select("meta[property=\"og:image\"]");
                    if(elements != null && elements.size() > 0){
                        Element imgElement = elements.last();
                        String str = imgElement.attr("content");
                        if(TextUtils.isEmpty(str) || str.toLowerCase().contains("fb_icon")){

                        } else {
                            Log.d(TAG, "GetFacebookVideo url img:" + str);
                            CommonVideoDownloader.Title = paramDocument.title();
                            FileItem item = new FileItem();
                            item.setUrl(str);
                            item.setText(paramDocument.title());
                            item.setExtend(".jpg");
                            fileList.add(item);
                        }
                    }
                }

                if(fileList.size() == 1){
//                    DownloadFile.getInstance(DownloadVideo.sContext).Downloading(fileList.get(0).getUrl(), paramDocument.title(), fileList.get(0).getExtend());
                    onDownloadSuccess(fileList, TYPE_FB);
                } else if(fileList.size() > 1){
                    onDownloadSuccess(fileList, TYPE_FB);
                } else {
                    if(needLoin){
                        onDownloadFail(TYPE_FB, FAIL_TYPE_LOGIN, "");
                    } else {
                        onDownloadFail(TYPE_FB, FAIL_TYPE_OTHERS, "");
                    }
                }

                if(pd != null){pd.dismiss();}

            } catch (NullPointerException exception) {
                Log.d(TAG, "GetFacebookVideo exception:" + exception.getMessage());
                if(needLoin){
                    onDownloadFail(TYPE_FB, FAIL_TYPE_LOGIN, exception.getMessage());
                } else {
                    onDownloadFail(TYPE_FB, FAIL_TYPE_OTHERS, exception.getMessage());
                }
            }

            if(pd != null){pd.dismiss();}
        }
    }

    private class GetInstagramVideo
            extends AsyncTask<String, Void, Document> {
        Document doc;

        protected Document doInBackground(String... paramVarArgs) {
            try {
//                CookieSyncManager.getInstance().sync();
                String cookieStr = CookieManager.getInstance().getCookie("https://www.instagram.com/");
                Map<String, String> cookies = new HashMap<>();
                if(cookieStr != null){
                    String maps[] = cookieStr.split(";");
                    for(String cookie: maps){
                        if(!TextUtils.isEmpty(cookie)){
                            String[] valuesmap = cookie.trim().split("=");
                            if(valuesmap.length == 2){
                                cookies.put(valuesmap[0].trim(),valuesmap[1].trim());
                            }
                        }
                    }

                }

                this.doc = Jsoup.connect(paramVarArgs[0]).userAgent(MyUtils.random_user_agent()).cookies(cookies).get();

//                this.doc = Jsoup.connect(paramVarArgs[0]).userAgent(WINDOWS_AGENT).get();
            } catch (IOException exception) {
                Log.d(TAG, "GetInstagramVideo exception:" + exception.getMessage());
            }
            return this.doc;
        }

        protected void onPostExecute(Document paramDocument) {
            boolean needLogin = false;
            try {
                List<FileItem> fileList = new ArrayList<>();

                if(paramDocument.location().contains("accounts/login")){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().removeAllCookies(null);
                    }
                }
                CommonVideoDownloader.Title = paramDocument.title();

                Set<String> imageUrls = new HashSet<>();
                Matcher matcher = Pattern.compile("\"display_url\":\"(.*?)\"").matcher(doc.toString());
                while (matcher.find()) {
                    imageUrls.add(matcher.group(1));
                    Log.d(TAG, "match:"+ matcher.group(1));
                }

                Set<String> videoUrls = new HashSet<>();
                Matcher matcher2 = Pattern.compile("\"video_url\":\"(.*?)\"").matcher(doc.toString());
                while (matcher2.find()) {
                    videoUrls.add(matcher2.group(1));
                    Log.d(TAG, "match:"+ matcher2.group(1));
                }

                if(imageUrls.isEmpty() && videoUrls.isEmpty()){
                    Elements videoElements = doc.select("meta[property=\"og:video\"]");
                    if (videoElements != null && videoElements.size() > 0) {
                        String url = videoElements.last().attr("content");
                        videoUrls.add(url);
                    } else {
                        Elements imageElements = doc.select("meta[property=\"og:image\"]");
                        if(imageElements != null && imageElements.size() > 0){
                            String url = imageElements.last().attr("content");
                            imageUrls.add(url);
                        }
                    }
                }

                if(imageUrls.isEmpty() && videoUrls.isEmpty()){
                    onDownloadFail(TYPE_INS, FAIL_TYPE_LOGIN, "");
                } else {
                    if(!videoUrls.isEmpty()){
                        for(String video:videoUrls){

                            FileItem item = new FileItem();
                            item.setUrl(video);
                            item.setText("Ins_"+System.currentTimeMillis());
                            item.setExtend(".mp4");
                            fileList.add(item);
//                            DownloadFile.getInstance(DownloadVideo.sContext).Downloading(video, DownloadVideo.Title, ".mp4");
                        }
                    } else if(!imageUrls.isEmpty()){
                        for (String img:imageUrls){
                            FileItem item = new FileItem();
                            item.setUrl(img);
                            item.setText("Ins_"+System.currentTimeMillis());
                            item.setExtend(".jpg");
                            fileList.add(item);
//                            DownloadFile.getInstance(DownloadVideo.sContext).Downloading(img, DownloadVideo.Title, ".jpg");
                        }
                    }
                }
            } catch (Exception exception) {
                Log.d(TAG, "GetInstagramVideo exception:" + exception.getMessage());
                onDownloadFail(TYPE_INS, FAIL_TYPE_OTHERS, exception.getMessage());
            }

            if(pd != null){pd.dismiss();}
        }
    }

//    private class GetTikTokVideo
//            extends AsyncTask<String, Void, FileItem> {
//        FileItem item = new FileItem();
//
//        protected FileItem doInBackground(String... paramVarArgs) {
//            try {
//                String url = "";
//                Document doc = Jsoup.connect(paramVarArgs[0]).userAgent(MOZILLA_AGENT).get();
//                url = doc.select("video[src]").attr("src");
//                if(!TextUtils.isEmpty(url)){
//                    item.setUrl(url);
//                    item.setText(doc.title());
//                    return item;
//                }
////                Pattern.compile("\"video_url\":\"(.*?)\"").matcher(doc.toString());
//                Matcher matcher2 = Pattern.compile("\"urls\":\\[(.*?)\\]").matcher(doc.toString());
//                while (matcher2.find()) {
//                    int count = matcher2.groupCount();
//                    String sUrl = matcher2.group(1);
//                    String urls[] = sUrl.split(",");
//                    if(urls != null || urls.length > 0){
//                        url = urls[0].replace("\"","");
//                        item.setUrl(url);
//                        item.setText(doc.title());
//                        return item;
//                    }
//
//                    Log.d(TAG, "match:"+ matcher2.group(1));
//                }
//
////                String str = doc.select("link[rel=\"canonical\"]").last().attr("href");
//////                if(TextUtils.isEmpty(str)){
//////                    str = doc.select("video[poster]").attr("poster");
//////                }
////                if ((!str.equals("")) && (str.contains("video/")))
////                {
////                    str = str.split("video/")[1];
////
////                }
////                HashMap localHashMap = new HashMap();
////                localHashMap.put("Cookie", "1");
////                localHashMap.put("User-Agent", "1");
////                localHashMap.put("Accept", "application/json");
////                localHashMap.put("Host", "api2-16-h2.musical.ly");
////                localHashMap.put("Connection", "keep-alive");
////                //https://api2-16-h2.musical.ly/aweme/v1/aweme/detail/
////                Document paramDocument = Jsoup.connect("https://api2-16-h2.musical.ly/aweme/v1/play/detail/").data("aweme_id", str).ignoreContentType(true).headers(localHashMap).get();
////
////                String html = paramDocument.body().toString().replace("<body>", "").replace("</body>", "");
////                try
////                {
////                    JSONObject jsonObject = new JSONObject(html);
////                    url = jsonObject.getJSONObject("aweme_detail").getJSONObject("video").getJSONObject("play_addr").getJSONArray("url_list").getString(0);
////                    item.setUrl(url);
////                    item.setText(doc.title());
////                }
////                catch (JSONException exception)
////                {
////                    Log.d("Error", exception.toString());
////                }
//            } catch (IOException exception) {
//                exception.printStackTrace();
//                Log.d(TAG, "GetTikTokVideo exception:" + exception.getMessage());
//            }
//
//            return item;
//        }
//
//        protected void onPostExecute(FileItem item) {
//            try {
////        String str = paramDocument.select("link[rel=\"canonical\"]").last().attr("href");
////                String str = paramDocument.select("video[src]").attr("src");
////                if(TextUtils.isEmpty(str)){
////                    str = paramDocument.select("video[poster]").attr("poster");
////                }
//                Log.d(TAG, "GetTikTokVideo url:" + item.getUrl());
//                if (!TextUtils.isEmpty(item.getUrl())) {
//                    CommonVideoDownloader.Title = item.getText();
//                    DownloadFile.getInstance(DownloadVideo.sContext).Downloading(item.getUrl(), CommonVideoDownloader.Title, ".mp4");
//                } else {
//                    onDownloadFail();
//                }
//                if (!fromService) {
//                    if(pd != null){pd.dismiss();}
//                }
//            } catch (Exception exception) {
//                Log.d(TAG, "GetTikTokVideo exception:" + exception.getMessage());
//                onDownloadFail();
//            }
//        }
//    }

    public void onDownloadFail(int videoType, int failType, String failDetail){
        if (pd != null && pd.isShowing()) {
            if(pd != null){pd.dismiss();}
        }
//        Toast.makeText(sContext, sContext.getString(R.string.no_video_image_found_1), Toast.LENGTH_LONG).show();
        if(onDownloadListener != null){
            onDownloadListener.onFail(videoType, failType, failDetail);
        }
    }

//    public void onDownloadFail(){
//        onDownloadFail(0);
//    }

    public void onDownloadSuccess(List<FileItem> items, int type){
        if(onDownloadListener != null){
            onDownloadListener.onSuccess(items, type);
        }
    }
}
