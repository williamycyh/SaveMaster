package com.savemaster.smlib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.savemaster.smlib.R;


public class webdownloader extends AppCompatActivity {


    public static Handler handler;
    static String myvidintenturlis = "";
    private static ValueCallback<Uri[]> mUploadMessageArr;
    String TAG = "whatsapptag";
    boolean doubleBackToExitPressedOnce = false;
    boolean isdownloadstarted = false;
    ProgressDialog progressDialog;
    private String mWebSiteUrl = "";

    WebView webViewscan;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.savemasterdown_downd_webview);

//        setSupportActionBar(findViewById(R.id.tool12));
        InitHandler();
        mWebSiteUrl = BaseCommon.decodeToString("aHR0cHM6Ly9pZC5zYXZlZnJvbS5uZXQv");

        webViewscan = findViewById(R.id.webViewscan);
        progressBar = findViewById(R.id.progressBar);

        try {
            progressDialog = new ProgressDialog(webdownloader.this);
            progressDialog.setMessage(getString(R.string.nodesavemasterdown_ifittakeslonger));
            progressDialog.show();
        } catch (Exception e) {

        }
        if (getIntent().getStringExtra("myvidurl") != null && !getIntent().getStringExtra("myvidurl").equals("")) {
            myvidintenturlis = getIntent().getStringExtra("myvidurl");
        }
//        opentiktok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mWebSiteUrl));
//
//                    intent.setPackage("com.zhiliaoapp.musically");
//
//                    startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    iUtils.ShowToast(TubeDownloadCloudBypassWebview_method_1.this, "Tiktok not Installed");
//                }
//            }
//        });

        if (Build.VERSION.SDK_INT >= 24) {
            onstart();
            webViewscan.clearFormData();
            webViewscan.getSettings().setSaveFormData(true);
            // webViewscan.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
            webViewscan.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
            // webViewscan.setWebChromeClient(new webChromeClients());
            webViewscan.setWebViewClient(new MyBrowser());
//            webViewscan.getSettings().setAppCacheMaxSize(5242880);
            webViewscan.getSettings().setAllowFileAccess(true);
//            webViewscan.getSettings().setAppCacheEnabled(true);
            webViewscan.getSettings().setJavaScriptEnabled(true);
            webViewscan.getSettings().setDefaultTextEncodingName("UTF-8");
            webViewscan.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webViewscan.getSettings().setDatabaseEnabled(true);
            webViewscan.getSettings().setBuiltInZoomControls(false);
            webViewscan.getSettings().setSupportZoom(true);
            webViewscan.getSettings().setUseWideViewPort(true);
            webViewscan.getSettings().setDomStorageEnabled(true);
            webViewscan.getSettings().setAllowFileAccess(true);
            webViewscan.getSettings().setLoadWithOverviewMode(true);
            webViewscan.getSettings().setLoadsImagesAutomatically(true);
            webViewscan.getSettings().setBlockNetworkImage(false);
            webViewscan.getSettings().setBlockNetworkLoads(false);
            webViewscan.getSettings().setLoadWithOverviewMode(true);


            webViewscan.setWebChromeClient(new WebChromeClient() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    request.grant(request.getResources());
                }
            });
            webViewscan.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimetype,
                                            long contentLength) {
//
//                    String nametitle = "Facebook_" +
//                            System.currentTimeMillis();
//
//                    new downloadFile().Downloading(FacebookDownloadCloudBypassWebview_method_1.this, url, nametitle, ".mp4");
//

                }
            });

            webViewscan.setWebChromeClient(new WebChromeClient() {

                public void onProgressChanged(WebView view, int progress) {
                    if (progress < 100 && progressBar.getVisibility() == View.GONE) {
                        progressBar.setVisibility(View.VISIBLE);

                    }

                    progressBar.setProgress(progress);
                    if (progress == 100) {
                        progressBar.setVisibility(View.GONE);

                    }
                }
            });


            webViewscan.loadUrl(mWebSiteUrl);


