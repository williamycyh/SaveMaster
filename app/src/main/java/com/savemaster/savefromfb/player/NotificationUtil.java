package com.savemaster.savefromfb.player;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;

import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.savemaster.savefromfb.uiact.MainActivity;
import com.savemaster.savefromfb.util.NavigationHelper;

import com.savemaster.savefromfb.R;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ALL;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ONE;

/**
 * This is a utility class for player notifications.
 */
public final class NotificationUtil {
    private static final int NOTIFICATION_ID = 1000;

    @Nullable private static NotificationUtil instance = null;

    @NotificationConstants.Action
    private final int[] notificationSlots = NotificationConstants.SLOT_DEFAULTS.clone();

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    private NotificationUtil() {
    }

    public static NotificationUtil getInstance() {
        if (instance == null) {
            instance = new NotificationUtil();
        }
        return instance;
    }

    /**
     * Creates the notification if it does not exist already and recreates it if forceRecreate is
     * true. Updates the notification with the data in the player.
     *
     * @param player        the player currently open, to take data from
     * @param forceRecreate whether to force the recreation of the notification even if it already
     *                      exists
     */
    synchronized void createNotificationIfNeededAndUpdate(final VideoPlayerImpl player, final boolean forceRecreate) {
        if (forceRecreate || notificationBuilder == null) {
            notificationBuilder = createNotification(player);
        }
        updateNotification(player);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private synchronized NotificationCompat.Builder createNotification(final VideoPlayerImpl player) {
        notificationManager = NotificationManagerCompat.from(player.context);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(player.context, player.context.getString(R.string.savemasterdown_notification_channel_id));

        initializeNotificationSlots(player);

        // count the number of real slots, to make sure compact slots indices are not out of bound
        int nonNothingSlotCount = 5;
        if (notificationSlots[3] == NotificationConstants.NOTHING) {
            --nonNothingSlotCount;
        }
        if (notificationSlots[4] == NotificationConstants.NOTHING) {
            --nonNothingSlotCount;
        }

        // build the compact slot indices array (need code to convert from Integer... because Java)
        final List<Integer> compactSlotList = NotificationConstants.getCompactSlotsFromPreferences(player.context, player.sharedPreferences, nonNothingSlotCount);
        final int[] compactSlots = new int[compactSlotList.size()];
        for (int i = 0; i < compactSlotList.size(); i++) {
            compactSlots[i] = compactSlotList.get(i);
        }

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(player.mediaSessionManager.getSessionToken())
                .setShowActionsInCompactView(compactSlots))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.savemasterdown_ic_headset_white_24dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(ContextCompat.getColor(player.context, R.color.savemasterdown_gray))
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setDeleteIntent(PendingIntent.getBroadcast(player.context, NOTIFICATION_ID, new Intent(UIMainPlayer.ACTION_CLOSE), FLAG_IMMUTABLE));

        return builder;
    }

    /**
     * Updates the notification builder and the button icons depending on the playback state.
     *
     * @param player the player currently open, to take data from
     */
    private synchronized void updateNotification(final VideoPlayerImpl player) {
        // also update content intent, in case the user switched players
        notificationBuilder.setContentIntent(PendingIntent.getActivity(player.context, NOTIFICATION_ID, getIntentForNotification(player), FLAG_IMMUTABLE));
        notificationBuilder.setContentTitle(player.getVideoTitle());
        notificationBuilder.setContentText(player.getUploaderName());
        notificationBuilder.setTicker(player.getVideoTitle());
        updateActions(notificationBuilder, player);
        setLargeIcon(notificationBuilder, player);
    }


    @SuppressLint("RestrictedApi")
    boolean shouldUpdateBufferingSlot() {
        if (notificationBuilder == null) {
            // if there is no notification active, there is no point in updating it
            return false;
        } else if (notificationBuilder.mActions.size() < 3) {
            // this should never happen, but let's make sure notification actions are populated
            return true;
        }

        // only second and third slot could contain PLAY_PAUSE_BUFFERING, update them only if they
        // are not already in the buffering state (the only one with a null action intent)
        return (notificationSlots[1] == NotificationConstants.PLAY_PAUSE_BUFFERING
                && notificationBuilder.mActions.get(1).actionIntent != null)
                || (notificationSlots[2] == NotificationConstants.PLAY_PAUSE_BUFFERING
                && notificationBuilder.mActions.get(2).actionIntent != null);
    }


