package com.savemaster.savefromfb.player.relatedvideo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.stream.StreamInfoItem;
import savemaster.save.master.pipd.stream.StreamType;

import butterknife.BindView;
import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.R;
import com.savemaster.savefromfb.info_list.InfoItemBuilder;
import com.savemaster.savefromfb.util.GlideUtils;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.recyclerview.AbstractViewHolder;

public class RelatedVideoViewHolder extends AbstractViewHolder {
	
	@BindView(R.id.itemThumbnailView) ImageView itemThumbnailView;
	@BindView(R.id.itemVideoTitleView) TextView itemVideoTitleView;
	@BindView(R.id.itemUploaderView) TextView itemUploaderView;
	@BindView(R.id.itemDurationView) TextView itemDurationView;
	private InfoItemBuilder infoItemBuilder;
	
	public RelatedVideoViewHolder(ViewGroup parent, RelatedVideoAdapter.Listener listener) {
		super(parent, R.layout.savemasterdown_list_stream_im_horizl);
		infoItemBuilder = new InfoItemBuilder(parent.getContext());
		itemView.setOnClickListener(view -> listener.onVideoClicked(getBindingAdapterPosition()));
	}
	
	public void set(final InfoItem infoItem) {
		if (!(infoItem instanceof StreamInfoItem)) return;
		final StreamInfoItem item = (StreamInfoItem) infoItem;
		
		itemVideoTitleView.setText(item.getName());
		itemUploaderView.setText(item.getUploaderName());
		
		if (item.getDuration() > 0) {
			itemDurationView.setText(Localization.getDurationString(item.getDuration()));
			itemDurationView.setBackgroundResource(R.drawable.savemasterdown_duration_bg);
			itemDurationView.setVisibility(View.VISIBLE);
		}
		else if (item.getStreamType() == StreamType.LIVE_STREAM) {
			itemDurationView.setText(R.string.savemasterdown_duration_live);
			itemDurationView.setBackgroundResource(R.drawable.savemasterdown_duration_bg_live);
			itemDurationView.setVisibility(View.VISIBLE);
		}
		else {
			itemDurationView.setVisibility(View.GONE);
		}
		
		// Default thumbnail is shown on error, while loading and if the url is empty
		GlideUtils.loadThumbnail(App.getAppContext(), itemThumbnailView, item.getThumbnailUrl().split("hqdefault.jpg")[0] + "hqdefault.jpg");
	}
}
