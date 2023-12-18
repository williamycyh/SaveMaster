package com.savemaster.savefromfb.uifra.detail;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static savemaster.save.master.pipd.stream.StreamExtractor.NO_AGE_LIMIT;
import static com.savemaster.savefromfb.player.helper.PlayerHelper.globalScreenOrientationLocked;
import static com.savemaster.savefromfb.util.AnimationUtils.animateView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;

import com.savemaster.savefromfb.db.subscription.SubscriptionEntity;
import com.savemaster.download.ui.DownloadDialog;
import com.savemaster.savefromfb.uifra.BackPressable;
import com.savemaster.savefromfb.uifra.BaseStateFragment;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdLoader;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.VideoOptions;
//import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.jakewharton.rxbinding2.view.RxView;

import org.jetbrains.annotations.NotNull;
import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.channel.ChannelInfo;
import savemaster.save.master.pipd.exceptions.ContentNotAvailableException;
import savemaster.save.master.pipd.playlist.PlaylistInfoItem;
import savemaster.save.master.pipd.stream.StreamInfo;
import savemaster.save.master.pipd.stream.StreamInfoItem;
import savemaster.save.master.pipd.stream.StreamType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import icepick.State;
import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.R;
import com.savemaster.savefromfb.uiact.UIReCaptchaActivity;
import com.savemaster.savefromfb.ads.AppInterstitialAd;
import com.savemaster.savefromfb.info_list.InfoItemBuilder;
import com.savemaster.savefromfb.local.dialog.PlaylistAppendDialog;
import com.savemaster.savefromfb.local.subscription.SubscriptionService;
import com.savemaster.moton.MyCommon;
import com.savemaster.savefromfb.player.UIMainPlayer;
import com.savemaster.savefromfb.player.VideoPlayerImpl;
import com.savemaster.savefromfb.player.event.OnKeyDownListener;
import com.savemaster.savefromfb.player.event.PlayerServiceExtendedEventListener;
import com.savemaster.savefromfb.player.helper.PlayerHelper;
import com.savemaster.savefromfb.player.helper.PlayerHolder;
import com.savemaster.savefromfb.player.playqueue.PlayQueue;
import com.savemaster.savefromfb.player.playqueue.PlayQueueItem;
import com.savemaster.savefromfb.player.playqueue.SinglePlayQueue;
import com.savemaster.savefromfb.util.AnimationUtils;
import com.savemaster.savefromfb.util.Constants;
import com.savemaster.savefromfb.util.DeviceUtils;
import com.savemaster.savefromfb.util.ExtractorHelper;
import com.savemaster.savefromfb.util.GlideUtils;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.OnClickGesture;
import com.savemaster.savefromfb.util.PermissionHelper;
import com.savemaster.savefromfb.util.SharedUtils;
import com.savemaster.savefromfb.util.ThemeHelper;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class VideoDetailFragment extends BaseStateFragment<StreamInfo> implements BackPressable,
        SharedPreferences.OnSharedPreferenceChangeListener,
        View.OnClickListener,
        PlayerServiceExtendedEventListener,
        OnKeyDownListener {

    private static final String TAG = VideoDetailFragment.class.getSimpleName();

    // Amount of videos to show on start
    private static final int INITIAL_RELATED_VIDEOS = 16;

    private InfoItemBuilder infoItemBuilder = null;

    private int updateFlags = 0;
    private static final int RELATED_STREAMS_UPDATE_FLAG = 0x1;
    private static final int RESOLUTIONS_MENU_UPDATE_FLAG = 0x2;
    public static final float MAX_OVERLAY_ALPHA = 1.0f;
    private static final float MAX_PLAYER_HEIGHT = 0.7f;

    public static final String ACTION_SHOW_MAIN_PLAYER = "com.android.protube.VideoDetailFragment.ACTION_SHOW_MAIN_PLAYER";
    public static final String ACTION_HIDE_MAIN_PLAYER = "com.android.protube.VideoDetailFragment.ACTION_HIDE_MAIN_PLAYER";
    public static final String ACTION_MINIMIZE_MAIN_PLAYER = "com.android.protube.VideoDetailFragment.ACTION_MINIMIZE_MAIN_PLAYER";
    public static final String ACTION_PLAYER_STARTED = "com.android.protube.VideoDetailFragment.ACTION_PLAYER_STARTED";
    public static final String ACTION_VIDEO_FRAGMENT_RESUMED = "com.android.protube.VideoDetailFragment.ACTION_VIDEO_FRAGMENT_RESUMED";
    public static final String ACTION_VIDEO_FRAGMENT_STOPPED = "com.android.protube.VideoDetailFragment.ACTION_VIDEO_FRAGMENT_STOPPED";

    @State
    protected int serviceId = Constants.YOUTUBE_SERVICE_ID;
    @State
    protected String name;
    @State
    protected String url;
    protected static PlayQueue playQueue;
    @State
    int bottomSheetState = BottomSheetBehavior.STATE_EXPANDED;
    @State
    protected boolean autoPlayEnabled = true;

    private StreamInfo currentInfo;
    private Disposable currentWorker;
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable subscribeButtonMonitor;
    private SubscriptionService subscriptionService;

    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
    private BroadcastReceiver broadcastReceiver;

    private ContentObserver settingsContentObserver;
    private UIMainPlayer playerService;
    private VideoPlayerImpl player;

    // Views
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;
    private LinearLayout contentRootLayoutHiding;

    private View videoPlayerLayout;
    private View frameVideoPlayer;
    private ViewGroup playerPlaceholder;
    private ImageView thumbnailImageView;
    private ImageView thumbnailPlayButton;
    private TextView detailDurationView;

    private View videoTitleRoot;
    private TextView videoTitleTextView;
    private ImageView videoTitleToggleArrow;
    private TextView videoCountView;

    private TextView detailControlsDownload;
    private TextView detailControlsPopup;
    private TextView detailControlsAddToPlaylist;

    private LinearLayout videoDescriptionRootLayout;
    private TextView videoUploadDateView;
    private TextView videoDescriptionView;

    private View uploaderRootLayout;
    private TextView uploaderTextView;
    private ImageView uploaderThumb;
    private TextView uploaderSubscriberTextView;
    private MaterialButton channelSubscribeButton;

    // overlay views
    private MaterialCardView overlay;
    private LinearLayout overlayMetadata;
    private ImageView overlayThumbnailImageView;
    private TextView overlayTitleTextView;
    private TextView overlayChannelTextView;
    private LinearLayout overlayButtons;
    private ImageButton overlayPlayPauseButton;
    private ImageButton overlayCloseButton;

    private View nextStreamTitleView;
    private LinearLayout relatedStreamsView;

    @BindView(R.id.switch_auto_play)
    SwitchMaterial switchAutoplay;
    @BindView(R.id.message_restricted)
    TextView messageRestricted;

    // NativeAd
    private View view;
    @BindView(R.id.template_view)
    FrameLayout nativeAdView;
//    @BindView(R.id.adView)
//    AdView adView;

    @Override
    public void onServiceConnected(VideoPlayerImpl connectedPlayer, UIMainPlayer connectedPlayerService, boolean playAfterConnect) {
        player = connectedPlayer;
        playerService = connectedPlayerService;

        // It will do nothing if the player is not in fullscreen mode
        hideSystemUiIfNeeded();

        if (!player.videoPlayerSelected() && !playAfterConnect) {
            return;
        }

        if (DeviceUtils.isLandscape(requireContext())) {
            // If the video is playing but orientation changed
            // let's make the video in fullscreen again
            checkLandscape();
        } else if (player.isFullscreen() && !player.isVerticalVideo()
                // Tablet UI has orientation-independent fullscreen
                && !DeviceUtils.isTablet(activity)) {
            // Device is in portrait orientation after rotation but UI is in fullscreen.
            // Return back to non-fullscreen state
            player.toggleFullscreen();
        }

        if (playerIsNotStopped() && player.videoPlayerSelected()) {
            addVideoPlayerView();
        }

        if (playAfterConnect || (currentInfo != null && isAutoplayEnabled() && player.getParentActivity() == null)) {
            openMainPlayer();
        }
    }

    @Override
    public void onServiceDisconnected() {
        playerService = null;
        player = null;
    }

    public static VideoDetailFragment getInstance(int serviceId, String videoUrl, String name, final PlayQueue queue) {

        VideoDetailFragment instance = new VideoDetailFragment();
        instance.setInitialData(serviceId, videoUrl, name, queue);
        return instance;
    }

    public static VideoDetailFragment getInstanceInCollapsedState() {
        final VideoDetailFragment instance = new VideoDetailFragment();
        instance.bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED;
        instance.setBottomNavigationViewVisibility(View.VISIBLE);
        return instance;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        subscriptionService = SubscriptionService.getInstance(context);
    }

    // Fragment's Lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(this);

        setupBroadcastReceiver();

        settingsContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(final boolean selfChange) {
                if (activity != null && !globalScreenOrientationLocked(activity)) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }
        };
        activity.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), false, settingsContentObserver);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.savemasterdown_fragment_video_detail, container, false);
        ButterKnife.bind(this, view);

        // init InterstitialAd
        AppInterstitialAd.getInstance().init(activity);

        return view;
    }

    @Override
    public void onPause() {
//        if (adView != null) {
//            adView.pause();
//        }
        super.onPause();
        if (currentWorker != null) currentWorker.dispose();
    }

    @Override
    public void onResume() {
//        if (adView != null) {
//            adView.resume();
//        }
        super.onResume();
        activity.sendBroadcast(new Intent(ACTION_VIDEO_FRAGMENT_RESUMED));

        if (updateFlags != 0) {
            if (!isLoading.get() && currentInfo != null) {
                if ((updateFlags & RELATED_STREAMS_UPDATE_FLAG) != 0) {
                    startLoading(false);
                }
                if ((updateFlags) != 0) {
                    startLoading(false);
                }
            }

            updateFlags = 0;
        }

        // Check if it was loading when the fragment was stopped/paused
        if (wasLoading.getAndSet(false) && !wasCleared()) {
            startLoading(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!activity.isChangingConfigurations()) {
            activity.sendBroadcast(new Intent(ACTION_VIDEO_FRAGMENT_STOPPED));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the service when user leaves the app with double back press
        // if video player is selected. Otherwise unbind
        if (activity.isFinishing() && player != null && player.videoPlayerSelected()) {
            PlayerHolder.stopService(App.getAppContext());
        } else {
            PlayerHolder.removeListener();
        }

        PreferenceManager.getDefaultSharedPreferences(activity).unregisterOnSharedPreferenceChangeListener(this);
        activity.unregisterReceiver(broadcastReceiver);
        activity.getContentResolver().unregisterContentObserver(settingsContentObserver);

        if (currentWorker != null) {
            currentWorker.dispose();
        }
        disposables.clear();
        currentWorker = null;
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback);

        if (activity.isFinishing()) {
            playQueue = null;
            currentInfo = null;
            stack = new LinkedList<>();
        }
    }

    @Override
    public void onDestroyView() {
        // destroy ad
//        if (adView != null) {
//            adView.destroy();
//        }
//        if (nativeAdView != null) {
//            nativeAdView.destroyNativeAd();
//        }
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UIReCaptchaActivity.RECAPTCHA_REQUEST && resultCode == Activity.RESULT_OK) {
            if (currentInfo != null) {
                NavigationHelper.openVideoDetailFragment(getFM(), serviceId, url, name);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.default_video_format_key))
                || key.equals(getString(R.string.default_resolution_key))
                || key.equals(getString(R.string.show_higher_resolutions_key))) {
            updateFlags |= RESOLUTIONS_MENU_UPDATE_FLAG;
        }

        boolean autoplay = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(getString(R.string.auto_queue_key), false);
        if (switchAutoplay != null) switchAutoplay.setChecked(autoplay);
    }

    // OnClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.detail_controls_download:
                if (PermissionHelper.checkStoragePermissions(activity, PermissionHelper.DOWNLOAD_DIALOG_REQUEST_CODE)) {
                    openDownloadDialog();
                } else {
                    Toast.makeText(getActivity(), "Please grant storage permission", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.detail_controls_popup:
//                AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> openPopupPlayer(false));
                openPopupPlayer(false);
                break;

            case R.id.detail_controls_playlist_append:
                if (getFM() != null && currentInfo != null) {
                    PlaylistAppendDialog.fromStreamInfo(currentInfo).show(getFM(), TAG);
                }
                break;

            case R.id.detail_uploader_root_layout:
                try {
                    if (!TextUtils.isEmpty(currentInfo.getUploaderUrl())) {
                        NavigationHelper.openChannelFragment(getFM(), currentInfo.getServiceId(), currentInfo.getUploaderUrl(), currentInfo.getUploaderName());
                    }
                } catch (Exception ignored) {
                }
                break;

            case R.id.detail_title_root_layout:
                toggleTitleAndDescription();
                break;

            case R.id.frame_video_player:
                openMainPlayer();
                break;

            case R.id.overlay_thumbnail:
            case R.id.overlay_metadata_layout:
            case R.id.overlay_buttons_layout:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                setBottomNavigationViewVisibility(View.GONE);
                break;

            case R.id.overlay_play_pause_button:
                if (playerIsNotStopped()) {
                    player.onPlayPause();
                    player.hideControls(0, 0);
                    if (DeviceUtils.isLandscape(requireContext())) {
                        showSystemUi();
                    }
                } else {
                    openMainPlayer();
                }

                setOverlayPlayPauseImage(player != null && player.isPlaying());
                break;

            case R.id.overlay_close_button:
//                AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> {
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//                    setBottomNavigationViewVisibility(View.VISIBLE);
//                });
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                setBottomNavigationViewVisibility(View.VISIBLE);
                break;
        }
    }

    public void openDownloadDialog() {
        try {
            DownloadDialog downloadDialog = DownloadDialog.newInstance(activity, currentInfo);
            downloadDialog.show(activity.getSupportFragmentManager(), "DownloadDialog");
        } catch (Exception e) {
            Toast.makeText(activity, R.string.savemasterdown_setup_menu_msg, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void toggleTitleAndDescription() {
        if (videoDescriptionRootLayout.getVisibility() == View.VISIBLE) {
            videoTitleTextView.setMaxLines(2);
            videoDescriptionRootLayout.setVisibility(View.GONE);
            videoTitleToggleArrow.setImageResource(R.drawable.savemasterdown_ic_arrow_down);
        } else {
            videoTitleTextView.setMaxLines(20);
            videoDescriptionRootLayout.setVisibility(View.VISIBLE);
            videoTitleToggleArrow.setImageResource(R.drawable.savemasterdown_ic_arrow_up);
        }
    }

    // Init
    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        contentRootLayoutHiding = rootView.findViewById(R.id.detail_content_root_hiding);

        // video player
        videoPlayerLayout = rootView.findViewById(R.id.video_player_layout);
        frameVideoPlayer = rootView.findViewById(R.id.frame_video_player);
        thumbnailImageView = rootView.findViewById(R.id.detail_thumbnail_image_view);
        thumbnailPlayButton = rootView.findViewById(R.id.detail_thumbnail_play_button);
        detailDurationView = rootView.findViewById(R.id.detail_duration_view);
        playerPlaceholder = rootView.findViewById(R.id.player_placeholder);

        // title
        videoTitleRoot = rootView.findViewById(R.id.detail_title_root_layout);
        videoTitleTextView = rootView.findViewById(R.id.detail_video_title_view);
        videoTitleToggleArrow = rootView.findViewById(R.id.detail_toggle_description_view);
        videoCountView = rootView.findViewById(R.id.detail_view_count_view);

        // control views
        detailControlsDownload = rootView.findViewById(R.id.detail_controls_download);
        detailControlsPopup = rootView.findViewById(R.id.detail_controls_popup);
        detailControlsAddToPlaylist = rootView.findViewById(R.id.detail_controls_playlist_append);

        // description
        videoDescriptionRootLayout = rootView.findViewById(R.id.detail_description_root_layout);
        videoUploadDateView = rootView.findViewById(R.id.detail_upload_date_view);
        videoDescriptionView = rootView.findViewById(R.id.detail_description_view);
        videoDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        videoDescriptionView.setAutoLinkMask(Linkify.WEB_URLS);
        videoDescriptionView.setLinkTextColor(ContextCompat.getColor(activity, R.color.md_blue_500));

        // channel views
        uploaderRootLayout = rootView.findViewById(R.id.detail_uploader_root_layout);
        uploaderTextView = rootView.findViewById(R.id.detail_uploader_text_view);
        uploaderThumb = rootView.findViewById(R.id.detail_uploader_thumbnail_view);
        uploaderSubscriberTextView = rootView.findViewById(R.id.detail_uploader_subscriber_text_view);
        channelSubscribeButton = rootView.findViewById(R.id.channel_subscribe_button);

        // next videos
        nextStreamTitleView = rootView.findViewById(R.id.detail_next_stream_title);
        relatedStreamsView = rootView.findViewById(R.id.detail_related_streams_view);

        // overlay views
        overlay = rootView.findViewById(R.id.overlay_layout);
        overlayMetadata = rootView.findViewById(R.id.overlay_metadata_layout);
        overlayThumbnailImageView = rootView.findViewById(R.id.overlay_thumbnail);
        overlayTitleTextView = rootView.findViewById(R.id.overlay_title_text_view);
        overlayChannelTextView = rootView.findViewById(R.id.overlay_channel_text_view);
        overlayButtons = rootView.findViewById(R.id.overlay_buttons_layout);
        overlayPlayPauseButton = rootView.findViewById(R.id.overlay_play_pause_button);
        overlayCloseButton = rootView.findViewById(R.id.overlay_close_button);

        // enable/disable Autoplay
        switchAutoplay.setChecked(PlayerHelper.isAutoQueueEnabled(activity));

        infoItemBuilder = new InfoItemBuilder(activity);

        // show ad
        showBannerAd();
        showNativeAd();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListeners() {
        super.initListeners();
        infoItemBuilder.setOnStreamSelectedListener(new OnClickGesture<StreamInfoItem>() {

            @Override
            public void selected(StreamInfoItem selectedItem) {
                AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> {
                    scrollToTop();
                    NavigationHelper.openVideoDetailFragment(getFM(), selectedItem.getServiceId(), selectedItem.getUrl(), selectedItem.getName());
                });
            }

            @Override
            public void more(StreamInfoItem selectedItem, View view) {
                showPopupMenu(selectedItem, view);
            }
        });

        infoItemBuilder.setOnPlaylistSelectedListener(new OnClickGesture<PlaylistInfoItem>() {
            @Override
            public void selected(PlaylistInfoItem selectedItem) {
                NavigationHelper.openPlaylistFragment(getFM(),
                        selectedItem.getServiceId(),
                        selectedItem.getUrl(),
                        selectedItem.getName());
            }
        });

        frameVideoPlayer.setOnClickListener(this);
        videoTitleRoot.setOnClickListener(this);
        uploaderRootLayout.setOnClickListener(this);
        detailControlsDownload.setOnClickListener(this);
        detailControlsPopup.setOnClickListener(this);
        detailControlsAddToPlaylist.setOnClickListener(this);
        overlayThumbnailImageView.setOnClickListener(this);
        overlayMetadata.setOnClickListener(this);
        overlayButtons.setOnClickListener(this);
        overlayCloseButton.setOnClickListener(this);
        overlayPlayPauseButton.setOnClickListener(this);

        setupBottomPlayer();
        if (!PlayerHolder.bound) {
            setHeightThumbnail();
        } else {
            PlayerHolder.startService(App.getAppContext(), false, this);
        }
    }

    private void initThumbnailViews(@NonNull StreamInfo info) {
        if (!TextUtils.isEmpty(info.getThumbnailUrl())) {
            GlideUtils.loadThumbnail(App.getAppContext(), thumbnailImageView, info.getThumbnailUrl());
        }

        if (!TextUtils.isEmpty(info.getUploaderAvatarUrl())) {
            GlideUtils.loadAvatar(App.getAppContext(), uploaderThumb, info.getUploaderAvatarUrl().replace("s48", "s720"));
        }
    }

    /**
     * Stack that contains the "navigation history".<br>
     * The peek is the current video.
     */
    private static LinkedList<StackItem> stack = new LinkedList<>();

    @Override
    public boolean onKeyDown(final int keyCode) {
        return player != null && player.onKeyDown(keyCode);
    }

    @Override
    public boolean onBackPressed() {
        /*// If we are in fullscreen mode just exit from it via first back press
        if (player != null && player.isFullscreen()) {
            if (!DeviceUtils.isTablet(activity)) {
                player.onPlay();
            }
            restoreDefaultOrientation();
            setAutoplay(true);
            return true;
        }*/

        // If we have something in history of played items we replay it here
        if (player != null && player.getPlayQueue() != null && player.videoPlayerSelected() && player.getPlayQueue().previous()) {
            return true;
        }
        // That means that we are on the start of the stack,
        // return false to let the MainActivity handle the onBack
        if (stack.size() <= 1) {
            restoreDefaultOrientation();
            return false;
        }

        // Remove top
        stack.pop();
        // Get stack item from the new top
        setupFromHistoryItem(stack.peek());

        return true;
    }

    private void setupFromHistoryItem(final StackItem item) {
        setAutoplay(true);
        hideMainPlayer();

        setInitialData(item.getServiceId(), item.getUrl(), !TextUtils.isEmpty(item.getTitle()) ? item.getTitle() : "", item.getPlayQueue());
        startLoading(false);

        // Maybe an item was deleted in background activity
        if (item.getPlayQueue().getItem() == null) {
            return;
        }

        final PlayQueueItem playQueueItem = item.getPlayQueue().getItem();
        // Update title, url, uploader from the last item in the stack (it's current now)
        final boolean isPlayerStopped = player == null || player.isPlayerStopped();
        if (playQueueItem != null && isPlayerStopped) {
            updateOverlayData(playQueueItem.getTitle(), playQueueItem.getUploader(), playQueueItem.getThumbnailUrl());
        }
    }

    // Info loading and handling
    @Override
    protected void doInitialLoadLogic() {
        if (wasCleared()) {
            return;
        }

        if (currentInfo == null) {
            prepareAndLoadInfo();
        } else {
            prepareAndHandleInfoIfNeededAfterDelay(currentInfo, true, 50);
        }
    }

    public void selectAndLoadVideo(final int sid, final String videoUrl, final String title, final PlayQueue queue) {
        // Situation when user switches from players to main player.
        // All needed data is here, we can start watching
        if (playQueue != null && playQueue.equals(queue)) {
            openMainPlayer();
            return;
        }
        setInitialData(sid, videoUrl, title, queue);
        if (player != null) {
            player.disablePreloadingOfCurrentTrack();
        }
        startLoading(false, true);
    }

    private void prepareAndHandleInfoIfNeededAfterDelay(final StreamInfo info, final boolean scrollToTop, final long delay) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (activity == null) {
                return;
            }
            // Data can already be drawn, don't spend time twice
            if (info.getName().equals(videoTitleTextView.getText().toString())) {
                return;
            }
            prepareAndHandleInfo(info, scrollToTop);
        }, delay);
    }

    private void prepareAndHandleInfo(final StreamInfo info, final boolean scrollToTop) {
        showLoading();
        if (scrollToTop) {
            scrollToTop();
        }
        handleResult(info);
        showContent();
    }

    protected void prepareAndLoadInfo() {
        scrollToTop();
        startLoading(false);
    }

    @Override
    public void startLoading(final boolean forceLoad) {
        super.startLoading(forceLoad);

        currentInfo = null;
        if (currentWorker != null) {
            currentWorker.dispose();
        }

        runWorker(forceLoad, stack.isEmpty());
    }

    private void startLoading(final boolean forceLoad, final boolean addToBackStack) {
        super.startLoading(forceLoad);
        currentInfo = null;
        if (currentWorker != null) {
            currentWorker.dispose();
        }

        runWorker(forceLoad, addToBackStack);
    }

    private void runWorker(final boolean forceLoad, final boolean addToBackStack) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        currentWorker = ExtractorHelper.getStreamInfo(serviceId, url, forceLoad)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    isLoading.set(false);
                    hideMainPlayer();
                    if (result.getAgeLimit() != NO_AGE_LIMIT) {
                        showAgeRestrictedContent();
                    } else {
                        handleResult(result);
                        showContent();
                        if (addToBackStack) {
                            if (playQueue == null) {
                                playQueue = new SinglePlayQueue(result);
                            }
                            if (stack.isEmpty() || !stack.peek().getPlayQueue().equals(playQueue)) {
                                stack.push(new StackItem(serviceId, url, name, playQueue));
                            }
                        }
                        if (isAutoplayEnabled()) {
                            openMainPlayer();
                        }
                    }
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    onError(throwable);
                });
    }

    public void scrollToTop() {
        nestedScrollView.scrollTo(0, 0);
    }

    private void openPopupPlayer(final boolean append) {
        if (!PermissionHelper.isPopupEnabled(activity)) {
            PermissionHelper.showPopupEnableToast(activity);
            return;
        }

        // See UI changes while remote playQueue changes
        if (player == null) {
            PlayerHolder.startService(App.getAppContext(), false, this);
        }

        //  If a user watched video inside fullscreen mode and than chose another player
        //  return to non-fullscreen mode
        if (player != null && player.isFullscreen()) {
            player.toggleFullscreen();
        }

        final PlayQueue queue = setupPlayQueueForIntent(append);
        if (append) {
            NavigationHelper.enqueueOnPopupPlayer(activity, queue, false);
        } else {
            NavigationHelper.playOnPopupPlayer(activity, queue, true);
        }
    }

    private void openMainPlayer() {
        if (playerService == null) {
            PlayerHolder.startService(App.getAppContext(), true, this);
            return;
        }
        if (currentInfo == null) {
            return;
        }

        final PlayQueue queue = setupPlayQueueForIntent(false);

        // Video view can have elements visible from popup,
        // We hide it here but once it ready the view will be shown in handleIntent()
        if (playerService.getView() != null) {
            playerService.getView().setVisibility(View.GONE);
        }
        addVideoPlayerView();

        final Intent playerIntent = NavigationHelper.getPlayerIntent(activity, UIMainPlayer.class, queue, null, true);
        activity.startService(playerIntent);
    }

    private void hideMainPlayer() {
        if (playerService == null || playerService.getView() == null || !player.videoPlayerSelected()) {
            return;
        }

        removeVideoPlayerView();
        playerService.stop(isAutoplayEnabled());
        playerService.getView().setVisibility(View.GONE);
    }

    private PlayQueue setupPlayQueueForIntent(final boolean append) {
        if (append) {
            return new SinglePlayQueue(currentInfo);
        }

        PlayQueue queue = playQueue;
        // Size can be 0 because queue removes bad stream automatically when error occurs
        if (queue == null || queue.size() == 0) {
            queue = new SinglePlayQueue(currentInfo);
        }

        return queue;
    }

    public void setAutoplay(final boolean autoplay) {
        this.autoPlayEnabled = autoplay;
    }

    // This method overrides default behaviour when setAutoplay() is called.
    // Don't auto play if the user selected an external player or disabled it in settings
    private boolean isAutoplayEnabled() {
        return autoPlayEnabled && (player == null || player.videoPlayerSelected()) && bottomSheetState != BottomSheetBehavior.STATE_HIDDEN;
    }

    private void addVideoPlayerView() {
        if (player == null || getView() == null) {
            return;
        }

        // Check if viewHolder already contains a child
        if (player.getRootView().getParent() != playerPlaceholder) {
            playerService.removeViewFromParent();
        }
        setHeightThumbnail();

        // Prevent from re-adding a view multiple times
        if (player.getRootView().getParent() == null) {
            playerPlaceholder.addView(player.getRootView());
        }
    }

    private void removeVideoPlayerView() {
        makeDefaultHeightForVideoPlaceholder();
        playerService.removeViewFromParent();
    }

    private void makeDefaultHeightForVideoPlaceholder() {
        if (getView() == null) {
            return;
        }
        playerPlaceholder.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
        playerPlaceholder.requestLayout();
    }

    // Utils
    private void prepareDescription(final String descriptionHtml) {
        if (!TextUtils.isEmpty(descriptionHtml)) {
            disposables.add(Single.just(descriptionHtml).map(description -> {
                Spanned parsedDescription;
                if (Build.VERSION.SDK_INT >= 24) {
                    parsedDescription = Html.fromHtml(description, 0);
                } else {
                    parsedDescription = Html.fromHtml(description);
                }
                return parsedDescription;
            }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(spanned -> {
                videoDescriptionView.setText(spanned);
                videoDescriptionView.setVisibility(View.VISIBLE);
            }));
        }
    }

    private final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            final DisplayMetrics metrics = getResources().getDisplayMetrics();

            if (getView() != null) {
                final int height = isInMultiWindow() ? requireView().getHeight() : activity.getWindow().getDecorView().getHeight();
                setHeightThumbnail(height, metrics);
                getView().getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
            }
            return false;
        }
    };

    /**
     * Method which controls the size of thumbnail and the size of main player inside
     * a layout with thumbnail. It decides what height the player should have in both
     * screen orientations. It knows about multiWindow feature
     * and about videos with aspectRatio ZOOM (the height for them will be a bit higher,
     * {@link #MAX_PLAYER_HEIGHT})
     */
    private void setHeightThumbnail() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        final boolean isPortrait = metrics.heightPixels > metrics.widthPixels;
        requireView().getViewTreeObserver().removeOnPreDrawListener(preDrawListener);

        if (player != null && player.isFullscreen()) {
            final int height = isInMultiWindow() ? requireView().getHeight() : activity.getWindow().getDecorView().getHeight();
            // Height is zero when the view is not yet displayed like after orientation change
            if (height != 0) {
                setHeightThumbnail(height, metrics);
            } else {
                requireView().getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            }
        } else {
            final int height = isPortrait ? (int) (metrics.widthPixels / (16.0f / 9.0f)) : (int) (metrics.heightPixels / 2.0f);
            setHeightThumbnail(height, metrics);
        }
    }

    private void setHeightThumbnail(final int newHeight, final DisplayMetrics metrics) {
        thumbnailImageView.setLayoutParams(new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, newHeight));
        thumbnailImageView.setMinimumHeight(newHeight);
        if (player != null) {
            final int maxHeight = (int) (metrics.heightPixels * MAX_PLAYER_HEIGHT);
            player.getSurfaceView().setHeights(newHeight, player.isFullscreen() ? newHeight : maxHeight);
        }
    }

    private void showContent() {
        contentRootLayoutHiding.setVisibility(View.VISIBLE);
    }

    protected void setInitialData(int serviceId, String url, String name, final PlayQueue queue) {
        this.serviceId = serviceId;
        this.url = url;
        this.name = !TextUtils.isEmpty(name) ? name : "";
        playQueue = queue;
    }

    private void setErrorImage(final int imageResource) {
        if (thumbnailImageView != null) {
            thumbnailImageView.setImageDrawable(ContextCompat.getDrawable(activity, imageResource));
            animateView(thumbnailImageView, false, 0, 0, () -> animateView(thumbnailImageView, true, 0));
        }
    }

    @Override
    public void showError(String message, boolean showRetryButton) {
        showError(message, showRetryButton, R.drawable.savemasterdown_n_img);
    }

    protected void showError(String message, boolean showRetryButton, @DrawableRes int imageError) {
        super.showError(message, showRetryButton);
        setErrorImage(imageError);
    }

    private void setupBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                switch (intent.getAction()) {
                    case ACTION_SHOW_MAIN_PLAYER:
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        setBottomNavigationViewVisibility(View.GONE);
                        break;
                    case ACTION_HIDE_MAIN_PLAYER:
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        setBottomNavigationViewVisibility(View.VISIBLE);
                        break;
                    case ACTION_MINIMIZE_MAIN_PLAYER:
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        setBottomNavigationViewVisibility(View.VISIBLE);
                        break;
                    case ACTION_PLAYER_STARTED:
                        // If the state is not hidden we don't need to show the mini player
                        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            setBottomNavigationViewVisibility(View.VISIBLE);
                        }
                        // Rebound to the service if it was closed via notification or mini player
                        if (!PlayerHolder.bound) {
                            PlayerHolder.startService(App.getAppContext(), false, VideoDetailFragment.this);
                        }
                        break;
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SHOW_MAIN_PLAYER);
        intentFilter.addAction(ACTION_HIDE_MAIN_PLAYER);
        intentFilter.addAction(ACTION_MINIMIZE_MAIN_PLAYER);
        intentFilter.addAction(ACTION_PLAYER_STARTED);
        activity.registerReceiver(broadcastReceiver, intentFilter);
    }

    // Orientation listener
    private void restoreDefaultOrientation() {
        if (player == null || !player.videoPlayerSelected() || activity == null) {
            return;
        }

        if (player != null && player.isFullscreen()) {
            player.toggleFullscreen();
        }
        // This will show systemUI and pause the player.
        // User can tap on Play button and video will be in fullscreen mode again
        // Note for tablet: trying to avoid orientation changes since it's not easy
        // to physically rotate the tablet every time
        if (!DeviceUtils.isTablet(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    // Contract
    @Override
    public void showLoading() {
        super.showLoading();

        // if data is already cached, transition from VISIBLE -> INVISIBLE -> VISIBLE is not required
        if (!ExtractorHelper.isCached(serviceId, url, InfoItem.InfoType.STREAM)) {
            contentRootLayoutHiding.setVisibility(View.INVISIBLE);
        }

        animateView(thumbnailPlayButton, false, 50);
        animateView(detailDurationView, false, 100);
        videoTitleTextView.setText(name != null ? name : "");
        videoTitleTextView.setMaxLines(2);
        animateView(videoTitleTextView, true, 0);

        videoDescriptionRootLayout.setVisibility(View.GONE);
        videoTitleToggleArrow.setImageResource(R.drawable.savemasterdown_ic_arrow_down);
        videoTitleToggleArrow.setVisibility(View.GONE);
        videoTitleRoot.setClickable(false);

        if (relatedStreamsView != null) {
            relatedStreamsView.setVisibility(player != null && player.isFullscreen() ? View.GONE : View.INVISIBLE);
        }

        uploaderThumb.setImageBitmap(null);
    }

    @SuppressLint("CheckResult")
    @Override
    public void handleResult(@NonNull StreamInfo streamInfo) {
        super.handleResult(streamInfo);

        currentInfo = streamInfo;
        setInitialData(streamInfo.getServiceId(), streamInfo.getOriginalUrl(), streamInfo.getName(), playQueue);

        animateView(thumbnailPlayButton, true, 200);
        videoTitleTextView.setText(name);

        if (!TextUtils.isEmpty(streamInfo.getUploaderName())) {
            uploaderTextView.setText(streamInfo.getUploaderName());
            uploaderTextView.setVisibility(View.VISIBLE);
            uploaderTextView.setSelected(true);
        } else {
            uploaderTextView.setVisibility(View.GONE);
        }

        if (streamInfo.getViewCount() >= 0) {
            if (streamInfo.getStreamType().equals(StreamType.AUDIO_LIVE_STREAM)) {
                videoCountView.setText(Localization.listeningCount(activity, streamInfo.getViewCount()));
            } else if (streamInfo.getStreamType().equals(StreamType.LIVE_STREAM)) {
                videoCountView.setText(Localization.localizeWatchingCount(activity, streamInfo.getViewCount()));
            } else {
                videoCountView.setText(Localization.localizeViewCount(activity, streamInfo.getViewCount()));
            }
            videoCountView.setVisibility(View.VISIBLE);
        } else {
            videoCountView.setVisibility(View.GONE);
        }

        // get channel's subscribers
        if (subscribeButtonMonitor != null) subscribeButtonMonitor.dispose();
        ExtractorHelper.getChannelInfo(this.serviceId, streamInfo.getUploaderUrl(), true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                        // onNext
                        channelInfo -> {
                            if (activity != null) {
                                uploaderSubscriberTextView.setVisibility(channelInfo.getSubscriberCount() > 0 ? View.VISIBLE : View.GONE);
                                uploaderSubscriberTextView.setText(Localization.localizeSubscribersCount(activity, channelInfo.getSubscriberCount()));
                            }
                            updateSubscription(channelInfo);
                            monitorSubscription(channelInfo);
                        },
                        // onError
                        throwable -> {
                            uploaderSubscriberTextView.setVisibility(View.GONE);
                            uploaderSubscriberTextView.setText(R.string.savemasterdown_unknown_content);
                        });

        if (streamInfo.getDuration() > 0) {
            detailDurationView.setText(Localization.getDurationString(streamInfo.getDuration()));
            detailDurationView.setBackgroundResource(R.drawable.savemasterdown_duration_bg);
            animateView(detailDurationView, true, 100);
        } else if (streamInfo.getStreamType() == StreamType.LIVE_STREAM) {
            detailDurationView.setText(R.string.savemasterdown_duration_live);
            detailDurationView.setBackgroundResource(R.drawable.savemasterdown_duration_bg_live);
            animateView(detailDurationView, true, 100);
        } else {
            detailDurationView.setVisibility(View.GONE);
        }

        videoTitleRoot.setClickable(true);
        videoTitleToggleArrow.setVisibility(View.VISIBLE);
        videoTitleToggleArrow.setImageResource(R.drawable.savemasterdown_ic_arrow_down);
        videoDescriptionView.setVisibility(View.GONE);
        videoDescriptionRootLayout.setVisibility(View.GONE);
        if (streamInfo.getUploadDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(streamInfo.getUploadDate().offsetDateTime().toInstant()));
            videoUploadDateView.setText(Localization.localizeDate(activity, calendar.getTime()));
        }

        prepareDescription(streamInfo.getDescription().getContent());
        initRelatedVideos(streamInfo);
        initThumbnailViews(streamInfo);

        if (player == null || player.isPlayerStopped()) {
            updateOverlayData(streamInfo.getName(), streamInfo.getUploaderName(), streamInfo.getThumbnailUrl());
        }

//		if (!streamInfo.getErrors().isEmpty()) {
//			showSnackBarError(streamInfo.getErrors(), UserAction.REQUESTED_STREAM, NewPipe.getNameOfService(streamInfo.getServiceId()), streamInfo.getUrl(), 0);
//		}

        final boolean noVideoStreams = streamInfo.getVideoStreams().isEmpty() && streamInfo.getVideoOnlyStreams().isEmpty();
        detailControlsPopup.setVisibility(noVideoStreams ? View.GONE : View.VISIBLE);
        thumbnailPlayButton.setImageResource(noVideoStreams ? R.drawable.savemasterdown_ic_headset_white_shadow_24dp : R.drawable.savemasterdown_ic_play_arrow_white_shadow_24dp);
    }

    private void showAgeRestrictedContent() {
        messageRestricted.setVisibility(View.VISIBLE);
        if (relatedStreamsView != null) {
            relatedStreamsView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected boolean onError(Throwable exception) {
        if (super.onError(exception)) return true;
        if (exception instanceof ContentNotAvailableException) {
            showError(getString(R.string.savemasterdown_content_not_available), false);
        }
//		else {
//			int errorId = exception instanceof YoutubeStreamExtractor.DeobfuscateException
//					? R.string.youtube_signature_decryption_error
//					: exception instanceof ParsingException
//					? R.string.parsing_error
//					: R.string.general_error;
//			onUnrecoverableError(exception, UserAction.REQUESTED_STREAM, NewPipe.getNameOfService(serviceId), url, errorId);
//		}
        return true;
    }

    @Override
    public void onQueueUpdate(PlayQueue queue) {
        playQueue = queue;
        // This should be the only place where we push data to stack.
        // It will allow to have live instance of PlayQueue with actual information about
        // deleted/added items inside Channel/Playlist queue and makes possible to have
        // a history of played items
        if ((stack.isEmpty() || !stack.peek().getPlayQueue().equals(queue) && queue.getItem() != null)) {
            stack.push(new StackItem(queue.getItem().getServiceId(), queue.getItem().getUrl(), queue.getItem().getTitle(), queue));
        } else {
            final StackItem stackWithQueue = findQueueInStack(queue);
            if (stackWithQueue != null) {
                // On every MainPlayer service's destroy() playQueue gets disposed and
                // no longer able to track progress. That's why we update our cached disposed
                // queue with the new one that is active and have the same history.
                // Without that the cached playQueue will have an old recovery position
                stackWithQueue.setPlayQueue(queue);
            }
        }
    }

    @Override
    public void onPlaybackUpdate(int state, int repeatMode, boolean shuffled, PlaybackParameters parameters) {
        setOverlayPlayPauseImage(player != null && player.isPlaying());
    }

    @Override
    public void onProgressUpdate(int currentProgress, int duration, int bufferPercent) {

    }

    @Override
    public void onMetadataUpdate(StreamInfo info, PlayQueue queue) {
        final StackItem item = findQueueInStack(queue);
        if (item != null) {
            // When PlayQueue can have multiple streams (PlaylistPlayQueue or ChannelPlayQueue)
            // every new played stream gives new title and url.
            // StackItem contains information about first played stream. Let's update it here
            item.setTitle(info.getName());
            item.setUrl(info.getUrl());
        }
        // They are not equal when user watches something in popup while browsing in fragment and
        // then changes screen orientation. In that case the fragment will set itself as
        // a service listener and will receive initial call to onMetadataUpdate()
        if (!queue.equals(playQueue)) {
            return;
        }

        updateOverlayData(info.getName(), info.getUploaderName(), info.getThumbnailUrl());
        if (currentInfo != null && info.getUrl().equals(currentInfo.getUrl())) {
            return;
        }

        currentInfo = info;
        setInitialData(info.getServiceId(), info.getUrl(), info.getName(), queue);
        setAutoplay(true);
        // Delay execution just because it freezes the main thread, and while playing
        // next/previous video you see visual glitches
        // (when non-vertical video goes after vertical video)
        prepareAndHandleInfoIfNeededAfterDelay(info, true, 200);
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (error.type == ExoPlaybackException.TYPE_SOURCE || error.type == ExoPlaybackException.TYPE_UNEXPECTED) {
            // Properly exit from fullscreen
            if (playerService != null && player.isFullscreen()) {
                player.toggleFullscreen();
            }
            hideMainPlayer();
        }
    }

    @Override
    public void onServiceStopped() {
        setOverlayPlayPauseImage(false);
        if (currentInfo != null) {
            updateOverlayData(currentInfo.getName(), currentInfo.getUploaderName(), currentInfo.getThumbnailUrl());
        }
    }

    @Override
    public void onFullscreenStateChanged(boolean fullscreen) {
        if (playerService.getView() == null || player.getParentActivity() == null) {
            return;
        }

        final View view = playerService.getView();
        final ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) {
            return;
        }

        if (fullscreen) {
            hideSystemUiIfNeeded();
        } else {
            showSystemUi();
        }

        if (relatedStreamsView != null) {
            relatedStreamsView.setVisibility(fullscreen ? View.GONE : View.VISIBLE);
        }
        scrollToTop();
        addVideoPlayerView();

        ViewGroup.LayoutParams params = videoPlayerLayout.getLayoutParams();
        ConstraintLayout.LayoutParams frameVideoPlayerParams = (ConstraintLayout.LayoutParams) frameVideoPlayer.getLayoutParams();
        if (player.isVerticalVideo()) {
            if (fullscreen) {
                params.height = MATCH_PARENT;
                frameVideoPlayerParams.dimensionRatio = "H,9:16";
            } else {
                params.height = WRAP_CONTENT;
                frameVideoPlayerParams.dimensionRatio = "H,16:9";
            }
        } else {
            if (fullscreen) {
                params.height = MATCH_PARENT;
                frameVideoPlayerParams.dimensionRatio = null;
            } else {
                params.height = WRAP_CONTENT;
                frameVideoPlayerParams.dimensionRatio = "H,16:9";
            }
        }
        videoPlayerLayout.setLayoutParams(params);
        frameVideoPlayer.setLayoutParams(frameVideoPlayerParams);
    }

    @Override
    public void onScreenRotationButtonClicked() {
        // In tablet user experience will be better if screen will not be rotated
        // from landscape to portrait every time.
        // Just turn on fullscreen mode in landscape orientation
        // or portrait & unlocked global orientation
        final boolean isLandscape = DeviceUtils.isLandscape(requireContext());
        if (DeviceUtils.isTablet(activity) && (!globalScreenOrientationLocked(activity) || isLandscape)) {
            player.toggleFullscreen();
            return;
        }
        final int newOrientation = isLandscape ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        activity.setRequestedOrientation(newOrientation);
    }

    private void showSystemUi() {
        if (activity == null) {
            return;
        }

        // Prevent jumping of the player on devices with cutout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
        }
        activity.getWindow().getDecorView().setSystemUiVisibility(0);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().setStatusBarColor(ThemeHelper.resolveColorFromAttr(activity, android.R.attr.colorPrimary));
    }

    private void hideSystemUi() {
        if (activity == null) {
            return;
        }
        // Prevent jumping of the player on devices with cutout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
        }
        int visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        // In multiWindow mode status bar is not transparent for devices with cutout
        // if I include this flag. So without it is better in this case
        if (!isInMultiWindow()) {
            visibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        activity.getWindow().getDecorView().setSystemUiVisibility(visibility);

        if (isInMultiWindow() || player != null && player.isFullscreen()) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    // Listener implementation
    public void hideSystemUiIfNeeded() {
        if (player != null && player.isFullscreen() && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            hideSystemUi();
        }
    }

    private boolean playerIsNotStopped() {
        return player != null && player.getPlayer() != null && player.getPlayer().getPlaybackState() != Player.STATE_IDLE;
    }

    private void checkLandscape() {
        if ((!player.isPlaying() && player.getPlayQueue() != playQueue) || player.getPlayQueue() == null) {
            setAutoplay(true);
        }

        player.checkLandscape();
        // Let's give a user time to look at video information page if video is not playing
        if (globalScreenOrientationLocked(activity) && !player.isPlaying()) {
            player.onPlay();
        }
    }

    private boolean isInMultiWindow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode();
    }

    /*
     * Means that the player fragment was swiped away via BottomSheetLayout
     * and is empty but ready for any new actions. See cleanUp()
     * */
    private boolean wasCleared() {
        return url == null;
    }

    private StackItem findQueueInStack(final PlayQueue queue) {
        StackItem item = null;
        final Iterator<StackItem> iterator = stack.descendingIterator();
        while (iterator.hasNext()) {
            final StackItem next = iterator.next();
            if (next.getPlayQueue().equals(queue)) {
                item = next;
                break;
            }
        }
        return item;
    }

    // Remove unneeded information while waiting for a next task
    private void cleanUp() {
        // New beginning
        stack.clear();
        if (currentWorker != null) {
            currentWorker.dispose();
        }
        PlayerHolder.stopService(App.getAppContext());
        setInitialData(0, null, "", null);
        currentInfo = null;
        updateOverlayData(null, null, null);
    }

    /**
     * Move focus from main fragment to the player or back
     * based on what is currently selected
     *
     * @param toMain if true than the main fragment will be focused or the player otherwise
     */
    private void moveFocusToMainFragment(final boolean toMain) {
        final ViewGroup mainFragment = requireActivity().findViewById(R.id.fragment_holder);
        // Hamburger button steels a focus even under bottomSheet
        final Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        final int afterDescendants = ViewGroup.FOCUS_AFTER_DESCENDANTS;
        final int blockDescendants = ViewGroup.FOCUS_BLOCK_DESCENDANTS;
        if (toolbar != null) {
            if (toMain) {
                mainFragment.setDescendantFocusability(afterDescendants);
                toolbar.setDescendantFocusability(afterDescendants);
                ((ViewGroup) requireView()).setDescendantFocusability(blockDescendants);
                mainFragment.requestFocus();
            } else {
                mainFragment.setDescendantFocusability(blockDescendants);
                toolbar.setDescendantFocusability(blockDescendants);
                ((ViewGroup) requireView()).setDescendantFocusability(afterDescendants);
            }
        }
    }

    @OnCheckedChanged(R.id.switch_auto_play)
    void onSwitchAutoPlayChecked(boolean checked) {
        PlayerHelper.setAutoQueueEnabled(activity, checked);
    }

    private void showPopupMenu(final StreamInfoItem streamInfoItem, final View view) {

        PopupMenu popup = new PopupMenu(activity, view, Gravity.END, 0, R.style.mPopupMenu);
        popup.getMenuInflater().inflate(R.menu.savemasterdown_menu_popup_detail, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(item -> {

            int id = item.getItemId();
            final int index = Math.max(currentInfo.getRelatedStreams().indexOf(streamInfoItem), 0);
            switch (id) {

                case R.id.action_play:
                    AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnMainPlayer(activity, getPlayQueue(index), true));
                    break;

                case R.id.action_append_playlist:
                    if (getFragmentManager() != null) {
                        PlaylistAppendDialog.fromStreamInfoItems(Collections.singletonList(streamInfoItem)).show(getFragmentManager(), TAG);
                    }
                    break;

                case R.id.action_share:
                    SharedUtils.shareUrl(activity);
                    break;
            }
            return true;
        });
    }

    private PlayQueue getPlayQueue(final int index) {

        final List<InfoItem> infoItems = currentInfo.getRelatedStreams();
        List<StreamInfoItem> streamInfoItems = new ArrayList<>(infoItems.size());

        for (final InfoItem item : infoItems) {
            if (item instanceof StreamInfoItem) {
                streamInfoItems.add((StreamInfoItem) item);
            }
        }
        return new SinglePlayQueue(streamInfoItems, index);
    }

    private void initRelatedVideos(StreamInfo streamInfo) {

        if (relatedStreamsView.getChildCount() > 0) relatedStreamsView.removeAllViews();

        if (streamInfo.getRelatedStreams() != null && !streamInfo.getRelatedStreams().isEmpty()) {
            nextStreamTitleView.setVisibility(View.VISIBLE);
            relatedStreamsView.setVisibility(View.VISIBLE);
            // set next video
            streamInfo.setVideoStreams(streamInfo.getVideoStreams());

            int maxRelatedVideos = Math.min(streamInfo.getRelatedStreams().size(), INITIAL_RELATED_VIDEOS);
            for (int i = 0; i < maxRelatedVideos; i++) {
                InfoItem infoItem = streamInfo.getRelatedStreams().get(i);
                relatedStreamsView.addView(infoItemBuilder.buildView(relatedStreamsView, infoItem, true));
            }
        } else {
            nextStreamTitleView.setVisibility(View.GONE);
            relatedStreamsView.setVisibility(View.GONE);
        }
    }

    private void monitorSubscription(final ChannelInfo info) {

        final Consumer<Throwable> onError = throwable -> {
            animateView(channelSubscribeButton, false, 100);
        };

        final Observable<List<SubscriptionEntity>> observable = subscriptionService.subscriptionTable()
                .getSubscription(info.getServiceId(), info.getUrl())
                .toObservable();

        disposables.add(observable.observeOn(AndroidSchedulers.mainThread()).subscribe(getSubscribeUpdateMonitor(info), onError));

        disposables.add(observable
                // Some updates are very rapid (when calling the updateSubscription(info), for example)
                // so only update the UI for the latest emission ("sync" the subscribe button's state)
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriptionEntities -> updateSubscribeButton(!subscriptionEntities.isEmpty()), onError));

    }

    private Function<Object, Object> mapOnSubscribe(final SubscriptionEntity subscription) {

        return object -> {
            subscriptionService.subscriptionTable().insert(subscription);
            return object;
        };
    }

    private Function<Object, Object> mapOnUnsubscribe(final SubscriptionEntity subscription) {

        return object -> {
            subscriptionService.subscriptionTable().delete(subscription);
            return object;
        };
    }

    private void updateSubscription(final ChannelInfo info) {

        final Action onComplete = () -> {
        };

        final Consumer<Throwable> onError = throwable -> {
        };

        disposables.add(subscriptionService.updateChannelInfo(info)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onComplete, onError));
    }

    private Disposable monitorSubscribeButton(final Button subscribeButton, final Function<Object, Object> action) {

        final Consumer<Object> onNext = object -> {
        };

        final Consumer<Throwable> onError = throwable -> {
        };

        /* Emit clicks from main thread unto io thread */
        return RxView.clicks(subscribeButton)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .debounce(100, TimeUnit.MILLISECONDS) // Ignore rapid clicks
                .map(action)
                .subscribe(onNext, onError);
    }

    private Consumer<List<SubscriptionEntity>> getSubscribeUpdateMonitor(final ChannelInfo info) {

        return subscriptionEntities -> {

            if (subscribeButtonMonitor != null) subscribeButtonMonitor.dispose();

            if (subscriptionEntities.isEmpty()) {
                SubscriptionEntity channel = new SubscriptionEntity();
                channel.setServiceId(info.getServiceId());
                channel.setUrl(info.getUrl());
                channel.setData(info.getName(), info.getAvatarUrl(), info.getDescription(), info.getSubscriberCount());
                subscribeButtonMonitor = monitorSubscribeButton(channelSubscribeButton, mapOnSubscribe(channel));
            } else {
                final SubscriptionEntity subscription = subscriptionEntities.get(0);
                subscribeButtonMonitor = monitorSubscribeButton(channelSubscribeButton, mapOnUnsubscribe(subscription));
            }
        };
    }

    private void updateSubscribeButton(boolean isSubscribed) {

        boolean isButtonVisible = channelSubscribeButton.getVisibility() == View.VISIBLE;
        int backgroundDuration = isButtonVisible ? 100 : 0;
        int textDuration = isButtonVisible ? 100 : 0;

        int subscribeBackground = ContextCompat.getColor(activity, R.color.savemasterdown_subscribe_background_color);
        int subscribeText = ContextCompat.getColor(activity, R.color.savemasterdown_subscribe_text_color);
        int subscribedBackground = ContextCompat.getColor(activity, R.color.savemasterdown_subscribed_background_color);
        int subscribedText = ContextCompat.getColor(activity, R.color.savemasterdown_subscribed_text_color);

        if (!isSubscribed) {
            channelSubscribeButton.setText(R.string.savemasterdown_subscribe_button_title);
            AnimationUtils.animateBackgroundColor(channelSubscribeButton, backgroundDuration, subscribedBackground, subscribeBackground);
            AnimationUtils.animateTextColor(channelSubscribeButton, textDuration, subscribedText, subscribeText);
        } else {
            channelSubscribeButton.setText(R.string.savemasterdown_subscribed_button_title);
            AnimationUtils.animateBackgroundColor(channelSubscribeButton, backgroundDuration, subscribeBackground, subscribedBackground);
            AnimationUtils.animateTextColor(channelSubscribeButton, textDuration, subscribeText, subscribedText);
        }

        animateView(channelSubscribeButton, AnimationUtils.Type.LIGHT_SCALE_AND_ALPHA, true, 100);
    }

    // Bottom mini player
    private void setupBottomPlayer() {
        final FrameLayout bottomSheetLayout = activity.findViewById(R.id.fragment_player_holder);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        try{
            bottomSheetBehavior.setState(bottomSheetState);
        }catch(Exception e){

        }

        final int peekHeight = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_height);
        if (bottomSheetState != BottomSheetBehavior.STATE_HIDDEN) {
            //manageSpaceAtTheBottom(false);
            bottomSheetBehavior.setPeekHeight(peekHeight);
            if (bottomSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
                overlay.setAlpha(MAX_OVERLAY_ALPHA);
                setBottomNavigationViewAlpha(MAX_OVERLAY_ALPHA);
                setBottomNavigationViewVisibility(View.VISIBLE);
            } else if (bottomSheetState == BottomSheetBehavior.STATE_EXPANDED) {
                overlay.setAlpha(0);
                setOverlayElementsClickable(false);
                setBottomNavigationViewAlpha(0);
                setBottomNavigationViewVisibility(View.GONE);
            }
        }

        bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
                bottomSheetState = newState;

                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        moveFocusToMainFragment(true);
                        //manageSpaceAtTheBottom(true);
                        bottomSheetBehavior.setPeekHeight(0);
                        setBottomNavigationViewVisibility(View.VISIBLE);
                        cleanUp();
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        moveFocusToMainFragment(false);
                        //manageSpaceAtTheBottom(false);
                        bottomSheetBehavior.setPeekHeight(peekHeight);
                        setBottomNavigationViewVisibility(View.GONE);
                        // Disable click because overlay buttons located on top of buttons
                        // from the player
                        setOverlayElementsClickable(false);
                        hideSystemUiIfNeeded();
                        // Conditions when the player should be expanded to fullscreen
                        if (DeviceUtils.isLandscape(requireContext())
                                && player != null
                                && player.isPlaying()
                                && !player.isFullscreen()
                                && !DeviceUtils.isTablet(activity)
                                && player.videoPlayerSelected()) {
                            player.toggleFullscreen();
                        }
                        setOverlayLook(1);
                        setBottomNavigationViewLook(1);
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        moveFocusToMainFragment(true);
                        //manageSpaceAtTheBottom(false);
                        bottomSheetBehavior.setPeekHeight(peekHeight);
                        setBottomNavigationViewVisibility(View.VISIBLE);

                        // Re-enable clicks
                        setOverlayElementsClickable(true);
                        if (player != null) {
                            player.onQueueClosed();
                        }
                        setOverlayLook(0);
                        setBottomNavigationViewLook(0);
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_SETTLING:
                        if (player != null && player.isFullscreen()) {
                            hideSystemUi();
                        }
                        if (player != null && player.isControlsVisible()) {
                            player.hideControls(0, 0);
                        }
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull final View bottomSheet, final float slideOffset) {
                setOverlayLook(slideOffset);
                setBottomNavigationViewLook(slideOffset);
            }
        };
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback);

        // User opened a new page and the player will hide itself
        activity.getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                setBottomNavigationViewVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * When the mini player exists the view underneath it is not touchable.
     * Bottom padding should be equal to the mini player's height in this case
     *
     * @param showMore whether main fragment should be expanded or not
     */
    private void manageSpaceAtTheBottom(final boolean showMore) {
        final int peekHeight = getResources().getDimensionPixelSize(R.dimen.mini_player_height);
        final ViewGroup holder = activity.findViewById(R.id.fragment_holder);
        final int newBottomPadding;
        if (showMore) {
            newBottomPadding = 0;
        } else {
            newBottomPadding = peekHeight;
        }
        if (holder.getPaddingBottom() == newBottomPadding) {
            return;
        }
        holder.setPadding(holder.getPaddingLeft(), holder.getPaddingTop(), holder.getPaddingRight(), newBottomPadding);
    }

    private void updateOverlayData(@Nullable final String title, @Nullable final String uploader, @Nullable final String thumbnailUrl) {
        overlayTitleTextView.setText(TextUtils.isEmpty(title) ? "" : title);
        overlayChannelTextView.setText(TextUtils.isEmpty(uploader) ? "" : uploader);
        if (!TextUtils.isEmpty(thumbnailUrl)) {
            GlideUtils.loadThumbnail(App.getAppContext(), overlayThumbnailImageView, thumbnailUrl);
        }
    }

    private void setOverlayPlayPauseImage(final boolean playerIsPlaying) {
        final int attr = playerIsPlaying ? R.attr.pause : R.attr.play;
        overlayPlayPauseButton.setImageResource(ThemeHelper.resolveResourceIdFromAttr(activity, attr));
    }

    private void setOverlayLook(final float slideOffset) {
        // SlideOffset < 0 when mini player is about to close via swipe.
        // Stop animation in this case
        if (slideOffset < 0) {
            return;
        }
        overlay.setAlpha(Math.min(MAX_OVERLAY_ALPHA, 1 - slideOffset));
    }

    private void setOverlayElementsClickable(final boolean enable) {
        overlayThumbnailImageView.setClickable(enable);
        overlayMetadata.setClickable(enable);
        overlayButtons.setClickable(enable);
        overlayPlayPauseButton.setClickable(enable);
        overlayCloseButton.setClickable(enable);
    }

    private void setBottomNavigationViewLook(final float slideOffset) {
        if (slideOffset < 0) return;
        setBottomNavigationViewAlpha(Math.min(MAX_OVERLAY_ALPHA, 1 - slideOffset));
    }

    private void showBannerAd() {
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                // Code to be executed when an ad finishes loading.
//                adView.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                // Code to be executed when an ad request fails.
//                adView.setVisibility(View.GONE);
//            }
//        });
//        adView.loadAd(adRequest);
    }

    MyCommon myCommon = new MyCommon();
    private void showNativeAd() {
        if(getActivity() == null || getActivity().isFinishing()){
            return;
        }
        myCommon.loadBigNative(getActivity(), nativeAdView);
    }
}