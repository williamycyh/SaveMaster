package com.savemaster.savefromfb.player.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.accessibility.CaptioningManager;

import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.util.MimeTypes;

import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.MediaFormat;
import savemaster.save.master.pipd.stream.AudioStream;
import savemaster.save.master.pipd.stream.StreamInfo;
import savemaster.save.master.pipd.stream.StreamInfoItem;
import savemaster.save.master.pipd.stream.SubtitlesStream;
import savemaster.save.master.pipd.stream.VideoStream;
import savemaster.save.master.pipd.Utils;

import java.lang.annotation.Retention;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import com.savemaster.savefromfb.R;
import com.savemaster.savefromfb.player.playqueue.PlayQueue;
import com.savemaster.savefromfb.player.playqueue.PlayQueueItem;
import com.savemaster.savefromfb.player.playqueue.SinglePlayQueue;

import static com.savemaster.savefromfb.player.helper.PlayerHelper.MinimizeMode.MINIMIZE_ON_EXIT_MODE_POPUP;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public class PlayerHelper {

    private PlayerHelper() {
    }

    private static final StringBuilder stringBuilder = new StringBuilder();
    @SuppressLint("ConstantLocale")
    private static final Formatter stringFormatter = new Formatter(stringBuilder, Locale.getDefault());
    private static final NumberFormat speedFormatter = new DecimalFormat("0.##x");

    @Retention(SOURCE)
    @IntDef({MINIMIZE_ON_EXIT_MODE_POPUP})
    public @interface MinimizeMode {
        int MINIMIZE_ON_EXIT_MODE_POPUP = 0;
    }

    public static String getTimeString(int milliSeconds) {

        int seconds = (milliSeconds % 60000) / 1000;
        int minutes = (milliSeconds % 3600000) / 60000;
        int hours = (milliSeconds % 86400000) / 3600000;
        int days = (milliSeconds % (86400000 * 7)) / 86400000;

        stringBuilder.setLength(0);
        return days > 0 ? stringFormatter.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds).toString()
                : hours > 0 ? stringFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : stringFormatter.format("%02d:%02d", minutes, seconds).toString();
    }

    public static String formatSpeed(double speed) {
        return speedFormatter.format(speed);
    }

    public static String subtitleMimeTypesOf(final MediaFormat format) {

        switch (format) {
            case VTT:
                return MimeTypes.TEXT_VTT;
            case TTML:
                return MimeTypes.APPLICATION_TTML;
            default:
                throw new IllegalArgumentException("Unrecognized mime type: " + format.name());
        }
    }

    @NonNull
    public static String captionLanguageOf(@NonNull final Context context, @NonNull final SubtitlesStream subtitles) {

        final String displayName = subtitles.getLocale().getDisplayName(subtitles.getLocale());
        return displayName + (subtitles.isAutoGenerated() ? " (" + context.getString(R.string.caption_auto_generated) + ")" : "");
    }

    @NonNull
    public static String cacheKeyOf(@NonNull final StreamInfo info, @NonNull VideoStream video) {
        return info.getUrl() + video.getResolution() + video.getFormat().getName();
    }

    @NonNull
    public static String cacheKeyOf(@NonNull final StreamInfo info, @NonNull AudioStream audio) {
        return info.getUrl() + audio.getAverageBitrate() + audio.getFormat().getName();
    }

    /**
     * Given a {@link StreamInfo} and the existing queue items, provide the
     * {@link SinglePlayQueue} consisting of the next video for auto queuing.
     * <br><br>
     * This method detects and prevents cycle by naively checking if a
     * candidate next video's url already exists in the existing items.
     * <br><br>
     * To select the next video, {@link StreamInfo#getRelatedStreams()} ()} is checked first.
     * If it is non-null and is not part of the existing items, it will be used as the next video.
     * Otherwise, an random item with non-repeating url will be selected from the {@link StreamInfo#getRelatedStreams()}.
     */
    @Nullable
    public static PlayQueue autoQueueOf(@NonNull final StreamInfo info, @NonNull final List<PlayQueueItem> existingItems) {

        final Set<String> urls = new HashSet<>(existingItems.size());
        for (final PlayQueueItem item : existingItems) {
            urls.add(item.getUrl());
        }

        final List<InfoItem> relatedItems = info.getRelatedStreams();
        if (Utils.isNullOrEmpty(relatedItems)) return null;

        if (relatedItems.get(0) != null && relatedItems.get(0) instanceof StreamInfoItem
                && !urls.contains(relatedItems.get(0).getUrl())) {
            return getAutoQueuedSinglePlayQueue((StreamInfoItem) relatedItems.get(0));
        }

        final List<StreamInfoItem> autoQueueItems = new ArrayList<>();
        for (final InfoItem item : relatedItems) {
            if (item instanceof StreamInfoItem && !urls.contains(item.getUrl())) {
                autoQueueItems.add((StreamInfoItem) item);
            }
        }
        Collections.shuffle(autoQueueItems);
        return autoQueueItems.isEmpty() ? null : getAutoQueuedSinglePlayQueue(autoQueueItems.get(0));
    }

    public static boolean isRememberingPopupDimensions(@NonNull final Context context) {
        return isRememberingPopupDimensions(context, true);
    }

    public static boolean isAutoQueueEnabled(@NonNull final Context context) {
        return isAutoQueueEnabled(context, false);
    }

    @MinimizeMode
    public static int getMinimizeOnExitAction(@NonNull final Context context) {
        return MINIMIZE_ON_EXIT_MODE_POPUP;
    }

    public static long getPreferredCacheSize() {
        return 64 * 1024 * 1024L;
    }

    public static long getPreferredFileSize() {
        return 512 * 1024L;
    }

    /**
     * Returns the number of milliseconds the player buffers for before starting playback.
     */
    public static int getPlaybackStartBufferMs() {
        return 500;
    }

    /**
     * Returns the minimum number of milliseconds the player always buffers to after starting playback.
     */
    public static int getPlaybackMinimumBufferMs() {
        return 25000;
    }

    /**
     * Returns the maximum/optimal number of milliseconds the player will buffer to once the buffer
     * hits the point of {@link #getPlaybackMinimumBufferMs()}.
     */
    public static int getPlaybackOptimalBufferMs() {
        return 60000;
    }

    public static TrackSelection.Factory getQualitySelector() {
        return new AdaptiveTrackSelection.Factory(1000,
												  AdaptiveTrackSelection.DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
												  AdaptiveTrackSelection.DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS,
												  AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION);
    }

    public static int getTossFlingVelocity() {
        return 2500;
    }

    @NonNull
    public static CaptionStyleCompat getCaptionStyle(@NonNull final Context context) {

        final CaptioningManager captioningManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        if (captioningManager == null || !captioningManager.isEnabled()) {
            return CaptionStyleCompat.DEFAULT;
        }

        return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
    }

    /**
     * System font scaling:
     * Very small - 0.25f, Small - 0.5f, Normal - 1.0f, Large - 1.5f, Very Large - 2.0f
     */
    public static float getCaptionScale(@NonNull final Context context) {

        final CaptioningManager captioningManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        if (captioningManager == null || !captioningManager.isEnabled()) {
            return 1f;
        }

        return captioningManager.getFontScale();
    }

    public static boolean globalScreenOrientationLocked(final Context context) {
        // 1: Screen orientation changes using accelerometer
        // 0: Screen orientation is locked
        return Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0;
    }

    @NonNull
    private static SharedPreferences getPreferences(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static boolean isRememberingPopupDimensions(@NonNull final Context context, final boolean b) {
        return getPreferences(context).getBoolean(context.getString(R.string.popup_remember_size_pos_key), b);
    }

    private static boolean isAutoQueueEnabled(@NonNull final Context context, final boolean b) {
        return getPreferences(context).getBoolean(context.getString(R.string.auto_queue_key), b);
    }

    public static void setAutoQueueEnabled(@NonNull final Context context, final boolean enabled) {
        getPreferences(context).edit().putBoolean(context.getString(R.string.auto_queue_key), enabled).apply();
    }

    private static SinglePlayQueue getAutoQueuedSinglePlayQueue(StreamInfoItem streamInfoItem) {

        SinglePlayQueue singlePlayQueue = new SinglePlayQueue(streamInfoItem);
        singlePlayQueue.getItem().setAutoQueued(true);
        return singlePlayQueue;
    }
}