//            Cloudflare cf = new Cloudflare(FacebookDownloadCloudBypassWebview_method_1.this, "https://www.getfvid.com/");
//            //   cf.setUser_agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
//            cf.setUser_agent(webViewscan.getSettings().getUserAgentString());
//            cf.setCfCallback(new CfCallback() {
//                @Override
//                public void onSuccess(List<HttpCookie> cookieList, boolean hasNewUrl, String newUrl) {
//
//                    webViewscan.loadUrl(newUrl);
//
//
//                }
//
//                @Override
//                public void onFail(int code, String msg) {
//                    //Toast.makeText(TikTokDownloadCloudBypassWebview_method_5.this, "" + msg, Toast.LENGTH_SHORT).show();
//
//
//                }
//            });
//            cf.getCookies();
        } else {
            onstart();
            webViewscan.clearFormData();
            webViewscan.getSettings().setSaveFormData(true);
            //  webViewscan.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
            webViewscan.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
            //   webViewscan.setWebChromeClient(new webChromeClients());
            webViewscan.setWebViewClient(new MyBrowser());
//            webViewscan.getSettings().setAppCacheMaxSize(5242880);
            webViewscan.getSettings().setAllowFileAccess(true);
//            webViewscan.getSettings().setAppCacheEnabled(true);
            webViewscan.getSettings().setJavaScriptEnabled(true);
            webViewscan.getSettings().setDefaultTextEncodingName("UTF-8");
            webViewscan.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webViewscan.getSettings().setDatabaseEnabled(true);
            webViewscan.getSettings().setBuiltInZoomControls(false);
            webViewscan.getSettings().setSupportZoom(false);
            webViewscan.getSettings().setUseWideViewPort(true);
            webViewscan.getSettings().setDomStorageEnabled(true);
            webViewscan.getSettings().setAllowFileAccess(true);
            webViewscan.getSettings().setLoadWithOverviewMode(true);
            webViewscan.getSettings().setLoadsImagesAutomatically(true);
            webViewscan.getSettings().setBlockNetworkImage(false);
            webViewscan.getSettings().setBlockNetworkLoads(false);
            webViewscan.getSettings().setLoadWithOverviewMode(true);
            webViewscan.setWebChromeClient(new WebChromeClient() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    request.grant(request.getResources());
                }
            });

            webViewscan.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimetype,
                                            long contentLength) {

//                    String nametitle = "Facebook_" +
//                            System.currentTimeMillis();
//
//                    new downloadFile().Downloading(FacebookDownloadCloudBypassWebview_method_1.this, url, nametitle, ".mp4");
//

                }
            });

            webViewscan.setWebChromeClient(new WebChromeClient() {

                public void onProgressChanged(WebView view, int progress) {
                    if (progress < 100 && progressBar.getVisibility() == View.GONE) {
                        progressBar.setVisibility(View.VISIBLE);

                    }

                    progressBar.setProgress(progress);
                    if (progress == 100) {
                        progressBar.setVisibility(View.GONE);

                    }
                }
            });
            webViewscan.loadUrl(mWebSiteUrl);
