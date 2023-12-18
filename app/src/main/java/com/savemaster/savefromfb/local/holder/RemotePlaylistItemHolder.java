package com.savemaster.savefromfb.local.holder;

import android.text.TextUtils;
import android.view.ViewGroup;

import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.db.LocalItem;
import com.savemaster.savefromfb.db.playlist.model.PlaylistRemoteEntity;
import com.savemaster.savefromfb.util.GlideUtils;
import com.savemaster.savefromfb.util.Localization;

import savemaster.save.master.pipd.NewPipe;

import java.text.DateFormat;

import com.savemaster.savefromfb.local.LocalItemBuilder;

public class RemotePlaylistItemHolder extends PlaylistItemHolder {
	
	public RemotePlaylistItemHolder(LocalItemBuilder infoItemBuilder, ViewGroup parent) {
		
		super(infoItemBuilder, parent);
	}
	
	@Override
	public void updateFromItem(final LocalItem localItem, final DateFormat dateFormat) {
		
		super.updateFromItem(localItem, dateFormat);
		
		if (!(localItem instanceof PlaylistRemoteEntity)) return;
		final PlaylistRemoteEntity item = (PlaylistRemoteEntity) localItem;
		
		itemTitleView.setText(item.getName());
		itemStreamCountView.setText(Localization.localizeStreamCount(itemStreamCountView.getContext(), item.getStreamCount()));
		if (!TextUtils.isEmpty(item.getUploader())) {
			itemUploaderView.setText(item.getUploader());
		}
		else {
			itemUploaderView.setText(NewPipe.getNameOfService(item.getServiceId()));
		}
		
		GlideUtils.loadThumbnail(App.getAppContext(), itemThumbnailView, item.getThumbnailUrl());
	}
}
