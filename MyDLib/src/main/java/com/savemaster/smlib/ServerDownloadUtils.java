package com.savemaster.smlib;

import android.app.Activity;
import android.app.ProgressDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.savemaster.smlib.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ServerDownloadUtils {

    public static boolean isSupport(String url){
        if(TextUtils.isEmpty(url)){
            return false;
        }

        if(url.contains("gfycat")
                || url.contains("funimate")
                || url.contains("wwe")
                || url.contains("1tv.ru")
                || url.contains("naver")
                || url.contains("gloria.tv")
                || url.contains("vidcommons.org")
                || url.contains("media.ccc.de")
                || url.contains("vlive")
                || url.contains("blogspot.com")
                || url.contains("vimeo.com")
                || (url.contains("flickr") && url.contains("flic.kr"))
                || url.contains("streamable")
                || (url.contains("redd.it")
                || url.contains("reddit"))
//                || (url.contains("soundcloud"))
                || url.contains("bandcamp")
                || url.contains("cocoscope")
                || url.contains("izlesene")
                || url.contains("bitchute")
                || url.contains("espn.com")
                || url.contains("coub")
                || url.contains("ted.com")
                || url.contains("twitch")
                || url.contains("imdb.com")
                || url.contains("camdemy")
                || url.contains("pinterest")
                || url.contains("pin.it")
                || url.contains("imgur.com")
                || url.contains("twitter")){
            return true;
        }
        return false;
    }

    @Keep
    public static void CalldlApisDataDataServerUrl(Activity activity, ProgressDialog pd, String url, boolean hasQualityOption) {
        String DlApisUrl2 = "https://dlphpapis.herokuapp.com/api/info?url=";
        String DlApisUrl = "https://dlphpapis2.herokuapp.com/api/info?url=";

        String serverUrl = ASharePreferenceUtils.getString(activity, ASharePreferenceUtils.SERVER_URL, "");

        if(TextUtils.isEmpty(serverUrl)){
            Random rand = new Random();
            int rand_int1 = rand.nextInt(2);
            if (rand_int1 == 0) {
                serverUrl = DlApisUrl2;
            } else {
                serverUrl = DlApisUrl;
            }
        }

        CalldlApisDataData(activity, pd, serverUrl, url, hasQualityOption);
    }


    @Keep
    public static void CalldlApisDataData(Activity activity, ProgressDialog pd, String serverUrl, String url, boolean hasQualityOption) {

        AndroidNetworking.get(serverUrl + url + "&flatten=True")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseCalldlApisDataData(activity, response);
                        if(pd != null){
                            if(pd != null){pd.dismiss();}
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(activity, "Download error:" + error.getErrorBody(), Toast.LENGTH_LONG).show();
                        if(pd != null){
                            if(pd != null){pd.dismiss();}
                        }
                    }
                });
    }

    @Keep
    public static void parseCalldlApisDataData(Activity Mcontext, JSONObject response) {
        Gson gson = new Gson();
        DLDataParser gsonObj = gson.fromJson(response.toString(), DLDataParser.class);

        View view = LayoutInflater.from(Mcontext).inflate(R.layout.savemasterdown_wload_bott_quality, null);

        Button btncancel_bottomsheet = view.findViewById(R.id.btncancel_bottomsheet);
        Button btnopen_bottomsheet = view.findViewById(R.id.btnopen_bottomsheet);
        TextView source_bottomsheet = view.findViewById(R.id.source_bottomsheet);
        TextView title_bottomsheet = view.findViewById(R.id.bottomsheet_title);
        TextView duration_bottomsheet = view.findViewById(R.id.bottomsheet_duration);
//        ImageView thumb_bottomsheet = view.findViewById(R.id.bottomsheet_thumbnail);

        RecyclerView recyclerView = view.findViewById(R.id.recqualitybottomsheet);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Mcontext));

