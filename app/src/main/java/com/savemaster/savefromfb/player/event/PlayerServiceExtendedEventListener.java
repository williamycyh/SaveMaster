package com.savemaster.savefromfb.player.event;

import com.savemaster.savefromfb.player.UIMainPlayer;
import com.savemaster.savefromfb.player.VideoPlayerImpl;

public interface PlayerServiceExtendedEventListener extends PlayerServiceEventListener {

    void onServiceConnected(VideoPlayerImpl player, UIMainPlayer playerService, boolean playAfterConnect);

    void onServiceDisconnected();
}
