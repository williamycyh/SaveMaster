package com.savemaster.savefromfb.uiact;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.savemaster.smlib.Downloader;
import com.savemaster.smlib.MainLib;

//不用
public class MyVideoWebsActivity extends AppCompatActivity {
//    public static String ABC_TWITTER_JS = "javascript:function clickOnVideo() {var videoLink;try{var items = document.getElementsByTagName(\"video\");for (i = 0; i < items.length; i++) {videoLink = items[i].getAttribute(\"src\");}var links = document.getElementsByTagName(\"video\");for (i = 0; i < links.length; i++) {if (links[i].getAttribute(\"data-testid\") == \"play\"){console.log('links[i].getAttribute: '+i);links[0].setAttribute('onclick', browser.getVideoData(videoLink));}}catch(e){}}clickOnVideo();";
    public static String FB_SCRIPT = "javascript:var e=0;\nwindow.onscroll=function()\n{\n\tvar ij=document.querySelectorAll(\"video\");\n\t\tfor(var f=0;f<ij.length;f++)\n\t\t{\n\t\t\tif((ij[f].parentNode.querySelectorAll(\"img\")).length==0)\n\t\t\t{\n\t\t\t\tvar nextimageWidth=ij[f].nextSibling.style.width;\n\t\t\t\tvar nextImageHeight=ij[f].nextSibling.style.height;\n\t\t\t\tvar Nxtimgwd=parseInt(nextimageWidth, 10);\n\t\t\t\tvar Nxtimghght=parseInt(nextImageHeight, 10); \n\t\t\t\tvar DOM_img = document.createElement(\"img\");\n\t\t\t\t\tDOM_img.height=\"68\";\n\t\t\t\t\tDOM_img.width=\"68\";\n\t\t\t\t\tDOM_img.style.top=(Nxtimghght/2-20)+\"px\";\n\t\t\t\t\tDOM_img.style.left=(Nxtimgwd/2-20)+\"px\";\n\t\t\t\t\tDOM_img.style.position=\"absolute\";\n\t\t\t\t\tDOM_img.src = \"https://image.ibb.co/kobwsk/one.png\"; \n\t\t\t\t\tij[f].parentNode.appendChild(DOM_img);\n\t\t\t}\t\t\n\t\t\tij[f].remove();\n\t\t} \n\t\t\te++;\n};var a = document.querySelectorAll(\"a[href *= 'video_redirect']\");\nfor (var i = 0; i < a.length; i++) {\n    var mainUrl = a[i].getAttribute(\"href\");\n  a[i].removeAttribute(\"href\");\n\tmainUrl=mainUrl.split(\"/video_redirect/?src=\")[1];\n\tmainUrl=mainUrl.split(\"&source\")[0];\n    var threeparent = a[i].parentNode.parentNode.parentNode;\n    threeparent.setAttribute(\"src\", mainUrl);\n    threeparent.onclick = function() {\n        var mainUrl1 = this.getAttribute(\"src\");\n         browser.getData(mainUrl1);\n    };\n}var k = document.querySelectorAll(\"div[data-store]\");\nfor (var j = 0; j < k.length; j++) {\n    var h = k[j].getAttribute(\"data-store\");\n    var g = JSON.parse(h);var jp=k[j].getAttribute(\"data-sigil\");\n    if (g.type === \"video\") {\nif(jp==\"inlineVideo\"){   k[j].removeAttribute(\"data-sigil\");}\n        var url = g.src;\n        k[j].setAttribute(\"src\", g.src);\n        k[j].onclick = function() {\n            var mainUrl = this.getAttribute(\"src\");\n               browser.getData(mainUrl);\n        };\n    }\n\n}";
//    public static String FACEBOOK_SCRIPT_old = "javascript:function clickOnVideo(link, classValueName){browser.getVideoData(link);var element = document.getElementById(\"mInlineVideoPlayer\");element.muted = true;var parent = element.parentNode; parent.removeChild(element);parent.setAttribute('class', classValueName);}function getVideoLink(){try{var items = document.getElementsByTagName(\"div\");for(i = 0; i < items.length; i++){if(items[i].getAttribute(\"data-sigil\") == \"inlineVideo\"){var classValueName = items[i].getAttribute(\"class\");var jsonString = items[i].getAttribute(\"data-store\");var obj = JSON && JSON.parse(jsonString) || $.parseJSON(jsonString);var videoLink = obj.src;var videoName = obj.videoID;items[i].setAttribute('onclick', \"clickOnVideo('\"+videoLink+\"','\"+classValueName+\"')\");}}var links = document.getElementsByTagName(\"a\");for(i = 0; i < links.length; i++){if(links[ i ].hasAttribute(\"data-store\")){var jsonString = links[i].getAttribute(\"data-store\");var obj = JSON && JSON.parse(jsonString) || $.parseJSON(jsonString);var videoName = obj.videoID;var videoLink = links[i].getAttribute(\"href\");var res = videoLink.split(\"src=\");var myLink = res[1];links[i].parentNode.setAttribute('onclick', \"browser.getVideoData('\"+myLink+\"')\");while (links[i].firstChild){links[i].parentNode.insertBefore(links[i].firstChild,links[i]);}links[i].parentNode.removeChild(links[i]);}}}catch(e){}}getVideoLink();";
    public static String INS_SCRIPT = "javascript:function clickOnVideo() {try{var items = document.getElementsByTagName(\"video\");var links = document.getElementsByClassName(\"fXIG0\");for (i = 0; i < links.length; i++) {if (links[i].getAttribute(\"role\") == \"button\"){links[i].setAttribute('onclick',  \"var videoLink = '\"+items[i].getAttribute('src')+\"';  browser.getData(videoLink);\" );}}}catch(e){}}clickOnVideo();";
//    public static String TWITTER_SCRIPT = "javascript:function clickOnVideo() {try{var items = document.getElementsByTagName(\"source\");for (i = 0; i < items.length; i++) {if (items[ i ].getAttribute(\"type\") == \"video/mp4\"){var videoLink = items[i].getAttribute(\"src\");browser.getVideoData(videoLink);}}}catch(e){}}clickOnVideo();";

    public WebView webView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Downloader.getInstance(this).setOnStartDownloadListener(new Downloader.OnStartDownloadListener() {
            @Override
            public void onStart() {
                MainLib.showFullScreen();
            }
        });
    }

//    FullScreenAd fullScreenAd = new FullScreenAd();
//    public void loadFullScreen(){
//        String mopubTest = "24534e1901884e398f1253216226017e";
//        String mopubId = "dc384d23d7cd4844962461dc2fbe37b3";
////        String admobId = "ca-app-pub-2350160287164274/4649582488";
////        String admobTest = "ca-app-pub-3940256099942544/1033173712";
////
////        String fbTest = "IMG_16_9_APP_INSTALL#265360235148244_266066601744274";
////        String fbId = "265360235148244_266066601744274";
//        fullScreenAd.loadFullScreen(this, mopubId, "", "");
//    }
//
//    public boolean showFullScreenAd(){
//        if(fullScreenAd != null){
//            return fullScreenAd.showAd();
//        }
//        return false;
//    }


    public void showPopDialog(final String url, int type){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setMessage("Confirm Download this video?");
        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Downloader.getInstance(MyVideoWebsActivity.this).downloadByUrl(url, type);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onBackPressed() {
        if(webView != null && webView.canGoBack()){
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
