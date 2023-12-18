package com.savemaster.savefromfb.player.playback;

import com.google.android.exoplayer2.source.MediaSource;

import savemaster.save.master.pipd.stream.StreamInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.savemaster.savefromfb.player.playqueue.PlayQueueItem;

public interface PlaybackListener {

    /**
     * Called to check if the currently playing stream is approaching the end of its playback.
     * Implementation should return true when the current playback position is progressing within
     * timeToEndMillis or less to its playback during.
     * <p>
     * May be called at any time.
     */
    boolean isApproachingPlaybackEdge(final long timeToEndMillis);

    /**
     * Called when the stream at the current queue index is not ready yet.
     * Signals to the listener to block the player from playing anything and notify the source
     * is now invalid.
     * <p>
     * May be called at any time.
     */
    void onPlaybackBlock();

    /**
     * Called when the stream at the current queue index is ready.
     * Signals to the listener to resume the player by preparing a new source.
     * <p>
     * May be called only when the player is blocked.
     */
    void onPlaybackUnblock(final MediaSource mediaSource);

    /**
     * Called when the queue index is refreshed.
     * Signals to the listener to synchronize the player's window to the manager's
     * window.
     * <p>
     * May be called anytime at any amount once unblock is called.
     */
    void onPlaybackSynchronize(@NonNull final PlayQueueItem item);

    /**
     * Requests the listener to resolve a stream info into a media source
     * according to the listener's implementation (background, popup or main video player).
     * <p>
     * May be called at any time.
     */
    @Nullable
	MediaSource sourceOf(final PlayQueueItem item, final StreamInfo info);

    /**
     * Called when the play queue can no longer to played or used.
     * Currently, this means the play queue is empty and complete.
     * Signals to the listener that it should shutdown.
     * <p>
     * May be called at any time.
     */
    void onPlaybackShutdown();

    /**
     * Called whenever the play queue was edited (items were added, deleted or moved),
     * use this to e.g. update notification buttons or fragment ui.
     * <p>
     * May be called at any time.
     * </p>
     */
    void onPlayQueueEdited();
}
