package com.savemaster.smlib;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.savemaster.smlib.R;


public class SavefbActivity extends AppCompatActivity {

    SavefbActivity activity;
    private String cookies;
    WebView webView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        setContentView(R.layout.savemasterdown_download_login);
        activity = this;

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> loadPage());

        webView = findViewById(R.id.webView);

        loadPage();
    }

    public void loadPage() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearCache(true);
        CookieSyncManager.createInstance(activity);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
//        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.addJavascriptInterface(activity, "Android");
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
            webSettings.setMixedContentMode(2);
        }
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        webView.setWebViewClient(new MyBrowser());

        webView.loadUrl("https://www.facebook.com/");
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            if (Build.VERSION.SDK_INT >= 21) {
                webView.loadUrl(webResourceRequest.getUrl().toString());
                cookies = CookieManager.getInstance().getCookie(webResourceRequest.getUrl().toString());
                if (!MyUtils.isNullOrEmpty(cookies) && cookies.contains("c_user")) {
                    ASharePreferenceUtils.putString(SavefbActivity.this, ASharePreferenceUtils.FBCOOKIES, cookies);
                }
            }
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            cookies = CookieManager.getInstance().getCookie(url);
            if (!MyUtils.isNullOrEmpty(cookies) && cookies.contains("c_user")) {
                ASharePreferenceUtils.putString(SavefbActivity.this, ASharePreferenceUtils.FBCOOKIES, cookies);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView webView, String str) {
            super.onPageFinished(webView, str);
            cookies = CookieManager.getInstance().getCookie(str);
            webView.loadUrl("javascript:Android.resultOnFinish();");
            webView.loadUrl("javascript:var el = document.querySelectorAll('input[name=fb_dtsg]');Android.resultOnFinish(el[0].value);");
        }
    }

    @JavascriptInterface
    public void resultOnFinish(String key) {
        if (key.length() < 15) {
            return;
        }
        try {
            if (!MyUtils.isNullOrEmpty(cookies) && cookies.contains("c_user")) {
                ASharePreferenceUtils.putString(activity, ASharePreferenceUtils.FBKEY, key);
                ASharePreferenceUtils.putBoolean(activity, ASharePreferenceUtils.ISFBLOGIN, true);
                System.out.println("Key - " + key);
                Intent intent = new Intent();
                intent.putExtra("result", "result");
                setResult(RESULT_OK, intent);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}