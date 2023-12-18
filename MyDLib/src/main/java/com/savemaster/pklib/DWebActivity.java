package com.savemaster.pklib;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.savemaster.smlib.BaseWebActivity;
import com.savemaster.smlib.Downloader;
import com.savemaster.smlib.R;

public class DWebActivity extends BaseWebActivity {

    public String target_url = "https://www.facebook.com";
    ProgressBar progress_bar;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savemasterdown_lidownloa_dfb_web);

        progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.GONE);
        webView = (WebView) findViewById(R.id.webview);
        // WebSettings webSettings = webView.getSettings();
        //webView.loadUrl("http://www.facebook.com");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.addJavascriptInterface(this, "browser");
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progress_bar.setProgress(newProgress);
            }

            @Override
            public Bitmap getDefaultVideoPoster() {
                return Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
            }
        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progress_bar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
//                webView.loadUrl("javascript:(function() { "
//                        + "var el = document.querySelectorAll('div[data-sigil]');"
//                        + "for(var i=0;i<el.length; i++)"
//                        + "{"
//                        + "var sigil = el[i].dataset.sigil;"
//                        + "if(sigil.indexOf('inlineVideo') > -1){"
//                        + "delete el[i].dataset.sigil;"
//                        + "var jsonData = JSON.parse(el[i].dataset.store);"
//                        + "el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\");');"
//                        + "}" + "}" + "})()");
//                webView.loadUrl(FACEBOOK_SCRIPT);
                progress_bar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadResource(WebView view, String url)
            {
//                webView.loadUrl("javascript:(function prepareVideo() { "
//                        + "var el = document.querySelectorAll('div[data-sigil]');"
//                        + "for(var i=0;i<el.length; i++)"
//                        + "{"
//                        + "var sigil = el[i].dataset.sigil;"
//                        + "if(sigil.indexOf('inlineVideo') > -1){"
//                        + "delete el[i].dataset.sigil;"
//                        + "console.log(i);"
//                        + "var jsonData = JSON.parse(el[i].dataset.store);"
//                        + "el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\",\"'+jsonData['videoID']+'\");');"
//                        + "}" + "}" + "})()");
//                webView.loadUrl("javascript:( window.onload=prepareVideo;"
//                        + ")()");
                webView.loadUrl(FB_SCRIPT);
            }
        });

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieSyncManager.getInstance().startSync();

        webView.loadUrl(target_url);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @JavascriptInterface
    public void getData(final String vidData)
    {
        try
        {
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   showPopDialog(vidData, Downloader.F_TYPE);
               }
           });
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Download Failed: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

//    @JavascriptInterface
//    public void processVideo(final String vidData, final String vidID)
//    {
//        try
//        {
//            String mBaseFolderPath = android.os.Environment
//                    .getExternalStorageDirectory()
//                    + File.separator
//                    + "FacebookVideos" + File.separator;
//            if (!new File(mBaseFolderPath).exists())
//            {
//                new File(mBaseFolderPath).mkdir();
//            }
//            String mFilePath = "file://" + mBaseFolderPath + "/" + vidID + ".mp4";
//
//            Uri downloadUri = Uri.parse(vidData);
//            DownloadManager.Request req = new DownloadManager.Request(downloadUri);
//            req.setDestinationUri(Uri.parse(mFilePath));
//            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//            DownloadManager dm = (DownloadManager) getSystemService(getApplicationContext().DOWNLOAD_SERVICE);
//            dm.enqueue(req);
//            Toast.makeText(this, "Download Started", Toast.LENGTH_LONG).show();
//        }
//        catch (Exception e)
//        {
//            Toast.makeText(this, "Download Failed: " + e.toString(), Toast.LENGTH_LONG).show();
//        }
//    }

}