//
//            Cloudflare cf = new Cloudflare(FacebookDownloadCloudBypassWebview_method_1.this, "https://www.getfvid.com/");
//            //   cf.setUser_agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
//            cf.setUser_agent(webViewscan.getSettings().getUserAgentString());
//            cf.setCfCallback(new CfCallback() {
//                @Override
//                public void onSuccess(List<HttpCookie> cookieList, boolean hasNewUrl, String newUrl) {
//
//                    webViewscan.loadUrl(newUrl);
//
//
//                }
//
//                @Override
//                public void onFail(int code, String msg) {
//                    //   Toast.makeText(TikTokDownloadCloudBypassWebview_method_5.this, "" + msg, Toast.LENGTH_SHORT).show();
//
//
//                }
//            });
//            cf.getCookies();
        }
    }

    public void onstart() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.EXPAND_STATUS_BAR"}, 123);
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 1001 && Build.VERSION.SDK_INT >= 21) {
            mUploadMessageArr.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(i2, intent));
            mUploadMessageArr = null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean z = true;
        if (keyCode == 4) {
            try {
                if (webViewscan.canGoBack()) {
                    webViewscan.goBack();
                    return z;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finish();
        z = super.onKeyDown(keyCode, event);
        return z;
    }

    @SuppressLint({"WrongConstant"})
    @RequiresApi(api = 21)
    public void onBackPressed() {
        if (this.doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.presssavemasterdown_again), Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    protected void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        webViewscan.clearCache(true);
    }

    public void onDestroy() {
        super.onDestroy();
        webViewscan.clearCache(true);
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        webViewscan.clearCache(true);
        super.onStop();
    }

    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @SuppressLint({"HandlerLeak"})
    private void InitHandler() {
        handler = new btnInitHandlerListner();
    }

    @SuppressLint("HandlerLeak")
    private class btnInitHandlerListner extends Handler {
        @SuppressLint({"SetTextI18n"})
        public void handleMessage(Message msg) {
        }
    }

    private class webChromeClients extends WebChromeClient {
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.e("CustomClient", consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }
    }

    private class MyBrowser extends WebViewClient {
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
            Log.e(TAG, "progressBar");
            super.onPageStarted(view, url, favicon);
        }

        public boolean shouldOverrideUrlLoading(WebView view, String request) {
            view.loadUrl(request);
            return true;
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e(TAG, "progressBar GONE");
            progressBar.setVisibility(View.GONE);


//            String test = "document.getElementsByName('url')[0]";
//
//            view.evaluateJavascript(test, new ValueCallback() {
//                public void onReceiveValue(Object obj) {
//                    Log.e(TAG, "progressBar reciveing data " + obj.toString());
//
//
//                }
//            });


            String jsscript = "javascript:(function() { "

                    + "document.getElementsByName('sf_url')[0].value ='" + myvidintenturlis + "';"
                    + "document.getElementById('sf_submit').click();"
                    //    + "await new Promise(resolve => setTimeout(resolve, 3000)); "
                    //  + "javascript:document.getElementsByClassName(\"pure-button pure-button-primary is-center u-bl dl-button download_link without_watermark_direct\").click(); "
                    + "})();";

            view.evaluateJavascript(jsscript, new ValueCallback() {
                public void onReceiveValue(Object obj) {
                    Log.e(TAG, "progressBar reciveing data " + obj.toString());


                }
            });
            try {


                Handler handler1 = new Handler();

                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Log.e(TAG, "progressBar reciveing data executed 1");


                        //    webViewscan.loadUrl("javascript:window.HTMLOUT.showHTML('" + url + "',''+document.getElementsByTagName('audio')[0].getAttribute(\"src\"));");

//                            String test = "javascript:document.getElementsByClassName('link link-download subname ga_track_events download-icon')[0]";
//                            Log.e(TAG, "progressBar 1111111111 " + test);
//                            view.evaluateJavascript(test, new ValueCallback() {
//                                public void onReceiveValue(Object obj) {
//                                    Log.e(TAG, "progressBar 1111111111 " + obj.toString());
//
//
//                                }
//                            });


                        view.evaluateJavascript("javascript:document.getElementsByClassName('link link-download subname ga_track_events download-icon')[0].getAttribute('href')", new ValueCallback() {
                            public void onReceiveValue(Object obj) {
                                Log.e(TAG, "progressBar reciveing data download " + obj.toString());
                                if (obj.toString() != null && obj.toString().contains("https://")) {
                                    Log.e(TAG, "progressBar reciveing data http " + obj.toString());

                                    handler1.removeCallbacksAndMessages(null);
//                                    String url = "https://snapinsta.app" + obj.toString();

                                    if (!isdownloadstarted) {
                                        Downloader.getInstance(webdownloader.this).downloadByUrl(obj.toString(), Downloader.TU_TYPE);
                                        isdownloadstarted = true;
                                    }

                                    //  startActivity(new Intent(TikTokDownloadWebview.this,MainActivity.class));
                                    finish();
                                }


                            }
                        });

                        handler1.postDelayed(this, 2000);

                    }
                }, 2000);


            } catch (Exception e) {

                finish();
            }


        }
    }


