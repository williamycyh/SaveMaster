package com.savemaster.savefromfb.info_list.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.savemaster.savefromfb.info_list.InfoItemBuilder;

import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.channel.ChannelInfoItem;

import de.hdodenhof.circleimageview.CircleImageView;
import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.util.GlideUtils;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.recyclerview.SwipeRevealLayout;

public class ChannelMiniInfoItemHolder extends InfoItemHolder {
	
	public final CircleImageView itemThumbnailView;
	public final TextView itemTitleView;
	public final TextView itemAdditionalDetailView;
	public final TextView itemChannelDescriptionView;
	public final View itemRoot;
	public final SwipeRevealLayout swipeLayout;
	public final View unsubscribeLayout;
	
	ChannelMiniInfoItemHolder(InfoItemBuilder infoItemBuilder, int layoutId, ViewGroup parent) {
		
		super(infoItemBuilder, layoutId, parent);
		
		itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView);
		itemTitleView = itemView.findViewById(R.id.itemTitleView);
		itemAdditionalDetailView = itemView.findViewById(R.id.itemAdditionalDetails);
		itemChannelDescriptionView = itemView.findViewById(R.id.itemChannelDescriptionView);
		itemRoot = itemView.findViewById(R.id.itemRoot);
		swipeLayout = itemView.findViewById(R.id.swipe_layout);
		unsubscribeLayout = itemView.findViewById(R.id.unsubscribe_layout);
	}
	
	public ChannelMiniInfoItemHolder(InfoItemBuilder infoItemBuilder, ViewGroup parent) {
		
		this(infoItemBuilder, R.layout.savemasterdown_list_channel_mini_item, parent);
	}
	
	@Override
	public void updateFromItem(final InfoItem infoItem) {
		
		if (!(infoItem instanceof ChannelInfoItem)) return;
		final ChannelInfoItem item = (ChannelInfoItem) infoItem;
		
		itemTitleView.setText(item.getName());
		itemAdditionalDetailView.setText(getDetailLine(item));
		itemChannelDescriptionView.setText(item.getDescription());
		GlideUtils.loadThumbnail(App.getAppContext(), itemThumbnailView, item.getThumbnailUrl());
		
		itemRoot.setOnClickListener(view -> {
			if (itemBuilder.getOnChannelSelectedListener() != null) {
				itemBuilder.getOnChannelSelectedListener().selected(item);
			}
		});
		
		unsubscribeLayout.setOnClickListener(view -> {
			if (itemBuilder.getOnChannelSelectedListener() != null) {
				itemBuilder.getOnChannelSelectedListener().swipe(item);
			}
		});
	}
	
	protected String getDetailLine(final ChannelInfoItem item) {
		String details = "";
		if (item.getSubscriberCount() >= 0) {
			details += Localization.shortSubscriberCount(itemBuilder.getContext(), item.getSubscriberCount());
		}
		return details;
	}
}
