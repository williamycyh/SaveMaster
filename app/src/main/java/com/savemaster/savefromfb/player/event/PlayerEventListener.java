package com.savemaster.savefromfb.player.event;

import com.google.android.exoplayer2.PlaybackParameters;

import savemaster.save.master.pipd.stream.StreamInfo;

import com.savemaster.savefromfb.player.playqueue.PlayQueue;

public interface PlayerEventListener {

    void onQueueUpdate(PlayQueue queue);

    void onPlaybackUpdate(int state, int repeatMode, boolean shuffled, PlaybackParameters parameters);

    void onProgressUpdate(int currentProgress, int duration, int bufferPercent);

    void onMetadataUpdate(StreamInfo info, PlayQueue queue);

    void onServiceStopped();
}