//        public void a(String str, int i) {
//            String str2 = "";
//            if (i == 1) {
//                str2 = "https://snaptik.app/";
//                i = "function get_snaptik(url){var input=document.querySelector('input');if(input){input.value=url;var button=document.querySelector('button[type=\"submit\"]');if(button){button.click();execute()}}}var phxVideoIsFound=false;var phxVideoSpiderTicks=0;var phxTimerId=-1;function execute(){if(phxVideoIsFound||phxVideoSpiderTicks>=15){clearTimeout(phxTimerId);phxTimerId=-1;phxVideoIsFound=false;phxVideoSpiderTicks=0;return}phxVideoSpiderTicks++;phxTimerId=setTimeout(\"getData()\",1000)}function getData(){var data={};var abuttons=document.querySelector('.abuttons');if(abuttons){var array=abuttons.querySelectorAll('a');for(var i=0;i<array.length;i++){if(array[i]){phxVideoIsFound=true;console.log('url==='+array[i].href);data.video_no_mark_url=array[i].href}}}if(phxVideoIsFound){phoenix.OnVideoFound(JSON.stringify(data))}if(!phxVideoIsFound){execute()}}";
//            } else if (i == 2) {
//                str2 = "https://ssstik.io/";
//                i = "function get_snaptik(url){var input=document.querySelector('#main_page_text');if(input){input.value=url;var button=document.querySelector('#submit');if(button){button.click();execute()}}}var phxVideoIsFound=false;var phxVideoSpiderTicks=0;var phxTimerId=-1;function execute(){if(phxVideoIsFound||phxVideoSpiderTicks>=15){clearTimeout(phxTimerId);phxTimerId=-1;phxVideoIsFound=false;phxVideoSpiderTicks=0;return}phxVideoSpiderTicks++;phxTimerId=setTimeout(\"getData()\",1000)}function getData(){var result_overlay=document.querySelector('.result');if(result_overlay){var data={};var array=result_overlay.querySelectorAll('a');for(var i=0;i<array.length;i++){var cls=array[i].className;if(cls.indexOf('without_watermark_direct')>0){data.video_no_mark_url_2=array[i].href;phxVideoIsFound=true}else if(cls.lastIndexOf('without_watermark')>0){data.video_no_mark_url=array[i].href;phxVideoIsFound=true}else if(cls.indexOf('music')>0){data.mp3_url=array[i].href}}if(phxVideoIsFound){phoenix.OnVideoFound(JSON.stringify(data))}}if(!phxVideoIsFound){execute()}}";
//            } else if (i == 3) {
//                str2 = "https://ttdownloader.com/";
//                i = "function get_snaptik(url){var input=document.querySelector('input');if(input){input.value=url;var button=document.querySelector('button[id=\"submit\"]');if(button){button.click();execute()}}}var phxVideoIsFound=false;var phxVideoSpiderTicks=0;var phxTimerId=-1;function execute(){if(phxVideoIsFound||phxVideoSpiderTicks>=15){clearTimeout(phxTimerId);phxTimerId=-1;phxVideoIsFound=false;phxVideoSpiderTicks=0;return}phxVideoSpiderTicks++;phxTimerId=setTimeout(\"getData()\",1000)}function getData(){var data={};var list=document.querySelector('.results-list');if(list){var a=list.querySelector('a');if(a){phxVideoIsFound=true;data.video_no_mark_url=a.href}}if(phxVideoIsFound){phoenix.OnVideoFound(JSON.stringify(data))}if(!phxVideoIsFound){execute()}}";
//            } else if (i == 4) {
//                str2 = "https://musicaldown.com/";
//                i = "function get_snaptik(url){var input=document.querySelector('input');if(input){input.value=url;var button=document.querySelector('button[type=\"submit\"]');if(button){button.click();execute()}}else{execute()}}var phxVideoIsFound=false;var phxVideoSpiderTicks=0;var phxTimerId=-1;function execute(){if(phxVideoIsFound||phxVideoSpiderTicks>=15){clearTimeout(phxTimerId);phxTimerId=-1;phxVideoIsFound=false;phxVideoSpiderTicks=0;return}phxVideoSpiderTicks++;phxTimerId=setTimeout(\"getData()\",1000)}function getData(){var data={};var array=document.querySelectorAll('a');for(var i=0;i<array.length;i++){if(array[i]){var url=array[i].href;if(url.indexOf('v1.musicallydown.com')>0){data.video_no_mark_url=url;phxVideoIsFound=true}}}var a=document.querySelector('a[rel=\"noreferrer\"]');if(a&&data.video_no_mark_url==undefined){phxVideoIsFound=true;data.video_no_mark_url=a.href}if(phxVideoIsFound){phoenix.OnVideoFound(JSON.stringify(data))}if(!phxVideoIsFound){execute()}}";
//            } else {
//                i = str2;
//            }
//            this.g = new d(str2, i);
//            a(str);
//        }


}