    void createNotificationAndStartForeground(final VideoPlayerImpl player, final Service service) {
        if (notificationBuilder == null) {
            notificationBuilder = createNotification(player);
        }
        updateNotification(player);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            service.startForeground(NOTIFICATION_ID, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            service.startForeground(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    void cancelNotificationAndStopForeground(final Service service) {
        service.stopForeground(true);

        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        notificationManager = null;
        notificationBuilder = null;
    }


    private void initializeNotificationSlots(final VideoPlayerImpl player) {
        for (int i = 0; i < 5; ++i) {
            notificationSlots[i] = player.sharedPreferences.getInt(player.context.getString(NotificationConstants.SLOT_PREF_KEYS[i]), NotificationConstants.SLOT_DEFAULTS[i]);
        }
    }

    @SuppressLint("RestrictedApi")
    private void updateActions(final NotificationCompat.Builder builder, final VideoPlayerImpl player) {
        builder.mActions.clear();
        for (int i = 0; i < 5; ++i) {
            addAction(builder, player, notificationSlots[i]);
        }
    }

    private void addAction(final NotificationCompat.Builder builder, final VideoPlayerImpl player, @NotificationConstants.Action final int slot) {
        final NotificationCompat.Action action = getAction(player, slot);
        if (action != null) {
            builder.addAction(action);
        }
    }

    @Nullable
    private NotificationCompat.Action getAction(final VideoPlayerImpl player, @NotificationConstants.Action final int selectedAction) {
        final int baseActionIcon = NotificationConstants.ACTION_ICONS[selectedAction];
        switch (selectedAction) {
            case NotificationConstants.PREVIOUS:
                return getAction(player, baseActionIcon, R.string.exo_controls_previous_description, UIMainPlayer.ACTION_PLAY_PREVIOUS);

            case NotificationConstants.NEXT:
                return getAction(player, baseActionIcon, R.string.exo_controls_next_description, UIMainPlayer.ACTION_PLAY_NEXT);

            case NotificationConstants.REWIND:
                return getAction(player, baseActionIcon, R.string.exo_controls_rewind_description, UIMainPlayer.ACTION_FAST_REWIND);

            case NotificationConstants.FORWARD:
                return getAction(player, baseActionIcon, R.string.exo_controls_fastforward_description, UIMainPlayer.ACTION_FAST_FORWARD);

            case NotificationConstants.SMART_REWIND_PREVIOUS:
                if (player.playQueue != null && player.playQueue.size() > 1) {
                    return getAction(player, R.drawable.ic_control_savemasterdown_previous_white_24dp, R.string.exo_controls_previous_description, UIMainPlayer.ACTION_PLAY_PREVIOUS);
                } else {
                    return getAction(player, R.drawable.savemasterdown_ic_fast_rewind, R.string.exo_controls_rewind_description, UIMainPlayer.ACTION_FAST_REWIND);
                }

            case NotificationConstants.SMART_FORWARD_NEXT:
                if (player.playQueue != null && player.playQueue.size() > 1) {
                    return getAction(player, R.drawable.ic_control_savemasterdown_next_white_24dp, R.string.exo_controls_next_description, UIMainPlayer.ACTION_PLAY_NEXT);
                } else {
                    return getAction(player, R.drawable.savemasterdown_ic_fast_forward, R.string.exo_controls_fastforward_description, UIMainPlayer.ACTION_FAST_FORWARD);
                }

            case NotificationConstants.PLAY_PAUSE_BUFFERING:
                if (player.getCurrentState() == BasePlayer.STATE_PREFLIGHT || player.getCurrentState() == BasePlayer.STATE_BLOCKED || player.getCurrentState() == BasePlayer.STATE_BUFFERING) {
                    // null intent -> show hourglass icon that does nothing when clicked
                    return new NotificationCompat.Action(R.drawable.savemasterdown_ic_hourglass_top_white_24dp, player.context.getString(R.string.savemasterdown_notification_action_buffering), null);
                }

            case NotificationConstants.PLAY_PAUSE:
                if (player.getCurrentState() == BasePlayer.STATE_COMPLETED) {
                    return getAction(player, R.drawable.savemasterdown_ic_replay_white_24dp, R.string.exo_controls_pause_description, UIMainPlayer.ACTION_PLAY_PAUSE);
                } else if (player.isPlaying()
                        || player.getCurrentState() == BasePlayer.STATE_PREFLIGHT
                        || player.getCurrentState() == BasePlayer.STATE_BLOCKED
                        || player.getCurrentState() == BasePlayer.STATE_BUFFERING) {
                    return getAction(player, R.drawable.savemasterdown_ic_pause_white_24dp, R.string.exo_controls_pause_description, UIMainPlayer.ACTION_PLAY_PAUSE);
                } else {
                    return getAction(player, R.drawable.savemasterdown_ic_play_arrow_white_24dp, R.string.exo_controls_play_description, UIMainPlayer.ACTION_PLAY_PAUSE);
                }

            case NotificationConstants.REPEAT:
                if (player.getRepeatMode() == REPEAT_MODE_ALL) {
                    return getAction(player, R.drawable.savemasterdown_controls_repeat_all, R.string.exo_controls_repeat_all_description, UIMainPlayer.ACTION_REPEAT);
                } else if (player.getRepeatMode() == REPEAT_MODE_ONE) {
                    return getAction(player, R.drawable.savemasterdown_controls_repeat_one, R.string.exo_controls_repeat_one_description, UIMainPlayer.ACTION_REPEAT);
                } else /* player.getRepeatMode() == REPEAT_MODE_OFF */ {
                    return getAction(player, R.drawable.savemasterdown_controls_repeat_off, R.string.exo_controls_repeat_off_description, UIMainPlayer.ACTION_REPEAT);
                }

            case NotificationConstants.SHUFFLE:
                if (player.playQueue != null && player.playQueue.isShuffled()) {
                    return getAction(player, R.drawable.savemasterdown_ic_shuffle_white_24dp, R.string.exo_controls_shuffle_on_description, UIMainPlayer.ACTION_SHUFFLE);
                } else {
                    return getAction(player, R.drawable.savemasterdown_ic_shuffle_dark_24dp, R.string.exo_controls_shuffle_off_description, UIMainPlayer.ACTION_SHUFFLE);
                }

            case NotificationConstants.CLOSE:
                return getAction(player, R.drawable.ic_closesavemasterdown__white_24dp, R.string.close, UIMainPlayer.ACTION_CLOSE);

            case NotificationConstants.NOTHING:
            default:
                // do nothing
                return null;
        }
    }

    private NotificationCompat.Action getAction(final VideoPlayerImpl player, @DrawableRes final int drawable, @StringRes final int title, final String intentAction) {
        return new NotificationCompat.Action(drawable, player.context.getString(title), PendingIntent.getBroadcast(player.context, NOTIFICATION_ID, new Intent(intentAction), FLAG_IMMUTABLE));
    }

    private Intent getIntentForNotification(final VideoPlayerImpl player) {
        if (player.popupPlayerSelected()) {
            // Means we play in popup only. Let's show the play queue
            return NavigationHelper.getPopupPlayerActivityIntent(player.context);
        } else {
            // We are playing in fragment. Don't open another activity just show fragment. That's it
            final Intent intent = NavigationHelper.getPlayerIntent(player.context, MainActivity.class, null, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            return intent;
        }
    }

    private void setLargeIcon(final NotificationCompat.Builder builder, final VideoPlayerImpl player) {
        builder.setLargeIcon(player.getThumbnail());
    }
}
