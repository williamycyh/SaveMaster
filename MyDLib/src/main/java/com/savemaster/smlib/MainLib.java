package com.savemaster.smlib;

import android.app.Activity;

import java.util.List;

public class MainLib {
    public interface OnBaseDownloadCall {
        public void showFullAd();
        public void showVideoList(List<FileItem> files, Activity activity);
    }
    public static void setOnBaseDownloadCall(OnBaseDownloadCall onBaseDwonloadCall){
        sOnBaseDownload = onBaseDwonloadCall;
    }
    static OnBaseDownloadCall sOnBaseDownload = null;

    public static void showFullScreen(){
        if(sOnBaseDownload != null){
            sOnBaseDownload.showFullAd();
        }
    }

    public static void showVideoList(List<FileItem> files, Activity activity){
        if(sOnBaseDownload != null){
            sOnBaseDownload.showVideoList(files, activity);
        }
    }
}
