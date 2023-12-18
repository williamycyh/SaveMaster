package com.savemaster.savefromfb.player;

import android.content.Intent;
import android.view.MenuItem;

import com.savemaster.savefromfb.R;

public final class UIPopupPlayerActivity extends ServicePlayerActivity {

    @Override
    public String getTag() {
        return "PopupVideoPlayerActivity";
    }

    @Override
    public String getSupportActionTitle() {
        return getResources().getString(R.string.title_activity_popup_player);
    }

    @Override
    public Intent getBindIntent() {
        return new Intent(this, UIMainPlayer.class);
    }

    @Override
    public void startPlayerListener() {
        if (player instanceof VideoPlayerImpl) {
            ((VideoPlayerImpl) player).setActivityListener(this);
        }
    }

    @Override
    public void stopPlayerListener() {
        if (player instanceof VideoPlayerImpl) {
            ((VideoPlayerImpl) player).removeActivityListener(this);
        }
    }

    @Override
    public boolean onPlayerOptionSelected(MenuItem item) {
        return false;
    }
}
