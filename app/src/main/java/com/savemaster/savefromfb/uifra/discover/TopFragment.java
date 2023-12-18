package com.savemaster.savefromfb.uifra.discover;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;
import com.savemaster.savefromfb.uiact.MainActivity;
import com.savemaster.download.DownloaderImpl;
import com.savemaster.moton.MyCommon;
import com.savemaster.savefromfb.retrofit.Retrofit2;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdLoader;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.VideoOptions;
//import com.google.android.gms.ads.nativead.NativeAdOptions;

import savemaster.save.master.pipd.stream.StreamInfoItem;
import savemaster.save.master.pipd.stream.StreamType;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.ads.AppInterstitialAd;
import com.savemaster.savefromfb.uiact.BaseFragment;
import com.savemaster.savefromfb.uifra.discover.adapter.VideoListAdapter;
import com.savemaster.savefromfb.local.dialog.PlaylistAppendDialog;
import com.savemaster.savefromfb.models.request.explore.Client;
import com.savemaster.savefromfb.models.request.explore.Context;
import com.savemaster.savefromfb.models.request.explore.ExploreRequest;
import com.savemaster.savefromfb.models.request.explore.MainAppWebInfo;
import com.savemaster.savefromfb.models.response.explore.ItemsItem;
import com.savemaster.savefromfb.player.playqueue.PlayQueue;
import com.savemaster.savefromfb.player.playqueue.SinglePlayQueue;
import com.savemaster.savefromfb.util.AppUtils;
import com.savemaster.savefromfb.util.Constants;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.SharedUtils;

