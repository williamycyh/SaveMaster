package com.savemaster.savefromfb.local.holder;

import android.view.View;
import android.view.ViewGroup;

import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.db.LocalItem;
import com.savemaster.savefromfb.db.playlist.PlaylistMetadataEntry;
import com.savemaster.savefromfb.util.GlideUtils;
import com.savemaster.savefromfb.util.Localization;

import java.text.DateFormat;

import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.local.LocalItemBuilder;

public class LocalPlaylistItemHolder extends PlaylistItemHolder {
	
	public LocalPlaylistItemHolder(LocalItemBuilder infoItemBuilder, ViewGroup parent) {
		
		super(infoItemBuilder, parent);
	}
	
	@Override
	public void updateFromItem(final LocalItem localItem, final DateFormat dateFormat) {
		
		super.updateFromItem(localItem, dateFormat);
		
		if (!(localItem instanceof PlaylistMetadataEntry)) return;
		final PlaylistMetadataEntry item = (PlaylistMetadataEntry) localItem;
		
		itemTitleView.setText(item.name);
		itemStreamCountView.setText(Localization.localizeStreamCount(itemStreamCountView.getContext(), item.streamCount));
		itemUploaderView.setText(R.string.me);
		
		GlideUtils.loadThumbnail(App.getAppContext(), itemThumbnailView, item.thumbnailUrl);
		itemMoreActions.setVisibility(itemBuilder.isShowOptionMenu() ? View.VISIBLE : View.GONE);
	}
}
