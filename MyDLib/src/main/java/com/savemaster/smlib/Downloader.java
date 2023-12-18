package com.savemaster.smlib;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.CookieManager;
import android.widget.Toast;
//
//import com.videodown.core.DownloadFragment;
//import com.videodown.myutils.SharePreferenceUtils;

import com.savemaster.smlib.R;

import java.io.File;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class Downloader {
    DownloadManager downloadManager;
    private String mBaseFolderPath;
    private String mBaseFolderPathPrefix;
    private Context mContext;
    private ProgressDialog pd;
    public boolean tuuuu = false;
    public static final String FB_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";
//    public static final String WINDOWS_AGENT = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.47 Safari/537.36";
    public static final String TAG = "Downloader";

    public static final int TU_TYPE = 1;
    public static final int F_TYPE = 2;
    public static final int INS_TYPE = 3;

    private static Downloader instance;
    public static synchronized Downloader getInstance(Context context) {
        if (instance == null) {
            instance = new Downloader(context);
        }
        return instance;
    }

    public Downloader(Context context){
        mContext = context;
        downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        mBaseFolderPath = "/MasterAVideo";
        mBaseFolderPathPrefix = "/MasterAVideo/" ;// /MYSFVIDEOS/Tube/     MyUtils.RootDirectoryTube
        RootDirectory =  new File(Environment.getExternalStorageDirectory() + "/Download"+mBaseFolderPathPrefix);
        try{
            if (!RootDirectory.exists()) {
                RootDirectory.mkdirs();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private OnStartDownloadListener onStartDownloadListener;

    public void setOnStartDownloadListener(OnStartDownloadListener onStartDownloadListener) {
        this.onStartDownloadListener = onStartDownloadListener;
    }

    public interface OnStartDownloadListener{
        void onStart();
    }

    public boolean isFbUrl(String url){
        return url.contains("facebook.com");
    }

    public boolean isInsta(String url){
        return url.contains("instagram.com");
    }

    public void download(FileItem item){
        download(item, true, false, System.currentTimeMillis());
    }

    public void downloadByUrl(String url, int type){
        String fileName = "";
        if(type == TU_TYPE){
            fileName = BaseCommon.decodeToString("VHViZV8=")+System.currentTimeMillis();
        } else if(type == F_TYPE){
            fileName = "FB_"+System.currentTimeMillis();
        } else if(type == INS_TYPE){
            fileName = "Ins_"+System.currentTimeMillis();
        } else {
            fileName = "File_"+System.currentTimeMillis();
        }
        downloadByUrl(url, fileName, ".mp4");
    }

    public void downloadByUrl(String url, String fileName, String extend){//extend, like .mp4/.gif
//        boolean permission = DownloadFragment.hasPermissions(mContext);
//        if(!permission){
//            Toast.makeText(mContext, "NO STORAGE Permission", Toast.LENGTH_LONG).show();
//            return;
//        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && url.startsWith("http:")){
            url = url.replace("http:", "https:");
        }

        url = url.replace("\\u0026","&").replace("\\","").replace("\"","");

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//        String fileName = "";
//
//        fileName = prefix + System.currentTimeMillis();
        request.setTitle(fileName  + mContext.getString(R.string.savemasterdown_down_notification_title));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        setDownloadDir(request, fileName + extend);
        try{

            request.allowScanningByMediaScanner();
            long downloadID = downloadManager.enqueue(request);

            if(onStartDownloadListener != null){
                onStartDownloadListener.onStart();
            }

            Toast.makeText(mContext, mContext.getString(R.string.start_savemasterdown_downloading), Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Toast.makeText(mContext, "Pls try to Change Method", Toast.LENGTH_LONG).show();
        }

    }


    public void download(FileItem item, boolean showNotify, boolean saveId, long l){
//        boolean permission = DownloadFragment.hasPermissions(mContext);
//        if(!permission){
//            Toast.makeText(mContext, "NO STORAGE Permission", Toast.LENGTH_LONG).show();
//            return;
//        }

        if(item.getUrl() == null){
            return;
        }
        if(item.getText() == null){
            item.setText("_");
        }

        String decodeUrl = item.getUrl().replace("\\u0026","&").replace("\\","").replace("\"","");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && decodeUrl.startsWith("http:")){
            decodeUrl = decodeUrl.replace("http:", "https:");
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(decodeUrl));
        String sane = item.getText().replaceAll("[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]", "_")
                .replaceAll("['+.^:,#\"]", "_");

        StringBuilder filename = new StringBuilder();

        if(sane.length() > 40){
            filename.append(sane.substring(0,40));
        } else {
            filename.append(sane);
        }
        String extend = item.getExtend();
        if(!item.getExtend().startsWith(".")){
            extend = "." + item.getExtend();
        }
        filename.append(extend);

        request.setTitle(item.getText());
        request.setDescription("Start Downloading");

        if(showNotify){
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        } else {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        }

        setDownloadDir(request, filename.toString());

        if(onStartDownloadListener != null){
            onStartDownloadListener.onStart();
        }
        request.allowScanningByMediaScanner();
        try{
            long downloadID = downloadManager.enqueue(request);
            if(saveId) {
                String na = filename.toString().substring(0, filename.toString().lastIndexOf('.'));
                ASharePreferenceUtils.putLong(mContext, "f_name"+na, downloadID);
            }
            Toast.makeText(mContext, "Start download after watch ad", Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(mContext, "Pls Change Method", Toast.LENGTH_LONG).show();
        }
    }

    public void setDownloadDir(DownloadManager.Request request, String filename){
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.

        long firstVersion = ASharePreferenceUtils.getLong(mContext, ASharePreferenceUtils.FIRST_VERSION, 0);
//        if(firstVersion > 100){
//            request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS,mBaseFolderPathPrefix+filename);  // Storage directory path
//        } else {
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//                request.setDestinationUri(Uri.fromFile(new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename)));
////            request.setDestinationInExternalFilesDir(mContext,Environment.DIRECTORY_DOWNLOADS, filename.toString());
//            } else {
//                request.setDestinationInExternalPublicDir(mBaseFolderPath, filename);
//            }
//        }
        request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS,mBaseFolderPathPrefix+filename);

        try {
            if (Build.VERSION.SDK_INT >= 19) {
                MediaScannerConnection.scanFile(mContext, new String[]{new File(DIRECTORY_DOWNLOADS + mBaseFolderPathPrefix+filename).getAbsolutePath()},
                        null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
            } else {
                mContext.sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.fromFile(new File(DIRECTORY_DOWNLOADS + mBaseFolderPathPrefix+filename))));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public File RootDirectory; //= new File(Environment.getExternalStorageDirectory() + "/Download"+mBaseFolderPathPrefix);

    public File getDownloadDir(){
//        long firstVersion = ASharePreferenceUtils.getLong(mContext, ASharePreferenceUtils.FIRST_VERSION, 0);
//        if(firstVersion > 100){
//            return RootDirectory;
//        }
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//            return mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//        }
        return RootDirectory;
    }

    public static boolean cookieOK(){
        if(CookieManager.getInstance() == null){
            return false;
        }
        String cookieStr = CookieManager.getInstance().getCookie("https://www.instagram.com/");
        if(cookieStr != null && cookieStr.contains("sessionid") && cookieStr.contains("ds_user_id")){
            return true;
        }
        return false;
    }

}