public class TopFragment extends BaseFragment implements VideoListAdapter.Listener {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.empty_state_view)
    View emptyView;
    @BindView(R.id.error_panel)
    View errorView;
    @BindView(R.id.error_message_view)
    TextView errorMessageView;

    // NativeAd
    private FrameLayout nativeAdView;

    private VideoListAdapter adapter;
    private String categoryId;
    private String categoryName;

    public TopFragment() {
    }

    public static TopFragment getInstance(String params, String categoryName) {

        TopFragment topFragment = new TopFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.CATEGORY_ID, params);
        bundle.putString(Constants.CATEGORY_NAME, categoryName);
        topFragment.setArguments(bundle);

        return topFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        // init InterstitialAd
        AppInterstitialAd.getInstance().init(activity);
    }

    private void init() {
        categoryId = getArguments() != null ? getArguments().getString(Constants.CATEGORY_ID) : "4gINGgt5dG1hX2NoYXJ0cw";
        categoryName = getArguments() != null ? getArguments().getString(Constants.CATEGORY_NAME) : getString(R.string.savemasterdown_music);
    }

    private void initRecyclerView() {
        // VideoListAdapter
        adapter = new VideoListAdapter(this);

        // LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        View headerView = getLayoutInflater().inflate(R.layout.savemasterdown_native_ad_list_header, recyclerView, false);
        nativeAdView = headerView.findViewById(R.id.template_view);
        adapter.setHeader(headerView);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.savemasterdown_top_fragment, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        initRecyclerView();
        getVideos();

        // show ad
        showNativeAd();
    }

    private void getVideos() {
        String countryCode = AppUtils.getCountryCode(activity);
//        String languageCode = AppUtils.getLanguageCode(activity);

        MainAppWebInfo mainAppWebInfo = new MainAppWebInfo();
        mainAppWebInfo.graftUrl = "/feed/trending";
        mainAppWebInfo.webDisplayMode = "WEB_DISPLAY_MODE_BROWSER";

        Client client = new Client();
        client.clientName = "WEB";
        client.clientVersion = "2.20210526.07.00";
        client.userAgent = DownloaderImpl.USER_AGENT;
        client.hl = "en";
        client.gl = countryCode;
        client.mainAppWebInfo = mainAppWebInfo;

        Context context = new Context();
        context.client = client;

        ExploreRequest request = new ExploreRequest();
        request.browseId = "FEtrending";
        request.params = categoryId;
        request.context = context;

        Retrofit2.restApi().explore(request)
                // apply schedulers
                .compose(Retrofit2.applySchedulers())
                // start, show progress
                .doOnSubscribe(() -> setProgressVisible(true))
                // terminate, hide progress
                .doOnTerminate(() -> setProgressVisible(false)).subscribe(
                // onNext
                exploreResponse -> {
                    List<ItemsItem> itemsItems = exploreResponse
                            .getContents()
                            .getTwoColumnBrowseResultsRenderer()
                            .getTabs().get(getTabIndex())
                            .getTabRenderer()
                            .getContent()
                            .getSectionListRenderer()
                            .getContents().get(0)
                            .getItemSectionRenderer()
                            .getContents().get(0)
                            .getShelfRenderer()
                            .getContent()
                            .getExpandedShelfContentsRenderer()
                            .getItems();
                    // set items to videoCategoryAdapter
                    adapter.setItems(itemsItems);

                    // show emptyView if empty
                    emptyView.setVisibility(adapter.getItems().isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(adapter.getItems().isEmpty() ? View.GONE : View.VISIBLE);
                    errorView.setVisibility(View.GONE);
                },
                // onError
                throwable -> {
                    // set error message
                    errorMessageView.setText(String.format(getString(R.string.savemasterdown_msg_nothing), categoryName));
                    // show errorView
                    errorView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                });
    }

    private int getTabIndex() {
        switch (categoryId) {
            // Music
            case "4gINGgt5dG1hX2NoYXJ0cw":
            default:
                return 1;
            // Gaming
            case "4gIcGhpnYW1pbmdfY29ycHVzX21vc3RfcG9wdWxhcg":
                return 2;
            // Movies
            case "4gIKGgh0cmFpbGVycw":
                return 3;
        }
    }

    private void setProgressVisible(boolean progressVisible) {
        errorView.setVisibility(View.GONE);
        progressBar.setVisibility(progressVisible ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.error_button_retry)
    void onRetry() {
        // retry if failed
        getVideos();
    }

    private PlayQueue getPlayQueue() {

        List<StreamInfoItem> streamInfoItems = Stream.of(adapter.getItems())
                // map to StreamInfoItem
                .map(item -> {

                    StreamInfoItem streamInfoItem = new StreamInfoItem(Constants.YOUTUBE_SERVICE_ID, Constants.VIDEO_BASE_URL + item.getVideoRenderer().getVideoId(), item.getVideoRenderer().getOwnerText().getRuns().get(0).getText(), StreamType.VIDEO_STREAM);
                    // need to set thumbnail url here
                    try{
                        streamInfoItem.setThumbnailUrl(item.getVideoRenderer().getThumbnail().getThumbnails().get(2).getUrl());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    streamInfoItem.setUploaderName(item.getVideoRenderer().getOwnerText().getRuns().get(0).getText());

                    return streamInfoItem;
                })
                // toList
                .toList();

        return new SinglePlayQueue(streamInfoItems, 0);
    }

    public void playAll() {
        if (!adapter.getItems().isEmpty()) {
            AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnPopupPlayer(activity, getPlayQueue(), false));
        }
    }

    @Override
    public void onVideoClicked(int position) {
//        AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> {
//            ItemsItem item = adapter.getItem(position);
//            StreamInfoItem streamInfoItem = new StreamInfoItem(Constants.YOUTUBE_SERVICE_ID, Constants.VIDEO_BASE_URL + item.getVideoRenderer().getVideoId(), item.getVideoRenderer().getOwnerText().getRuns().get(0).getText(), StreamType.VIDEO_STREAM);
//            // need to set thumbnail url here
//            streamInfoItem.setThumbnailUrl(item.getVideoRenderer().getThumbnail().getThumbnails().get(2).getUrl());
//            streamInfoItem.setUploaderName(item.getVideoRenderer().getOwnerText().getRuns().get(0).getText());
//            NavigationHelper.openVideoDetailFragment(getFM(), streamInfoItem.getServiceId(), streamInfoItem.getUrl(), streamInfoItem.getName());
//        });

        ItemsItem item = adapter.getItem(position);
        StreamInfoItem streamInfoItem = new StreamInfoItem(Constants.YOUTUBE_SERVICE_ID, Constants.VIDEO_BASE_URL + item.getVideoRenderer().getVideoId(), item.getVideoRenderer().getOwnerText().getRuns().get(0).getText(), StreamType.VIDEO_STREAM);
        // need to set thumbnail url here
        streamInfoItem.setThumbnailUrl(item.getVideoRenderer().getThumbnail().getThumbnails().get(2).getUrl());
        streamInfoItem.setUploaderName(item.getVideoRenderer().getOwnerText().getRuns().get(0).getText());
        NavigationHelper.openVideoDetailFragment(getFM(), streamInfoItem.getServiceId(), streamInfoItem.getUrl(), streamInfoItem.getName());

        //show ad
        if(getActivity() instanceof MainActivity){
            ((MainActivity)getActivity()).showDetailADFragment();
        }
    }

    @Override
    public void onMoreOption(int position, View view) {
        ItemsItem item = adapter.getItem(position);

        StreamInfoItem infoItem = new StreamInfoItem(0, Constants.VIDEO_BASE_URL + item.getVideoRenderer().getVideoId(), item.getVideoRenderer().getOwnerText().getRuns().get(0).getText(), StreamType.VIDEO_STREAM);
        infoItem.setThumbnailUrl(item.getVideoRenderer().getThumbnail().getThumbnails().get(2).getUrl());
        infoItem.setUploaderName(item.getVideoRenderer().getOwnerText().getRuns().get(0).getText());

        // show popup menu
        showPopupMenu(infoItem, view);
    }

    private void showPopupMenu(StreamInfoItem infoItem, View view) {

        PopupMenu popup = new PopupMenu(activity, view, Gravity.END, 0, R.style.mPopupMenu);
        popup.getMenuInflater().inflate(R.menu.savemasterdown_menu_popup, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()) {

                case R.id.action_play:
                    AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnMainPlayer(activity, new SinglePlayQueue(Collections.singletonList(infoItem), 0), true));
                    break;

                case R.id.action_append_playlist:
                    PlaylistAppendDialog.fromStreamInfoItems(Collections.singletonList(infoItem)).show(getChildFragmentManager(), "TopFragment");
                    break;

                case R.id.action_share:
                    SharedUtils.shareUrl(activity);
                    break;
            }
            return true;
        });
    }

    MyCommon myCommon = new MyCommon();
    private void showNativeAd() {
        if(getActivity() == null || getActivity().isFinishing()){
            return;
        }
        myCommon.loadBigNative(getActivity(), nativeAdView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
