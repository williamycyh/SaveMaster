package com.savemaster.smlib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.savemaster.smlib.R;

public class MySaveLoginActivity extends AppCompatActivity {

    public static void startMe(Activity activity){
        Intent intent = new Intent();
        intent.setClass(activity, MySaveLoginActivity.class);
        activity.startActivity(intent);
    }

    String mURL = "https://www.instagram.com/accounts/login/";
    WebView mWebView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savemasterdown_download_login_ins);

        mWebView = findViewById(R.id.webview_login);
        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.addJavascriptInterface(new MyBridge(InsLoginActivity.this), "bridge");
        startWebView();

    }

    private Boolean isSessionid = false;
    private String username = "";
    private void startWebView() {

        mWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e("InstagramLogin", "shouldOverrideUrlLoading: "+  url);
                if(url.equalsIgnoreCase("https://instagram.com/")){
                    return  true;
                }else {
                    return false;
                }
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);


                if (isSessionid ) {
                    setResult(RESULT_OK);
                    finish();

                }

            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);

                if(Downloader.getInstance(MySaveLoginActivity.this).cookieOK()){
                    isSessionid = true;
                }


            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                ToastUtils.ErrorToast(InsLoginActivity.this, description);
                Toast.makeText(MySaveLoginActivity.this, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }


        });

        mWebView.loadUrl(mURL);

    }

}
