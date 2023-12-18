package com.savemaster.smlib;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

//import com.videodownloader.sharedPre.UIConfigManager;
//import com.videodownloader.utils.Commons;
//import com.videodownloader.utils.MyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Extractor extends AsyncTask<String, Void, SparseArray<YFile>> {

    static boolean CACHING = true;
    static boolean LOGGING = false;

    private final static String LOG_TAG = "FBExtractor";
    private final static String CACHE_FILE_NAME = "decipher_js_funct";

    private final WeakReference<Context> refContext;
    private String videoID;
    private VideoMeta videoMeta;
    private final String cacheDirPath;

    private volatile String decipheredSignature;

    private static String decipherJsFileName;
    private static String decipherFunctions;
    private static String decipherFunctionName;

    private final Lock lock = new ReentrantLock();
    private final Condition jsExecuting = lock.newCondition();

    public static final String USER_AGENT_MAC = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";
    public static final String USER_AGENT_WIN = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3704.8 Safari/537.36";

    private static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";

    private Pattern patYouTubePageLink = null;
    private Pattern patYouTubeShortLink = null;

    private Pattern patVariableFunction = null;
    private Pattern patFunction = null;

    private Pattern patDecryptionJsFile = null;
    private static final Pattern patSignatureDecFunction = Pattern.compile("(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{1,4})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)");
    private static  Pattern patDecryptionJsFileWithoutSlash; // = Pattern.compile("/s/player/([^\"]+?).js");
    private static  Pattern patPlayerResponse; // = Pattern.compile("var ytInitialPlayerResponse\\s*=\\s*(\\{.+?\\})\\s*;");
    private static  Pattern patSigEncUrl; // = Pattern.compile("url=(.+?)(\\u0026|$)");
    private static  Pattern patSignature; // = Pattern.compile("s=(.+?)(\\u0026|$)");

    StringBuilder exceptionBuilder = new StringBuilder();

    private static final SparseArray<MyInsActivity.Format> FORMAT_MAP = new SparseArray<>();

    private String decodeStr(String encodeStr){
        return BaseCommon.decodeToString(encodeStr);
    }

    public MyInsActivity.Format getFormat(String encode){

        String[] list = encode.split(",");
        MyInsActivity.Format format = null;
        try{
            format = new MyInsActivity.Format(Integer.valueOf(list[0].trim()),list[1].trim(),Integer.valueOf(list[2].trim()), Integer.valueOf(list[3].trim()),Integer.valueOf(list[4].trim()));
        }catch (NumberFormatException e){
        }

        return format;
    }

    public void initFormat(String encodeStr){
        MyInsActivity.Format format = getFormat(BaseCommon.decodeToString(encodeStr));
        if(format != null){
            FORMAT_MAP.put(format.getItag(), format);
        }
    }

    public void init(){
        patDecryptionJsFileWithoutSlash = Pattern.compile(decodeStr("L3MvcGxheWVyLyhbXiJdKz8pLmpz"));
        patPlayerResponse = Pattern.compile( decodeStr("dmFyIHl0SW5pdGlhbFBsYXllclJlc3BvbnNlXHMqPVxzKihcey4rP1x9KVxzKjs="));
        patSigEncUrl = Pattern.compile(decodeStr("dXJsPSguKz8pKFx1MDAyNnwkKQ=="));
        patSignature = Pattern.compile(decodeStr("cz0oLis/KShcdTAwMjZ8JCk="));

        patYouTubePageLink = Pattern.compile(decodeStr("KGh0dHB8aHR0cHMpOi8vKHd3d1wufG0ufCl5b3V0dWJlXC5jb20vd2F0Y2hcP3Y9KC4rPykoIHxcenwmKQ=="));//(http|https)://(www\\.|m.|)youtube\\.com/watch\\?v=(.+?)( |\\z|&)
        patYouTubeShortLink = Pattern.compile(decodeStr("KGh0dHB8aHR0cHMpOi8vKHd3d1wufCl5b3V0dS5iZS8oLis/KSggfFx6fCYp"));//(http|https)://(www\\.|)youtu.be/(.+?)( |\\z|&)
//        patTitle = Pattern.compile(decodeStr("InRpdGxlIlxzKjpccyoiKC4qPyki"));//\"title\"\\s*:\\s*\"(.*?)\"
//        patStatusOk = Pattern.compile(decodeStr("c3RhdHVzPW9rKCZ8LHxceik"));//status=ok(&|,|\z)
//        patHlsvp = Pattern.compile(decodeStr("aGxzdnA9KC4rPykoJnxceik="));//hlsvp=(.+?)(&|\z)
//        patHlsItag = Pattern.compile(decodeStr("L2l0YWcvKFxkKz8pLw=="));// /itag/(\d+?)/
//        patItag = Pattern.compile(decodeStr("aXRhZz0oWzAtOV0rPykoJnxceik="));//itag=([0-9]+?)(&|\z)
//        patEncSig = Pattern.compile(decodeStr("cz0oLnsxMCx9PykoXFxcXHUwMDI2fFx6KQ=="));//s=(.{10,}?)(\\\\u0026|\z)
//        patUrl = Pattern.compile(decodeStr("InVybCJccyo6XHMqIiguKz8pIg=="));//"url"\s*:\s*"(.+?)"
//        patCipher = Pattern.compile(decodeStr("InNpZ25hdHVyZUNpcGhlciJccyo6XHMqIiguKz8pIg=="));//"signatureCipher"\s*:\s*"(.+?)"
//        patCipherUrl = Pattern.compile(decodeStr("dXJsPSguKz8pKFxcXFx1MDAyNnxceik="));//url=(.+?)(\\\\u0026|\z)
        patVariableFunction = Pattern.compile(decodeStr("KFt7OyA9XSkoW2EtekEtWiRdW2EtekEtWjAtOSRdezAsMn0pXC4oW2EtekEtWiRdW2EtekEtWjAtOSRdezAsMn0pXCg="));//([{; =])([a-zA-Z$][a-zA-Z0-9$]{0,2})\.([a-zA-Z$][a-zA-Z0-9$]{0,2})\(
        patFunction = Pattern.compile(decodeStr("KFt7OyA9XSkoW2EtekEtWiRfXVthLXpBLVowLTkkXXswLDJ9KVwo"));//([{; =])([a-zA-Z$_][a-zA-Z0-9$]{0,2})\(
        patDecryptionJsFile = Pattern.compile(decodeStr("XFwvc1xcL3BsYXllclxcLyhbXiJdKz8pXC5qcw=="));//\\/s\\/player\\/([^"]+?)\.js
//        patDecryptionJsFile01 = Pattern.compile(decodeStr("L3MvcGxheWVyLyhbXlwiXSs/KS5qcw=="));
//        patSignatureDecFunction = Pattern.compile(decodeStr("KD86XGJ8W15hLXpBLVowLTkkXSkoW2EtekEtWjAtOSRdezEsNH0pXHMqPVxzKmZ1bmN0aW9uXChccyphXHMqXClccypce1xzKmFccyo9XHMqYVwuc3BsaXRcKFxzKiIiXHMqXCk="));


        // Video and Audio

//        FORMAT_MAP.put(17, new Format(17, "3gp", 144,24, 0));
        initFormat("MTcsM2dwLDE0NCwyNCww");
//        FORMAT_MAP.put(36, new Format(36, "3gp", 240,32, 0));
        initFormat("MzYsM2dwLDI0MCwzMiww");
//        FORMAT_MAP.put(5, new Format(5, "flv", 240, 64, 0));
        initFormat("NSxmbHYsMjQwLDY0LDA=");
//        FORMAT_MAP.put(43, new Format(43, "webm", 360,128, 0));
        initFormat("NDMsd2VibSwzNjAsMTI4LDA=");
//        FORMAT_MAP.put(22, new Format(22, "mp4", 720, 192, 0));
        initFormat("MjIsbXA0LDcyMCwxOTIsMA==");
//        FORMAT_MAP.put(18, new Format(18, "mp4", 360, 96, 0));
        initFormat("MTgsbXA0LDM2MCw5Niww");

        initFormat("NixmbHYsMjcwLDY0LDA=");//6,flv,270,64,0
        initFormat("MzQsM2dwLDM2MCwxMjgsMA==");//34,3gp,360,128,0
        initFormat("MzUsZmx2LDQ4MCwxMjgsMA==");//35,flv,480,128,0
        initFormat("MzYsM2dwLDI0MCwzMiww");//36,3gp,240,32,0
        initFormat("MzcsbXA0LDEwODAsMTkyLDA=");//37,mp4,1080,192,0
        initFormat("MzgsbXA0LDMwNzIsMTkyLDA=");//38,mp4,3072,192,0
        initFormat("NDQsd2VibSw0ODAsMTI4LDA=");//44,webm,480,128,0
        initFormat("NDUsd2VibSw3MjAsMTkyLDA=");//45,webm,720,192,0
        initFormat("NDYsd2VibSwxMDgwLDE5Miww");//46,webm,1080,192,0
        initFormat("NTksbXA0LDQ4MCwxMjgsMA==");//59,mp4,480,128,0
        initFormat("NzgsbXA0LDQ4MCwxMjgsMA==");//78,mp4,480,128,0


        // Dash Audio
//        FORMAT_MAP.put(140, new Format(140, "m4a", -1, 128, 1));
        initFormat("MTQwLG00YSwtMSwxMjgsMQ==");
//        FORMAT_MAP.put(256, new Format(256, "m4a", -1, 192, 1));
        initFormat("MjU2LG00YSwtMSwxOTIsMQ==");
//        FORMAT_MAP.put(141, new Format(141, "m4a", -1, 256, 1));
        initFormat("MTQxLG00YSwtMSwyNTYsMQ==");
//        FORMAT_MAP.put(258, new Format(258, "m4a", -1, 384, 1));
        initFormat("MjU4LG00YSwtMSwzODQsMQ==");

        // Dash Video
//        FORMAT_MAP.put(160, new Format(160, "mp4", 144, 0,1));
        initFormat("MTYwLG1wNCwxNDQsMCwx");
//        FORMAT_MAP.put(133, new Format(133, "mp4", 240, 0,1));
        initFormat("MTMzLG1wNCwyNDAsMCwx");
//        FORMAT_MAP.put(134, new Format(134, "mp4", 360,  0,1));
        initFormat("MTM0LG1wNCwzNjAsMCwx");
//        FORMAT_MAP.put(135, new Format(135, "mp4", 480, 0,1));
        initFormat("MTM1LG1wNCw0ODAsMCwx");
        //397,mp4,640,0,1
//        initFormat("397,mp4,480,0,1");
        //396,mp4,640,0,1
//        initFormat("396,mp4,480,0,1");
        //395,mp4,640,0,1
//        initFormat("395,mp4,240,0,1");
//        FORMAT_MAP.put(136, new Format(136, "mp4", 720, 0,1));
        initFormat("MTM2LG1wNCw3MjAsMCwx");
//        FORMAT_MAP.put(137, new Format(137, "mp4", 1080, 0,1));
        initFormat("MTM3LG1wNCwxMDgwLDAsMQ==");
//        FORMAT_MAP.put(264, new Format(264, "mp4", 1440, 0,1));
        initFormat("MjY0LG1wNCwxNDQwLDAsMQ==");
//        FORMAT_MAP.put(266, new Format(266, "mp4", 2160, 0,1));
        initFormat("MjY2LG1wNCwyMTYwLDAsMQ==");

//        FORMAT_MAP.put(298, new Format(298, "mp4", 720, 0,1));
        initFormat("Mjk4LG1wNCw3MjAsMCwx");
//        FORMAT_MAP.put(299, new Format(299, "mp4", 1080, 0,1));
        initFormat("Mjk5LG1wNCwxMDgwLDAsMQ==");

        // WEBM Dash Video
//        FORMAT_MAP.put(278, new Format(278, "webm", 144, 0,1));
        initFormat("Mjc4LHdlYm0sMTQ0LDAsMQ==");
//        FORMAT_MAP.put(242, new Format(242, "webm", 240, 0,1));
        initFormat("MjQyLHdlYm0sMjQwLDAsMQ==");
//        FORMAT_MAP.put(243, new Format(243, "webm", 360, 0,1));
        initFormat("MjQzLHdlYm0sMzYwLDAsMQ==");
//        FORMAT_MAP.put(244, new Format(244, "webm", 480, 0,1));
        initFormat("MjQ0LHdlYm0sNDgwLDAsMQ==");
//        FORMAT_MAP.put(247, new Format(247, "webm", 720, 0,1));
        initFormat("MjQ3LHdlYm0sNzIwLDAsMQ==");
//        FORMAT_MAP.put(248, new Format(248, "webm", 1080, 0,1));
        initFormat("MjQ4LHdlYm0sMTA4MCwwLDE=");
//        FORMAT_MAP.put(271, new Format(271, "webm", 1440, 0,1));
        initFormat("MjcxLHdlYm0sMTQ0MCwwLDE=");
//        FORMAT_MAP.put(313, new Format(313, "webm", 2160, 0,1));
        initFormat("MzEzLHdlYm0sMjE2MCwwLDE=");

//        FORMAT_MAP.put(302, new Format(302, "webm", 720, 0,1));
        initFormat("MzAyLHdlYm0sNzIwLDAsMQ==");
//        FORMAT_MAP.put(308, new Format(308, "webm", 1440, 0,1));
        initFormat("MzA4LHdlYm0sMTQ0MCwwLDE=");
//        FORMAT_MAP.put(303, new Format(303, "webm", 1080,  0,1));
        initFormat("MzAzLHdlYm0sMTA4MCwwLDE=");
//        FORMAT_MAP.put(315, new Format(315, "webm", 2160, 0,1));
        initFormat("MzE1LHdlYm0sMjE2MCwwLDE=");

        // WEBM Dash Audio
//        FORMAT_MAP.put(171, new Format(171, "webm", -1, 128, 1));
        initFormat("MTcxLHdlYm0sLTEsMTI4LDE=");

//        FORMAT_MAP.put(249, new Format(249, "webm", -1, 48, 1));
        initFormat("MjQ5LHdlYm0sLTEsNDgsMQ==");
//        FORMAT_MAP.put(250, new Format(250, "webm", -1, 64, 1));
        initFormat("MjUwLHdlYm0sLTEsNjQsMQ==");
//        FORMAT_MAP.put(251, new Format(251, "webm", -1, 160, 1));
        initFormat("MjUxLHdlYm0sLTEsMTYwLDE=");

        // HLS Live Stream
//        FORMAT_MAP.put(91, new Format(91, "mp4", 144 , 48, 0));
        initFormat("OTEsbXA0LDE0NCw0OCww");
//        FORMAT_MAP.put(92, new Format(92, "mp4", 240 , 48, 0));
        initFormat("OTIsbXA0LDI0MCw0OCww");
//        FORMAT_MAP.put(93, new Format(93, "mp4", 360 , 128, 0));
        initFormat("OTMsbXA0LDM2MCwxMjgsMA==");
//        FORMAT_MAP.put(94, new Format(94, "mp4", 480 , 128, 0));
        initFormat("OTQsbXA0LDQ4MCwxMjgsMA==");
//        FORMAT_MAP.put(95, new Format(95, "mp4", 720 , 256, 0));
        initFormat("OTUsbXA0LDcyMCwyNTYsMA==");
//        FORMAT_MAP.put(96, new Format(96, "mp4", 1080 , 256, 0));
        initFormat("OTYsbXA0LDEwODAsMjU2LDA=");
    }


    public Extractor(@NonNull Context con) {
        refContext = new WeakReference<>(con);
        cacheDirPath = con.getCacheDir().getAbsolutePath();
    }

    @Override
    protected void onPostExecute(SparseArray<YFile> ytFiles) {
        onExtractionComplete(ytFiles, videoMeta, exceptionBuilder);
//        UIConfigManager.setLastException(exceptionBuilder.toString());
    }


    /**
     * Start the extraction.
     *
     * @param youtubeLink the youtube page link or video id
     */
    public void extract(String youtubeLink, boolean includeWebM, String userAgent) {
//        if(!TextUtils.isEmpty(MyApp.getInstance().getConfig().agent_str)){
//            USER_AGENT = MyApp.getInstance().getConfig().agent_str;
//        } else {
//            USER_AGENT = MyUtils.random_user_agent();
//        }
        if(TextUtils.isEmpty(userAgent)){
            this.USER_AGENT = USER_AGENT_WIN;
        } else {
            this.USER_AGENT = userAgent;
        }
        exceptionBuilder.append(youtubeLink);
        exceptionBuilder.append("\n");

        exceptionBuilder.append(USER_AGENT);
        exceptionBuilder.append("\n");

        exceptionBuilder.append(Build.VERSION.SDK_INT + " " + Build.BRAND);
        exceptionBuilder.append("\n");

//        this.includeWebM = includeWebM;
        this.execute(youtubeLink);
    }

    protected abstract void onExtractionComplete(SparseArray<YFile> yFiles, VideoMeta videoMeta, StringBuilder exceptionBuilder);

    @Override
    protected SparseArray<YFile> doInBackground(String... params) {
        videoID = null;
        String ytUrl = params[0];
        if (ytUrl == null) {
            return null;
        }
        Matcher mat = patYouTubePageLink.matcher(ytUrl);
        if (mat.find()) {
            videoID = mat.group(3);
        } else {
            mat = patYouTubeShortLink.matcher(ytUrl);
            if (mat.find()) {
                videoID = mat.group(3);
            }
        }
        String searchShort = BaseCommon.decodeToString("c2hvcnRzLw==");
        if(TextUtils.isEmpty(videoID) && ytUrl.contains(searchShort)){
            videoID = ytUrl.substring(ytUrl.indexOf(searchShort) + searchShort.length());
        }

        if(TextUtils.isEmpty(videoID) && ytUrl.matches("\\p{Graph}+?")){
            videoID = ytUrl;
        }

        if (videoID != null) {
            try {
                return getStreamUrls();
            } catch (Exception e) {
                e.printStackTrace();
                exceptionBuilder.append(e.getMessage());
                exceptionBuilder.append("\n");
            }
        } else {
            exceptionBuilder.append("Wrong FB link format");
            exceptionBuilder.append("\n");
            Log.e(LOG_TAG, "Wrong FB link format");
        }
        return null;
    }

    private SparseArray<YFile> getStreamUrls() throws IOException, InterruptedException, JSONException {

        String pageHtml;
        SparseArray<String> encSignatures = new SparseArray<>();
        SparseArray<YFile> ytFiles = new SparseArray<>();

        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        URL getUrl = new URL(decodeStr("aHR0cHM6Ly95b3V0dWJlLmNvbS93YXRjaD92PQ==") + videoID);
        try {
            urlConnection = (HttpURLConnection) getUrl.openConnection();
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder sbPageHtml = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sbPageHtml.append(line);
            }
            pageHtml = sbPageHtml.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        String itagStr = decodeStr("aXRhZw==");//"itag"
        String signatureCipher = decodeStr("c2lnbmF0dXJlQ2lwaGVy");//"signatureCipher"
        Matcher mat = patPlayerResponse.matcher(pageHtml);
        if (mat.find()) {
            JSONObject ytPlayerResponse = new JSONObject(mat.group(1));
            JSONObject streamingData = ytPlayerResponse.getJSONObject("streamingData");

            JSONArray formats = streamingData.getJSONArray("formats");
            for (int i = 0; i < formats.length(); i++) {

                JSONObject format = formats.getJSONObject(i);

                // FORMAT_STREAM_TYPE_OTF(otf=1) requires downloading the init fragment (adding
                // `&sq=0` to the URL) and parsing emsg box to determine the number of fragment that
                // would subsequently requested with (`&sq=N`) (cf. youtube-dl)
                String type = format.optString("type");
                if (type != null && type.equals("FORMAT_STREAM_TYPE_OTF"))
                    continue;

                int itag = format.getInt(itagStr);

                if (FORMAT_MAP.get(itag) != null) {
                    if (format.has("url")) {
                        String url = format.getString("url").replace("\\u0026", "&");
                        ytFiles.append(itag, new YFile(FORMAT_MAP.get(itag), url));
                    } else if (format.has(signatureCipher)) {

                        mat = patSigEncUrl.matcher(format.getString(signatureCipher));
                        Matcher matSig = patSignature.matcher(format.getString(signatureCipher));
                        if (mat.find() && matSig.find()) {
                            String url = URLDecoder.decode(mat.group(1), "UTF-8");
                            String signature = URLDecoder.decode(matSig.group(1), "UTF-8");
                            ytFiles.append(itag, new YFile(FORMAT_MAP.get(itag), url));
                            encSignatures.append(itag, signature);
                        }
                    }
                }
            }

            JSONArray adaptiveFormats = streamingData.getJSONArray(decodeStr("YWRhcHRpdmVGb3JtYXRz"));//"adaptiveFormats"
            for (int i = 0; i < adaptiveFormats.length(); i++) {

                JSONObject adaptiveFormat = adaptiveFormats.getJSONObject(i);

                String type = adaptiveFormat.optString("type");
                if (type != null && type.equals("FORMAT_STREAM_TYPE_OTF"))
                    continue;

                int itag = adaptiveFormat.getInt(itagStr);

                if (FORMAT_MAP.get(itag) != null) {
                    if (adaptiveFormat.has("url")) {
                        String url = adaptiveFormat.getString("url").replace("\\u0026", "&");
                        ytFiles.append(itag, new YFile(FORMAT_MAP.get(itag), url));
                    } else if (adaptiveFormat.has(signatureCipher)) {

                        mat = patSigEncUrl.matcher(adaptiveFormat.getString(signatureCipher));
                        Matcher matSig = patSignature.matcher(adaptiveFormat.getString(signatureCipher));
                        if (mat.find() && matSig.find()) {
                            String url = URLDecoder.decode(mat.group(1), "UTF-8");
                            String signature = URLDecoder.decode(matSig.group(1), "UTF-8");
                            ytFiles.append(itag, new YFile(FORMAT_MAP.get(itag), url));
                            encSignatures.append(itag, signature);
                        }
                    }
                }
            }

            JSONObject videoDetails = ytPlayerResponse.getJSONObject("videoDetails");
            this.videoMeta = new VideoMeta(videoDetails.getString("videoId"),
                    videoDetails.getString("title"),
//                    videoDetails.getString("author"),
//                    videoDetails.getString("channelId"),
//                    Long.parseLong(videoDetails.getString("lengthSeconds")),
//                    Long.parseLong(videoDetails.getString("viewCount")),
                    videoDetails.getBoolean("isLiveContent")
//                    videoDetails.getString("shortDescription")
            );

        } else {
            exceptionBuilder.append("ytPlayerResponse was not found");
            exceptionBuilder.append("\n");
            Log.d(LOG_TAG, "ytPlayerResponse was not found");
        }

        if (encSignatures.size() > 0) {

            String curJsFileName;

            if (CACHING
                    && (decipherJsFileName == null || decipherFunctions == null || decipherFunctionName == null)) {
                readDecipherFunctFromCache();
            }

            mat = patDecryptionJsFile.matcher(pageHtml);
            if (!mat.find())
                mat = patDecryptionJsFileWithoutSlash.matcher(pageHtml);
            if (mat.find()) {
                curJsFileName = mat.group(0).replace("\\/", "/");
                if (decipherJsFileName == null || !decipherJsFileName.equals(curJsFileName)) {
                    decipherFunctions = null;
                    decipherFunctionName = null;
                }
                decipherJsFileName = curJsFileName;
            }

            String msg = "Decipher signatures: " + encSignatures.size() + ", videos: " + ytFiles.size();
            if (LOGGING){
                Log.d(LOG_TAG, msg);
            }
            exceptionBuilder.append(msg);
            exceptionBuilder.append("\n");

            String signature;
            decipheredSignature = null;
            if (decipherSignature(encSignatures)) {
                lock.lock();
                try {
                    jsExecuting.await(7, TimeUnit.SECONDS);
                } finally {
                    lock.unlock();
                }
            }
            signature = decipheredSignature;
            if (signature == null) {
                return null;
            } else {
                String[] sigs = signature.split("\n");
                for (int i = 0; i < encSignatures.size() && i < sigs.length; i++) {
                    int key = encSignatures.keyAt(i);
                    String url = ytFiles.get(key).getUrl();
                    url +=  decodeStr("JnNpZz0=") + sigs[i];
                    YFile newFile = new YFile(FORMAT_MAP.get(key), url);
                    ytFiles.put(key, newFile);
                }
            }
        }

        if (ytFiles.size() == 0) {
            if (LOGGING){
                Log.d(LOG_TAG, pageHtml);
            }
            exceptionBuilder.append("ytFiles.size 0");
            exceptionBuilder.append("\n");
            return null;
        } else {
            exceptionBuilder.append("ytFiles.size:" +ytFiles.size());
            exceptionBuilder.append("\n");
        }
        return ytFiles;
    }

    private boolean decipherSignature(final SparseArray<String> encSignatures) throws IOException {
        // Assume the functions don't change that much
        if (decipherFunctionName == null || decipherFunctions == null) {
            String decipherFunctUrl = BaseCommon.decodeToString("aHR0cHM6Ly95b3V0dWJlLmNvbQ==") + decipherJsFileName;

            BufferedReader reader = null;
            String javascriptFile;
            URL url = new URL(decipherFunctUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            try {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append(" ");
                }
                javascriptFile = sb.toString();
            } finally {
                if (reader != null)
                    reader.close();
                urlConnection.disconnect();
            }

            if (LOGGING){
                Log.d(LOG_TAG, "FunctURL: " + decipherFunctUrl);
            }
            exceptionBuilder.append("FunctURL: " + decipherFunctUrl);
            exceptionBuilder.append("\n");

            Matcher mat = patSignatureDecFunction.matcher(javascriptFile);
            if (mat.find()) {
                decipherFunctionName = mat.group(1);
                if (LOGGING)
                    Log.d(LOG_TAG, "Functname: " + decipherFunctionName);

                Pattern patMainVariable = Pattern.compile("(var |\\s|,|;)" + decipherFunctionName.replace("$", "\\$") +
                        "(=function\\((.{1,3})\\)\\{)");

                String mainDecipherFunct;

                mat = patMainVariable.matcher(javascriptFile);
                if (mat.find()) {
                    mainDecipherFunct = "var " + decipherFunctionName + mat.group(2);
                } else {
                    Pattern patMainFunction = Pattern.compile("function " + decipherFunctionName.replace("$", "\\$") +
                            "(\\((.{1,3})\\)\\{)");
                    mat = patMainFunction.matcher(javascriptFile);
                    if (!mat.find())
                        return false;
                    mainDecipherFunct = "function " + decipherFunctionName + mat.group(2);
                }

                int startIndex = mat.end();

                for (int braces = 1, i = startIndex; i < javascriptFile.length(); i++) {
                    if (braces == 0 && startIndex + 5 < i) {
                        mainDecipherFunct += javascriptFile.substring(startIndex, i) + ";";
                        break;
                    }
                    if (javascriptFile.charAt(i) == '{')
                        braces++;
                    else if (javascriptFile.charAt(i) == '}')
                        braces--;
                }
                decipherFunctions = mainDecipherFunct;
                // Search the main function for extra functions and variables
                // needed for deciphering
                // Search for variables
                mat = patVariableFunction.matcher(mainDecipherFunct);
                while (mat.find()) {
                    String variableDef = "var " + mat.group(2) + "={";
                    if (decipherFunctions.contains(variableDef)) {
                        continue;
                    }
                    startIndex = javascriptFile.indexOf(variableDef) + variableDef.length();
                    for (int braces = 1, i = startIndex; i < javascriptFile.length(); i++) {
                        if (braces == 0) {
                            decipherFunctions += variableDef + javascriptFile.substring(startIndex, i) + ";";
                            break;
                        }
                        if (javascriptFile.charAt(i) == '{')
                            braces++;
                        else if (javascriptFile.charAt(i) == '}')
                            braces--;
                    }
                }
                // Search for functions
                mat = patFunction.matcher(mainDecipherFunct);
                while (mat.find()) {
                    String functionDef = "function " + mat.group(2) + "(";
                    if (decipherFunctions.contains(functionDef)) {
                        continue;
                    }
                    startIndex = javascriptFile.indexOf(functionDef) + functionDef.length();
                    for (int braces = 0, i = startIndex; i < javascriptFile.length(); i++) {
                        if (braces == 0 && startIndex + 5 < i) {
                            decipherFunctions += functionDef + javascriptFile.substring(startIndex, i) + ";";
                            break;
                        }
                        if (javascriptFile.charAt(i) == '{')
                            braces++;
                        else if (javascriptFile.charAt(i) == '}')
                            braces--;
                    }
                }

                if (LOGGING)
                    Log.d(LOG_TAG, "Function: " + decipherFunctions);
                decipherViaWebView(encSignatures);
                if (CACHING) {
                    writeDeciperFunctToChache();
                }
            } else {
                exceptionBuilder.append("decipherSignature find false");
                exceptionBuilder.append("\n");
                return false;
            }
        } else {
            decipherViaWebView(encSignatures);
        }
        return true;
    }

    private void readDecipherFunctFromCache() {
        File cacheFile = new File(cacheDirPath + "/" + CACHE_FILE_NAME);
        // The cached functions are valid for 2 weeks
        if (cacheFile.exists() && (System.currentTimeMillis() - cacheFile.lastModified()) < 1209600000) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile), "UTF-8"));
                decipherJsFileName = reader.readLine();
                decipherFunctionName = reader.readLine();
                decipherFunctions = reader.readLine();
            } catch (Exception e) {
                exceptionBuilder.append(e.getMessage());
                exceptionBuilder.append("\n");
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void setParseDashManifest(boolean parseDashManifest) {
    }


    public void setIncludeWebM(boolean includeWebM) {
    }


    public void setDefaultHttpProtocol(boolean useHttp) {
    }

    private void writeDeciperFunctToChache() {
        File cacheFile = new File(cacheDirPath + "/" + CACHE_FILE_NAME);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8"));
            writer.write(decipherJsFileName + "\n");
            writer.write(decipherFunctionName + "\n");
            writer.write(decipherFunctions);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void decipherViaWebView(final SparseArray<String> encSignatures) {
        final Context context = refContext.get();
        if (context == null) {
            return;
        }

        final StringBuilder stb = new StringBuilder(decipherFunctions + " function decipher(");
        stb.append("){return ");
        for (int i = 0; i < encSignatures.size(); i++) {
            int key = encSignatures.keyAt(i);
            if (i < encSignatures.size() - 1)
                stb.append(decipherFunctionName).append("('").append(encSignatures.get(key)).
                        append("')+\"\\n\"+");
            else
                stb.append(decipherFunctionName).append("('").append(encSignatures.get(key)).
                        append("')");
        }
        stb.append("};decipher();");

        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                new JsEvaluator(context).evaluate(stb.toString(), new JsCallback() {
                    @Override
                    public void onResult(String result) {
                        lock.lock();
                        try {
                            decipheredSignature = result;
                            jsExecuting.signal();
                        } finally {
                            lock.unlock();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        lock.lock();
                        try {
                            if (LOGGING){
                                Log.e(LOG_TAG, errorMessage);
                            }
                            exceptionBuilder.append(errorMessage);
                            exceptionBuilder.append("\n");

                            jsExecuting.signal();
                        } finally {
                            lock.unlock();
                        }
                    }
                });
            }
        });
    }
}
