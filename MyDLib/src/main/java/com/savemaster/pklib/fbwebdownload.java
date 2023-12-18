package com.savemaster.pklib;

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

import com.savemaster.smlib.Downloader;
import com.savemaster.smlib.R;

public class fbwebdownload extends AppCompatActivity {


    public static Handler handler;
    static String myvidintenturlis = "";
    private static ValueCallback<Uri[]> mUploadMessageArr;
    String TAG = "whatsapptag";
    boolean doubleBackToExitPressedOnce = false;
    boolean isdownloadstarted = false;
    ProgressDialog progressDialog;
//    private ActivityTikTokDownloadWebviewBinding binding;

    String openUrl = "https://www.getfvid.com/";
    WebView webViewscan;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        binding = ActivityTikTokDownloadWebviewBinding.inflate(getLayoutInflater());
//        View view = binding.getRoot();
//        setContentView(view);
        setContentView(R.layout.savemasterdown_downd_webview);

        setSupportActionBar(findViewById(R.id.tool12));
        InitHandler();

        try {
            progressDialog = new ProgressDialog(fbwebdownload.this);
            progressDialog.setMessage(getString(R.string.nodesavemasterdown_ifittakeslonger));
            progressDialog.show();
        } catch (Exception e) {

        }
        if (getIntent().getStringExtra("myvidurl") != null && !getIntent().getStringExtra("myvidurl").equals("")) {
            myvidintenturlis = getIntent().getStringExtra("myvidurl");
        }
//        binding.opentiktok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.getfvid.com/"));
//
//                    intent.setPackage("com.zhiliaoapp.musically");
//
//                    startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    iUtils.ShowToast(FacebookDownloadCloudBypassWebview_method_1.this, "Tiktok not Installed");
//                }
//            }
//        });
        webViewscan = findViewById(R.id.webViewscan);
        progressBar = findViewById(R.id.progressBar);

        if (Build.VERSION.SDK_INT >= 24) {
            onstart();
            webViewscan.clearFormData();
            webViewscan.getSettings().setSaveFormData(true);
            // binding.webViewscan.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
            webViewscan.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
            // binding.webViewscan.setWebChromeClient(new webChromeClients());
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


            webViewscan.loadUrl(openUrl);

        } else {
            onstart();
            webViewscan.clearFormData();
            webViewscan.getSettings().setSaveFormData(true);
            //  binding.webViewscan.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
            webViewscan.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
            //   binding.webViewscan.setWebChromeClient(new webChromeClients());
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
//                    new downloadFile().Downloading(FacebookDownloadCloudBypassWebview_method_1.this, url, nametitle, ".mp4");

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
            webViewscan.loadUrl(openUrl);
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
            Log.e(TAG, "binding.progressBar");
            super.onPageStarted(view, url, favicon);
        }

        public boolean shouldOverrideUrlLoading(WebView view, String request) {
            view.loadUrl(request);
            return true;
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e(TAG, "binding.progressBar GONE");
            progressBar.setVisibility(View.GONE);


            String jsscript = "javascript:(function() { "

                    + "document.getElementsByClassName('form-control input-md ht53')[0].value ='" + myvidintenturlis + "';"
                    + "document.getElementById('btn_submit').click();"
                    //    + "await new Promise(resolve => setTimeout(resolve, 3000)); "
                    //  + "javascript:document.getElementsByClassName(\"pure-button pure-button-primary is-center u-bl dl-button download_link without_watermark_direct\").click(); "
                    + "})();";

            view.evaluateJavascript(jsscript, new ValueCallback() {
                public void onReceiveValue(Object obj) {
                    Log.e(TAG, "binding.progressBar reciveing data " + obj.toString());


                }
            });
            try {


                Handler handler1 = new Handler();

                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Log.e(TAG, "binding.progressBar reciveing data executed 1");


                        //    binding.webViewscan.loadUrl("javascript:window.HTMLOUT.showHTML('" + url + "',''+document.getElementsByTagName('audio')[0].getAttribute(\"src\"));");


                        view.evaluateJavascript("javascript:document.getElementsByTagName('a')[11].getAttribute('href')", new ValueCallback() {
                            public void onReceiveValue(Object obj) {
                                Log.e(TAG, "binding.progressBar reciveing data download " + obj.toString());
                                if (obj.toString() != null && obj.toString().contains("http") && !obj.toString().contains("getfvid.com")) {
                                    Log.e(TAG, "binding.progressBar reciveing data http " + obj.toString());

                                    handler1.removeCallbacksAndMessages(null);

                                    if (!isdownloadstarted) {
                                        Downloader.getInstance(fbwebdownload.this).downloadByUrl(obj.toString(), Downloader.F_TYPE);
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
}
