package com.savemaster.smlib;

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

import com.savemaster.smlib.R;

public class MyInsActivity extends BaseWebActivity {

    public String target_url = "https://www.instagram.com/";
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

                progress_bar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadResource(WebView view, String url)
            {
                webView.loadUrl(INS_SCRIPT);
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
                    showPopDialog(vidData, Downloader.INS_TYPE);
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Download Failed: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static class Format {

        public enum VCodec {
            H263, H264, MPEG4, VP8, VP9, NONE
        }

        public enum ACodec {
            MP3, AAC, VORBIS, OPUS, NONE
        }

        private int itag;
        private String ext;
        private int height;
    //    private int fps;
    //    private VCodec vCodec;
    //    private ACodec aCodec;
        private int audioBitrate;
        private boolean isDashContainer;
    //    private boolean isHlsContent;

    //    Format(int itag, String ext, int height, boolean isDashContainer) {
    //        this.itag = itag;
    //        this.ext = ext;
    //        this.height = height;
    ////        this.fps = 30;
    //        this.audioBitrate = -1;
    //        this.isDashContainer = isDashContainer;
    ////        this.isHlsContent = false;
    //    }

    //    Format(int itag, String ext, VCodec vCodec, ACodec aCodec, int audioBitrate, boolean isDashContainer) {
    //        this.itag = itag;
    //        this.ext = ext;
    //        this.height = -1;
    //        this.fps = 30;
    //        this.audioBitrate = audioBitrate;
    //        this.isDashContainer = isDashContainer;
    //        this.isHlsContent = false;
    //    }

        //dashContainer true: 无音频
        public Format(int itag, String ext, int height, int audioBitrate,
                      int dashContainer) {
            this.itag = itag;
            this.ext = ext;
            this.height = height;
    //        this.fps = 30;
            this.audioBitrate = audioBitrate;
            if(dashContainer == 1){
                this.isDashContainer = true;
            } else {
                this.isDashContainer = false;
            }
        }

    //    Format(int itag, String ext, int height, VCodec vCodec, ACodec aCodec, int audioBitrate,
    //           boolean isDashContainer, boolean isHlsContent) {
    //        this.itag = itag;
    //        this.ext = ext;
    //        this.height = height;
    ////        this.fps = 30;
    //        this.audioBitrate = audioBitrate;
    //        this.isDashContainer = isDashContainer;
    ////        this.isHlsContent = isHlsContent;
    //    }

    //    Format(int itag, String ext, int height, boolean isDashContainer) {
    //        this.itag = itag;
    //        this.ext = ext;
    //        this.height = height;
    //        this.audioBitrate = -1;
    ////        this.fps = fps;
    //        this.isDashContainer = isDashContainer;
    //        this.isHlsContent = false;
    //    }

    //    /**
    //     * Get the frames per second
    //     */
    //    public int getFps() {
    //        return fps;
    //    }

        /**
         * Audio bitrate in kbit/s or -1 if there is no audio track.
         */
        public int getAudioBitrate() {
            return audioBitrate;
        }

        /**
         * An identifier used by youtube for different formats.
         */
        public int getItag() {
            return itag;
        }

        /**
         * The file extension and conainer format like "mp4"
         */
        public String getExt() {
            return ext;
        }

        public boolean isDashContainer() {
            return isDashContainer;
        }

    //    public ACodec getAudioCodec() {
    //        return aCodec;
    //    }
    //
    //    public VCodec getVideoCodec() {
    //        return vCodec;
    //    }

    //    public boolean isHlsContent() {
    //        return isHlsContent;
    //    }

        /**
         * The pixel height of the video stream or -1 for audio files.
         */
        public int getHeight() {
            return height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Format format = (Format) o;

            if (itag != format.itag) return false;
            if (height != format.height) return false;
    //        if (fps != format.fps) return false;
            if (audioBitrate != format.audioBitrate) return false;
            if (isDashContainer != format.isDashContainer) return false;
    //        if (isHlsContent != format.isHlsContent) return false;
            if (ext != null ? !ext.equals(format.ext) : format.ext != null) return false;
    //        if (vCodec != format.vCodec) return false;
    //        return aCodec == format.aCodec;
            return true;
        }

        @Override
        public int hashCode() {
            int result = itag;
            result = 31 * result + (ext != null ? ext.hashCode() : 0);
            result = 31 * result + height;
    //        result = 31 * result + fps;
    //        result = 31 * result + (vCodec != null ? vCodec.hashCode() : 0);
    //        result = 31 * result + (aCodec != null ? aCodec.hashCode() : 0);
            result = 31 * result + audioBitrate;
            result = 31 * result + (isDashContainer ? 1 : 0);
    //        result = 31 * result + (isHlsContent ? 1 : 0);
            return result;
        }

    }
}
