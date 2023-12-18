package com.savemaster.savefromfb.info_list.holder;

import android.annotation.SuppressLint;
//import android.icu.util.Calendar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.savemaster.savefromfb.info_list.InfoItemBuilder;

import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.stream.StreamInfoItem;
import savemaster.save.master.pipd.stream.StreamType;

import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.util.ExtractorHelper;
import com.savemaster.savefromfb.util.GlideUtils;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.NavigationHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class StreamMiniInfoItemHolder extends InfoItemHolder {
	
	public final ImageView itemThumbnailView;
	public final TextView itemVideoTitleView;
	public final CircleImageView itemUploaderThumbnailView;
	public final TextView itemDurationView;
	private final ImageButton itemMoreAction;
	private final TextView itemAdditionalDetails;
	
	StreamMiniInfoItemHolder(InfoItemBuilder infoItemBuilder, int layoutId, ViewGroup parent) {
		super(infoItemBuilder, layoutId, parent);
		
		itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView);
		itemVideoTitleView = itemView.findViewById(R.id.itemVideoTitleView);
		itemUploaderThumbnailView = itemView.findViewById(R.id.itemUploaderThumbnailView);
		itemDurationView = itemView.findViewById(R.id.itemDurationView);
		itemMoreAction = itemView.findViewById(R.id.btn_action);
		itemAdditionalDetails = itemView.findViewById(R.id.itemAdditionalDetails);
	}
	
	public StreamMiniInfoItemHolder(InfoItemBuilder infoItemBuilder, ViewGroup parent) {
		this(infoItemBuilder, R.layout.savemasterdown_list_stre_mi_item, parent);
	}
	
	@SuppressLint("CheckResult")
	@Override
	public void updateFromItem(final InfoItem infoItem) {
		if (!(infoItem instanceof StreamInfoItem)) return;
		final StreamInfoItem item = (StreamInfoItem) infoItem;
		
		itemVideoTitleView.setText(item.getName());
		itemAdditionalDetails.setText(getStreamInfoDetail(item));
		
		ExtractorHelper.getChannelInfo(item.getServiceId(), item.getUploaderUrl(), true)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread()).subscribe(
				// onNext
				channelInfo -> {
					String avatarUrl = TextUtils.isEmpty(channelInfo.getAvatarUrl()) ? channelInfo.getAvatarUrl() : channelInfo.getAvatarUrl().replace("s100", "s720");
					GlideUtils.loadAvatar(App.getAppContext(), itemUploaderThumbnailView, avatarUrl);
				},
				// onError
				throwable -> {
				});
		
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
		String thumbnailUrl = item.getThumbnailUrl().contains("hqdefault") ? item.getThumbnailUrl().split("hqdefault.jpg")[0] + "hqdefault.jpg" : item.getThumbnailUrl();
		GlideUtils.loadThumbnail(App.getAppContext(), itemThumbnailView, thumbnailUrl);
		
		itemView.setOnClickListener(view -> {
			if (itemBuilder.getOnStreamSelectedListener() != null) {
				itemBuilder.getOnStreamSelectedListener().selected(item);
			}
		});

		itemUploaderThumbnailView.setOnClickListener(v -> {
			final AppCompatActivity activity = (AppCompatActivity) itemBuilder.getContext();
			try {
				NavigationHelper.openChannelFragment(activity.getSupportFragmentManager(), item.getServiceId(), item.getUploaderUrl(), item.getUploaderName());
			} catch (final Exception ignored) {
			}
		});
		
		itemMoreAction.setOnClickListener(view -> {
			if (itemBuilder.getOnStreamSelectedListener() != null) {
				itemBuilder.getOnStreamSelectedListener().more(item, itemMoreAction);
			}
		});
	}

	private String getStreamInfoDetail(final StreamInfoItem infoItem) {
		String detailInfo = infoItem.getUploaderName() + Localization.DOT_SEPARATOR;
		if (infoItem.getViewCount() >= 0) {
			detailInfo = detailInfo + Localization.shortViewCount(itemBuilder.getContext(), infoItem.getViewCount());
		}

		final String uploadDate = getFormattedRelativeUploadDate(infoItem);
		if (!TextUtils.isEmpty(uploadDate)) {
			return Localization.concatenateStrings(detailInfo, uploadDate);
		}
		return detailInfo;
	}

	private String getFormattedRelativeUploadDate(final StreamInfoItem infoItem) {
		if (infoItem.getUploadDate() != null) {
//			return Localization.relativeTime(infoItem.getUploadDate().date());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(Date.from(infoItem.getUploadDate().offsetDateTime().toInstant()));
			return Localization.relativeTime(calendar);
		}
		else {
			return infoItem.getTextualUploadDate();
		}
	}
}
