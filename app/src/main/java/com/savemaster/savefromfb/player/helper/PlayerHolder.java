package com.savemaster.savefromfb.player.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.player.UIMainPlayer;
import com.savemaster.savefromfb.player.VideoPlayerImpl;
import com.savemaster.savefromfb.player.event.PlayerServiceEventListener;
import com.savemaster.savefromfb.player.event.PlayerServiceExtendedEventListener;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;

import savemaster.save.master.pipd.stream.StreamInfo;

import com.savemaster.savefromfb.player.playqueue.PlayQueue;

public final class PlayerHolder {

    private PlayerHolder() {
    }

    private static PlayerServiceExtendedEventListener listener;
    private static ServiceConnection serviceConnection;
    public static boolean bound;
    private static UIMainPlayer playerService;
    private static VideoPlayerImpl player;

    public static void setListener(final PlayerServiceExtendedEventListener newListener) {
        listener = newListener;
        // Force reload data from service
        if (player != null) {
            listener.onServiceConnected(player, playerService, false);
            startPlayerListener();
        }
    }

    public static void removeListener() {
        listener = null;
    }


    public static void startService(final Context context, final boolean playAfterConnect, final PlayerServiceExtendedEventListener newListener) {
        setListener(newListener);
        if (bound) {
            return;
        }
        // startService() can be called concurrently and it will give a random crashes
        // and NullPointerExceptions inside the service because the service will be
        // bound twice. Prevent it with unbinding first
        unbind(context);
        context.startService(new Intent(context, UIMainPlayer.class));
        serviceConnection = getServiceConnection(context, playAfterConnect);
        bind(context);
    }

    public static void stopService(final Context context) {
        unbind(context);
        context.stopService(new Intent(context, UIMainPlayer.class));
    }

    private static ServiceConnection getServiceConnection(final Context context, final boolean playAfterConnect) {
        return new ServiceConnection() {
            @Override
            public void onServiceDisconnected(final ComponentName compName) {
                unbind(context);
            }

            @Override
            public void onServiceConnected(final ComponentName compName, final IBinder service) {
                final UIMainPlayer.LocalBinder localBinder = (UIMainPlayer.LocalBinder) service;

                playerService = localBinder.getService();
                player = localBinder.getPlayer();
                if (listener != null) {
                    listener.onServiceConnected(player, playerService, playAfterConnect);
                }
                startPlayerListener();
            }
        };
    }

    private static void bind(final Context context) {
        final Intent serviceIntent = new Intent(context, UIMainPlayer.class);
        bound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        if (!bound) {
            context.unbindService(serviceConnection);
        }
    }

    private static void unbind(final Context context) {
        if (bound) {
            context.unbindService(serviceConnection);
            bound = false;
            stopPlayerListener();
            playerService = null;
            player = null;
            if (listener != null) {
                listener.onServiceDisconnected();
            }
        }
    }


    private static void startPlayerListener() {
        if (player != null) {
            player.setFragmentListener(INNER_LISTENER);
        }
    }

    private static void stopPlayerListener() {
        if (player != null) {
            player.removeFragmentListener(INNER_LISTENER);
        }
    }


    private static final PlayerServiceEventListener INNER_LISTENER = new PlayerServiceEventListener() {
        @Override
        public void onFullscreenStateChanged(final boolean fullscreen) {
            if (listener != null) {
                listener.onFullscreenStateChanged(fullscreen);
            }
        }

        @Override
        public void onScreenRotationButtonClicked() {
            if (listener != null) {
                listener.onScreenRotationButtonClicked();
            }
        }

        @Override
        public void onPlayerError(final ExoPlaybackException error) {
            if (listener != null) {
                listener.onPlayerError(error);
            }
        }

        @Override
        public void hideSystemUiIfNeeded() {
            if (listener != null) {
                listener.hideSystemUiIfNeeded();
            }
        }

        @Override
        public void onQueueUpdate(final PlayQueue queue) {
            if (listener != null) {
                listener.onQueueUpdate(queue);
            }
        }

        @Override
        public void onPlaybackUpdate(final int state, final int repeatMode, final boolean shuffled, final PlaybackParameters parameters) {
            if (listener != null) {
                listener.onPlaybackUpdate(state, repeatMode, shuffled, parameters);
            }
        }

        @Override
        public void onProgressUpdate(final int currentProgress, final int duration, final int bufferPercent) {
            if (listener != null) {
                listener.onProgressUpdate(currentProgress, duration, bufferPercent);
            }
        }

        @Override
        public void onMetadataUpdate(final StreamInfo info, final PlayQueue queue) {
            if (listener != null) {
                listener.onMetadataUpdate(info, queue);
            }
        }

        @Override
        public void onServiceStopped() {
            if (listener != null) {
                listener.onServiceStopped();
            }
            unbind(App.getAppContext());
        }
    };
}