//        RecyclerView recyclerView_audio = view.findViewById(R.id.recqualitybottomsheet_aud);
//        recyclerView_audio.setHasFixedSize(true);
//        recyclerView_audio.setLayoutManager(new LinearLayoutManager(Mcontext));

        QualityBottomsheetAdapter qualityBottomsheetAdapter = null;
        try {
            if (response.getJSONArray("videos").length() > 1) {
                if (response.getJSONArray("videos").getJSONObject(0).has("protocol")) {
                    splitDataToVideoAndAudio_video(Mcontext, gsonObj.getVideos(), recyclerView, null, qualityBottomsheetAdapter, gsonObj.getVideos().get(0).getExtractor());
                }

                BottomSheetDialog dialog = new BottomSheetDialog(Mcontext);

                if (response.getJSONArray("videos").getJSONObject(0).has("extractor")) {
                    String styledText = "Source: <font color='red'>" + gsonObj.getVideos().get(0).getExtractor() + "</font>";
                    source_bottomsheet.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
                }

                if (response.getJSONArray("videos").getJSONObject(0).has("duration")) {

                    String mystring = gsonObj.getVideos().get(0).getDuration() + "";
                    String[] correctstring = mystring.split("\\.");

                    long hours = Long.parseLong(correctstring[0]) / 3600;
                    long minutes = (Long.parseLong(correctstring[0]) % 3600) / 60;
                    long seconds = Long.parseLong(correctstring[0]) % 60;

                    String DurationstyledText = "Duration: <font color='red'>" + String.format("%02d:%02d:%02d", hours, minutes, seconds) + "</font>";
                    duration_bottomsheet.setText(Html.fromHtml(DurationstyledText), TextView.BufferType.SPANNABLE);
                }

                if (response.getJSONArray("videos").getJSONObject(0).has("title")) {

                    String titletyledText = "Title: <font color='red'>" + String.format("%s", gsonObj.getVideos().get(0).getTitle()) + "</font>";
                    title_bottomsheet.setText(Html.fromHtml(titletyledText), TextView.BufferType.SPANNABLE);
                }

//                if (response.getJSONArray("videos").getJSONObject(0).has("thumbnail")) {
//                    Glide.with(Mcontext)
//                            .load(gsonObj.getVideos().get(0).getThumbnail())
//                            .into(thumb_bottomsheet);
//                }
                // source_bottomsheet.setText(String.format("Source: %s", gsonObj.getVideos().get(0).getExtractor()));
                btncancel_bottomsheet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                btnopen_bottomsheet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                        dialog.getBehavior().setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
                    }
                });

                dialog.setContentView(view);
                dialog.show();

            } else {
                if (response.getJSONArray("videos").getJSONObject(0).has("formats")) {

                    splitDataToVideoAndAudio_format(Mcontext, gsonObj.getVideos().get(0).getFormats(), recyclerView, null, qualityBottomsheetAdapter, gsonObj.getVideos().get(0).getExtractor());

                } else {
                    if (response.getJSONArray("videos").getJSONObject(0).has("protocol")) {

                        String ishttp = response.getJSONArray("videos").getJSONObject(0).getString("protocol");
                        if (ishttp.contains("http")) {
                            qualityBottomsheetAdapter = new QualityBottomsheetAdapter(Mcontext, gsonObj.getVideos().get(0).getURL(), gsonObj.getVideos().get(0).getExtractor(), true);
                            recyclerView.setAdapter(qualityBottomsheetAdapter);

                        }
                    }
                }


                BottomSheetDialog dialog = new BottomSheetDialog(Mcontext);

                if (response.getJSONArray("videos").getJSONObject(0).has("extractor")) {
                    String styledText = "Source: <font color='red'>" + gsonObj.getVideos().get(0).getExtractor() + "</font>";
                    source_bottomsheet.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
                }

                if (response.getJSONArray("videos").getJSONObject(0).has("duration")) {

                    String mystring = gsonObj.getVideos().get(0).getDuration() + "";
                    String[] correctstring = mystring.split("\\.");

                    long hours = Long.parseLong(correctstring[0]) / 3600;
                    long minutes = (Long.parseLong(correctstring[0]) % 3600) / 60;
                    long seconds = Long.parseLong(correctstring[0]) % 60;

                    String DurationstyledText = "Duration: <font color='red'>" + String.format("%02d:%02d:%02d", hours, minutes, seconds) + "</font>";
                    duration_bottomsheet.setText(Html.fromHtml(DurationstyledText), TextView.BufferType.SPANNABLE);
                }

                if (response.getJSONArray("videos").getJSONObject(0).has("title")) {

                    System.out.println("reccccc VVKKtttt " + gsonObj.getVideos().get(0).getTitle());


                    String titletyledText = "Title: <font color='red'>" + String.format("%s", gsonObj.getVideos().get(0).getTitle()) + "</font>";
                    title_bottomsheet.setText(Html.fromHtml(titletyledText), TextView.BufferType.SPANNABLE);
                }
                btncancel_bottomsheet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                dialog.setContentView(view);
                dialog.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void splitDataToVideoAndAudio_format(Activity Mcontext, List<Format> formatList, RecyclerView recyclerView_video, RecyclerView recyclerView_audio, QualityBottomsheetAdapter qualityBottomsheetAdapter, String extractor) {


        List<Format> formatList_sub = new ArrayList<>();
        List<Format> formatList_sub_video = new ArrayList<>();
        for (int i = 0; i < formatList.size(); i++) {


            if (formatList.get(i).getProtocol().contains("http") && !formatList.get(i).getProtocol().contains("http_dash_segments") && !formatList.get(i).getURL().contains(".m3u8")) {

                if (formatList.get(i).getAcodec() != null && !formatList.get(i).getAcodec().equals("none")) {

                    if (formatList.get(i).getEXT().equals("m4a") ||
                            formatList.get(i).getEXT().equals("mp3") ||
                            formatList.get(i).getEXT().equals("wav")) {
                        formatList_sub.add(formatList.get(i));
                    } else if (formatList.get(i).getEXT().equals("mp4") || formatList.get(i).getEXT().equals("mpeg")) {

                        formatList_sub_video.add(formatList.get(i));

                    }
                } else {

                    if (formatList.get(i).getEXT().equals("m4a") ||
                            formatList.get(i).getEXT().equals("mp3") ||
                            formatList.get(i).getEXT().equals("wav")) {
                        formatList_sub.add(formatList.get(i));
                    } else if (formatList.get(i).getEXT().equals("mp4") || formatList.get(i).getEXT().equals("mpeg")) {

                        formatList_sub_video.add(formatList.get(i));

                    }

                    formatList.get(i).setFormat("(no audio) " + formatList.get(i).getFormat());

                }

            }
        }

        Collections.reverse(formatList_sub_video);

        qualityBottomsheetAdapter = new QualityBottomsheetAdapter(Mcontext, formatList_sub_video, extractor, false);
        recyclerView_video.setAdapter(qualityBottomsheetAdapter);

//        qualityBottomsheetAdapter = new QualityBottomsheetAdapter(Mcontext, formatList_sub, extractor, false);
//        recyclerView_audio.setAdapter(qualityBottomsheetAdapter);


    }

    private static void splitDataToVideoAndAudio_video(Activity Mcontext, List<Video> videoList, RecyclerView recyclerView_video, RecyclerView recyclerView_audio, QualityBottomsheetAdapter qualityBottomsheetAdapter, String extractor) {

        List<Video> videoList_sub = new ArrayList<>();
        List<Video> videoList_sub_video = new ArrayList<>();
        for (int i = 0; i < videoList.size(); i++) {
            if (videoList.get(i).getProtocol().contains("http") && !videoList.get(i).getProtocol().contains("http_dash_segments") && !videoList.get(i).getURL().contains(".m3u8")) {
                if (videoList.get(i).getEXT().equals("m4a") ||
                        videoList.get(i).getEXT().equals("mp3") ||
                        videoList.get(i).getEXT().equals("wav")) {
                    videoList_sub.add(videoList.get(i));
                } else if (videoList.get(i).getEXT().equals("mp4") || videoList.get(i).getEXT().equals("mpeg")) {

                    videoList_sub_video.add(videoList.get(i));
                }
            }
        }

        Collections.reverse(videoList_sub_video);


        qualityBottomsheetAdapter = new QualityBottomsheetAdapter(Mcontext, videoList_sub_video.get(0).getExtractor(), false, videoList_sub_video, true);
        recyclerView_video.setAdapter(qualityBottomsheetAdapter);

//        qualityBottomsheetAdapter = new QualityBottomsheetAdapter(Mcontext, videoList_sub.get(0).getExtractor(), false, videoList_sub, true);
//        recyclerView_audio.setAdapter(qualityBottomsheetAdapter);
    }

}
