package com.savemaster.savefromfb.player.playqueue;

import com.savemaster.savefromfb.util.ExtractorHelper;

import savemaster.save.master.pipd.Page;
import savemaster.save.master.pipd.channel.ChannelInfo;
import savemaster.save.master.pipd.channel.ChannelInfoItem;
import savemaster.save.master.pipd.stream.StreamInfoItem;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public final class ChannelPlayQueue extends AbstractInfoPlayQueue<ChannelInfo, ChannelInfoItem> {
	
	public ChannelPlayQueue(final ChannelInfoItem item) {
		super(item);
	}
	
	public ChannelPlayQueue(final ChannelInfo info) {
		this(info.getServiceId(), info.getUrl(), info.getNextPage(), info.getRelatedItems(), 0);
	}
	
	public ChannelPlayQueue(final int serviceId, final String url, final Page nextPage, final List<StreamInfoItem> streams, final int index) {
		super(serviceId, url, nextPage, streams, index);
	}
	
	@Override
	protected String getTag() {
		return "ChannelPlayQueue@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void fetch() {
		
		if (this.isInitial) {
			ExtractorHelper.getChannelInfo(this.serviceId, this.baseUrl, false)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(getHeadListObserver());
		}
		else {
			ExtractorHelper.getMoreChannelItems(this.serviceId, this.baseUrl, this.nextPage)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(getNextPageObserver());
		}
	}
}
