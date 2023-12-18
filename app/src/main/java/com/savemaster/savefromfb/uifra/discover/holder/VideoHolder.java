package com.savemaster.savefromfb.uifra.discover.holder;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.stream.StreamInfoItem;
import savemaster.save.master.pipd.stream.StreamType;

import com.savemaster.moton.Utils;
import com.savemaster.savefromfb.uifra.discover.adapter.VideoListAdapter;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.models.response.explore.ItemsItem;
import com.savemaster.savefromfb.util.Constants;
import com.savemaster.savefromfb.util.GlideUtils;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.recyclerview.AbstractViewHolder;

public class VideoHolder extends AbstractViewHolder {

    @BindView(R.id.itemThumbnailView)
    ImageView thumbnails;
    @BindView(R.id.itemDurationView)
    TextView duration;
    @BindView(R.id.itemVideoTitleView)
    TextView title;
    @BindView(R.id.itemUploaderThumbnailView)
    CircleImageView itemUploaderThumbnailView;
    @BindView(R.id.itemAdditionalDetails)
    TextView additionalInfo;
    @BindView(R.id.btn_action)
    ImageButton btnMoreOptions;

    @BindView(R.id.btn_download_action)
    ImageView btn_download_action;

    public VideoHolder(ViewGroup parent, VideoListAdapter.Listener listener) {

        super(parent, R.layout.savemasterdown_list_stream_item_medium);

        itemView.setOnClickListener(view -> listener.onVideoClicked(getBindingAdapterPosition()));
        btnMoreOptions.setOnClickListener(view -> listener.onMoreOption(getBindingAdapterPosition(), view));
    }

    ProgressDialog progressDialog;
    @SuppressLint("CheckResult")
    public void startDownload(final InfoItem infoItem){

        FragmentActivity activity = (FragmentActivity) btnMoreOptions.getContext();
        Utils.download(activity, infoItem.getUrl());
//        progressDialog = new ProgressDialog(activity);
//        progressDialog.setMessage("loading...");
//        progressDialog.setCancelable(true);
//        progressDialog.show();
//        ExtractorHelper.getStreamInfo(infoItem.getServiceId(), infoItem.getUrl(), false)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(result -> {
//                    try {
//                        progressDialog.dismiss();
//                        DownloadDialog downloadDialog = DownloadDialog.newInstance(activity, result);
//                        downloadDialog.show(activity.getSupportFragmentManager(), "DownloadDialog");
//                    } catch (Exception e) {
////						Toast.makeText(activity, R.string.musicplayerdowner_setup_menu_msg, Toast.LENGTH_LONG).show();
//                        e.printStackTrace();
//                    }
//                }, (@NonNull final Throwable throwable) -> {
//                    progressDialog.dismiss();
//                });
    }

    @SuppressLint("CheckResult")
    public void set(ItemsItem item) {

        title.setText(item.getVideoRenderer().getTitle().getRuns().get(0).getText());

        duration.setText(item.getVideoRenderer().getLengthText().getSimpleText());

        additionalInfo.setText(getAdditionalInfo(item));

        GlideUtils.loadAvatar(App.getAppContext(), itemUploaderThumbnailView, item.getVideoRenderer().getChannelThumbnailSupportedRenderers().getChannelThumbnailWithLinkRenderer().getThumbnail().getThumbnails().get(0).getUrl());

        // default thumbnail is shown on error, while loading and if the url is empty
        String thumbnailUrl = item.getVideoRenderer().getThumbnail().getThumbnails().get(0).getUrl().contains("hqdefault") ? item.getVideoRenderer().getThumbnail().getThumbnails().get(0).getUrl().split("hqdefault.jpg")[0] + "hqdefault.jpg" : item.getVideoRenderer().getThumbnail().getThumbnails().get(0).getUrl();
        GlideUtils.loadThumbnail(App.getAppContext(), thumbnails, thumbnailUrl);

        itemUploaderThumbnailView.setOnClickListener(v -> {
            final AppCompatActivity activity = (AppCompatActivity) itemView.getContext();
            try {
                String uploaderUrl = Constants.YOUTUBE_URL + item.getVideoRenderer().getOwnerText().getRuns().get(0).getNavigationEndpoint().getCommandMetadata().getWebCommandMetadata().getUrl();
                String uploaderName = item.getVideoRenderer().getOwnerText().getRuns().get(0).getText();
                NavigationHelper.openChannelFragment(activity.getSupportFragmentManager(), Constants.YOUTUBE_SERVICE_ID, uploaderUrl, uploaderName);
            } catch (final Exception ignored) {
            }
        });

        btn_download_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StreamInfoItem infoItem = new StreamInfoItem(0, Constants.VIDEO_BASE_URL + item.getVideoRenderer().getVideoId(), item.getVideoRenderer().getOwnerText().getRuns().get(0).getText(), StreamType.VIDEO_STREAM);
                startDownload(infoItem);
            }
        });
    }

    private String getAdditionalInfo(ItemsItem item) {
        String detailInfo = item.getVideoRenderer().getOwnerText().getRuns().get(0).getText() + Localization.DOT_SEPARATOR;
        detailInfo = detailInfo + item.getVideoRenderer().getShortViewCountText().getSimpleText();
        detailInfo += " • " + item.getVideoRenderer().getPublishedTimeText().getSimpleText();
        return detailInfo;
    }
}
