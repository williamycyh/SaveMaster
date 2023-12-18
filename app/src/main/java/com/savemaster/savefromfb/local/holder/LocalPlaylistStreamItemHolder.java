package com.savemaster.savefromfb.local.holder;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.db.LocalItem;
import com.savemaster.savefromfb.db.playlist.PlaylistStreamEntry;
import com.savemaster.savefromfb.util.GlideUtils;
import com.savemaster.savefromfb.util.Localization;

import java.text.DateFormat;

import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.local.LocalItemBuilder;

public class LocalPlaylistStreamItemHolder extends LocalItemHolder {
	
	public final ImageView itemThumbnailView;
	public final TextView itemVideoTitleView;
	public final TextView itemAdditionalDetailsView;
	public final TextView itemDurationView;
	public final View itemHandleView;
	
	public LocalPlaylistStreamItemHolder(LocalItemBuilder infoItemBuilder, int layoutId, ViewGroup parent) {
		
		super(infoItemBuilder, layoutId, parent);
		
		itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView);
		itemVideoTitleView = itemView.findViewById(R.id.itemVideoTitleView);
		itemAdditionalDetailsView = itemView.findViewById(R.id.itemAdditionalDetails);
		itemDurationView = itemView.findViewById(R.id.itemDurationView);
		itemHandleView = itemView.findViewById(R.id.itemHandle);
	}
	
	public LocalPlaylistStreamItemHolder(LocalItemBuilder infoItemBuilder, ViewGroup parent) {
		
		this(infoItemBuilder, R.layout.savemasterdown_list_str_playl_item, parent);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void updateFromItem(final LocalItem localItem, final DateFormat dateFormat) {
		
		if (!(localItem instanceof PlaylistStreamEntry)) return;
		final PlaylistStreamEntry item = (PlaylistStreamEntry) localItem;
		
		itemVideoTitleView.setText(item.title);
		itemAdditionalDetailsView.setText(Localization.concatenateStrings(item.uploader));
		
		if (item.duration > 0) {
			itemDurationView.setText(Localization.getDurationString(item.duration));
			itemDurationView.setBackgroundResource(R.drawable.savemasterdown_duration_bg);
			itemDurationView.setVisibility(View.VISIBLE);
		}
		else {
			itemDurationView.setVisibility(View.GONE);
		}
		
		// Default thumbnail is shown on error, while loading and if the url is empty
		GlideUtils.loadThumbnail(App.getAppContext(), itemThumbnailView, item.thumbnailUrl);
		
		itemView.setOnClickListener(view -> {
			
			if (itemBuilder.getOnItemSelectedListener() != null) {
				itemBuilder.getOnItemSelectedListener().selected(item);
			}
		});
		
		itemView.setLongClickable(true);
		itemView.setOnLongClickListener(view -> {
			
			if (itemBuilder.getOnItemSelectedListener() != null) {
				itemBuilder.getOnItemSelectedListener().held(item, view);
			}
			return true;
		});
		
		itemHandleView.setOnTouchListener(getOnTouchListener(item));
	}
	
	private View.OnTouchListener getOnTouchListener(final PlaylistStreamEntry item) {
		
		return (view, motionEvent) -> {
			
			view.performClick();
			
			if (itemBuilder != null && itemBuilder.getOnItemSelectedListener() != null && motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
				itemBuilder.getOnItemSelectedListener().drag(item, LocalPlaylistStreamItemHolder.this);
			}
			return false;
		};
	}
}
