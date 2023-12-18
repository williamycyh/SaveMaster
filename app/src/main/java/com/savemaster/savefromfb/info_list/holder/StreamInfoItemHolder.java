package com.savemaster.savefromfb.info_list.holder;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.savemaster.savefromfb.info_list.InfoItemBuilder;

import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.stream.StreamInfoItem;

import com.savemaster.savefromfb.R;

import com.savemaster.moton.Utils;
import com.savemaster.savefromfb.util.Localization;

import java.util.Calendar;
import java.util.Date;

public class StreamInfoItemHolder extends StreamMiniInfoItemHolder {
	
	public final TextView itemAdditionalDetails;
	public final ImageView btn_download_action;
	
	public StreamInfoItemHolder(InfoItemBuilder infoItemBuilder, ViewGroup parent) {
		super(infoItemBuilder, R.layout.savemasterdown_list_stream_item_medium, parent);
		itemAdditionalDetails = itemView.findViewById(R.id.itemAdditionalDetails);
		btn_download_action = itemView.findViewById(R.id.btn_download_action);
	}
	
	@Override
	public void updateFromItem(final InfoItem infoItem) {
		super.updateFromItem(infoItem);
		
		if (!(infoItem instanceof StreamInfoItem)) return;
		final StreamInfoItem item = (StreamInfoItem) infoItem;
		
		itemAdditionalDetails.setText(getStreamInfoDetail(item));
		btn_download_action.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startDownload(infoItem);
			}
		});

	}

	ProgressDialog progressDialog;
	@SuppressLint("CheckResult")
	public void startDownload(final InfoItem infoItem){

		FragmentActivity activity = (FragmentActivity) itemBuilder.getContext();
		Utils.download(activity, infoItem.getUrl());
//		progressDialog = new ProgressDialog(activity);
//		progressDialog.setMessage("loading...");
//		progressDialog.setCancelable(true);
//		progressDialog.show();
//		ExtractorHelper.getStreamInfo(infoItem.getServiceId(), infoItem.getUrl(), false)
//				.subscribeOn(Schedulers.io())
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(result -> {
//					try {
//						progressDialog.dismiss();
//						DownloadDialog downloadDialog = DownloadDialog.newInstance(activity, result);
//						downloadDialog.show(activity.getSupportFragmentManager(), "DownloadDialog");
//					} catch (Exception e) {
////						Toast.makeText(activity, R.string.musicplayerdowner_setup_menu_msg, Toast.LENGTH_LONG).show();
//						e.printStackTrace();
//					}
//				}, (@NonNull final Throwable throwable) -> {
//					progressDialog.dismiss();
//				});
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
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(Date.from(infoItem.getUploadDate().offsetDateTime().toInstant()));
			return Localization.relativeTime(calendar);
		}
		else {
			return infoItem.getTextualUploadDate();
		}
	}
}
