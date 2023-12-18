package com.savemaster.savefromfb.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.savemaster.savefromfb.util.ThemeHelper;

import com.savemaster.savefromfb.R;

/**
 * One service for all players.
 */
public final class UIMainPlayer extends Service {

    private VideoPlayerImpl playerImpl;
    private WindowManager windowManager;

    private final IBinder mBinder = new LocalBinder();

    public enum PlayerType {
        VIDEO,
        POPUP
    }

    // Notification
    static final String ACTION_CLOSE = "com.android.protube.player.MainPlayer.CLOSE";
    static final String ACTION_PLAY_PAUSE = "com.android.protube.player.MainPlayer.PLAY_PAUSE";
    static final String ACTION_OPEN_CONTROLS = "com.android.protube.player.MainPlayer.OPEN_CONTROLS";
    static final String ACTION_REPEAT = "com.android.protube.player.MainPlayer.REPEAT";
    static final String ACTION_PLAY_NEXT = "com.android.protube.player.MainPlayer.ACTION_PLAY_NEXT";
    static final String ACTION_PLAY_PREVIOUS = "com.android.protube.player.MainPlayer.ACTION_PLAY_PREVIOUS";
    static final String ACTION_FAST_REWIND = "com.android.protube.player.MainPlayer.ACTION_FAST_REWIND";
    static final String ACTION_FAST_FORWARD = "com.android.protube.player.MainPlayer.ACTION_FAST_FORWARD";
    static final String ACTION_SHUFFLE = "com.android.protube.player.MainPlayer.ACTION_SHUFFLE";
    public static final String ACTION_RECREATE_NOTIFICATION = "com.android.protube.player.MainPlayer.ACTION_RECREATE_NOTIFICATION";

    /*//////////////////////////////////////////////////////////////////////////
    // Service's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreate() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        ThemeHelper.setTheme(this);
        createView();
    }

    private void createView() {
        final View layout = View.inflate(this, R.layout.savemasterdown_player, null);

        playerImpl = new VideoPlayerImpl(this);
        playerImpl.setup(layout);
        playerImpl.shouldUpdateOnProgress = true;

        NotificationUtil.getInstance().createNotificationAndStartForeground(playerImpl, this);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()) && playerImpl.playQueue == null) {
            // Player is not working, no need to process media button's action
            return START_NOT_STICKY;
        }

        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()) || intent.getStringExtra(VideoPlayer.PLAY_QUEUE_KEY) != null) {
            NotificationUtil.getInstance().createNotificationAndStartForeground(playerImpl, this);
        }

        playerImpl.handleIntent(intent);
        if (playerImpl.mediaSessionManager != null) {
            playerImpl.mediaSessionManager.handleMediaButtonIntent(intent);
        }
        return START_NOT_STICKY;
    }

    public void stop(final boolean autoplayEnabled) {
        if (playerImpl.getPlayer() != null) {
            playerImpl.wasPlaying = playerImpl.getPlayer().getPlayWhenReady();
            // Releases wifi & cpu, disables keepScreenOn, etc.
            if (!autoplayEnabled) {
                playerImpl.onPause();
            }
            // We can't just pause the player here because it will make transition
            // from one stream to a new stream not smooth
            playerImpl.getPlayer().stop(false);
            playerImpl.setRecovery();
            // Android TV will handle back button in case controls will be visible
            // (one more additional unneeded click while the player is hidden)
            playerImpl.hideControls(0, 0);
            playerImpl.onQueueClosed();
            // Notification shows information about old stream but if a user selects
            // a stream from backStack it's not actual anymore
            // So we should hide the notification at all.
            // When autoplay enabled such notification flashing is annoying so skip this case
            if (!autoplayEnabled) {
                NotificationUtil.getInstance().cancelNotificationAndStopForeground(this);
            }
        }
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (!playerImpl.videoPlayerSelected()) {
            return;
        }
        onDestroy();
        // Unload from memory completely
        Runtime.getRuntime().halt(0);
    }

    @Override
    public void onDestroy() {
        onClose();
    }

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(AudioServiceLeak.preventLeakOf(base));
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    // Actions
    private void onClose() {
        if (playerImpl != null) {
            // Exit from fullscreen when user closes the player via notification
            if (playerImpl.isFullscreen()) {
                playerImpl.toggleFullscreen();
            }
            removeViewFromParent();

            playerImpl.setRecovery();
            playerImpl.savePlaybackState();
            playerImpl.stopActivityBinding();
            playerImpl.removePopupFromView();
            playerImpl.destroy();
        }

        NotificationUtil.getInstance().cancelNotificationAndStopForeground(this);
        stopSelf();
    }

    // Utils
    public boolean isLandscape() {
        // DisplayMetrics from activity context knows about MultiWindow feature
        // while DisplayMetrics from app context doesn't
        final DisplayMetrics metrics = (playerImpl != null && playerImpl.getParentActivity() != null) ? playerImpl.getParentActivity().getResources().getDisplayMetrics() : getResources().getDisplayMetrics();
        return metrics.heightPixels < metrics.widthPixels;
    }

    public View getView() {
        if (playerImpl == null) {
            return null;
        }

        return playerImpl.getRootView();
    }

    public void removeViewFromParent() {
        if (getView() != null && getView().getParent() != null) {
            if (playerImpl.getParentActivity() != null) {
                // This means view was added to fragment
                final ViewGroup parent = (ViewGroup) getView().getParent();
                parent.removeView(getView());
            } else {
                // This means view was added by windowManager for popup player
                windowManager.removeViewImmediate(getView());
            }
        }
    }

    public class LocalBinder extends Binder {

        public UIMainPlayer getService() {
            return UIMainPlayer.this;
        }

        public VideoPlayerImpl getPlayer() {
            return UIMainPlayer.this.playerImpl;
        }
    }
}
