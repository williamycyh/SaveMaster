package com.savemaster.savefromfb.uifra.list.channel;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdLoader;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.VideoOptions;
//import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.savemaster.savefromfb.db.subscription.SubscriptionEntity;
import com.savemaster.savefromfb.uifra.BackPressable;
import com.savemaster.moton.MyCommon;
import com.savemaster.savefromfb.util.UserAction;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.rxbinding2.view.RxView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;
import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.ListExtractor;
import savemaster.save.master.pipd.NewPipe;
import savemaster.save.master.pipd.channel.ChannelInfo;
import savemaster.save.master.pipd.exceptions.ExtractionException;
import savemaster.save.master.pipd.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.ButterKnife;
import com.savemaster.savefromfb.App;
import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.ads.AppInterstitialAd;
import com.savemaster.savefromfb.uifra.list.BaseListInfoFragment;
import com.savemaster.savefromfb.local.subscription.SubscriptionService;
import com.savemaster.savefromfb.player.playqueue.ChannelPlayQueue;
import com.savemaster.savefromfb.player.playqueue.PlayQueue;
import com.savemaster.savefromfb.util.AnimationUtils;
import com.savemaster.savefromfb.util.ExtractorHelper;
import com.savemaster.savefromfb.util.GlideUtils;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.SharedUtils;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ChannelFragment extends BaseListInfoFragment<ChannelInfo> implements BackPressable {
	
	private CompositeDisposable disposables = new CompositeDisposable();
	private Disposable subscribeButtonMonitor;
	private SubscriptionService subscriptionService;
	
	// Views
	private View headerRootLayout;
	private ImageView headerChannelBanner;
	private ImageView headerAvatarView;
	private TextView headerSubscribersTextView;
	private MaterialButton headerSubscribeButton;
	private View playlistCtrl;
	
	private MaterialButton headerPlayAllButton;
	private MaterialButton headerPopupButton;
	
	private String toolbarTitle;
	
	// NativeAd
	private FrameLayout nativeAdView;
	
	public static ChannelFragment getInstance(int serviceId, String url, String name) {
		
		ChannelFragment instance = new ChannelFragment();
		instance.setInitialData(serviceId, url, name);
		return instance;
	}
	
	@Override
	public void onAttach(@NotNull Context context) {
		
		super.onAttach(context);
		subscriptionService = SubscriptionService.getInstance(activity);
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.savemasterdown_fragment_channel, container, false);
		ButterKnife.bind(this, view);
		
		return view;
	}
	
	@Override
	protected void initViews(View rootView, Bundle savedInstanceState) {
		
		super.initViews(rootView, savedInstanceState);
		
		headerRootLayout = activity.getLayoutInflater().inflate(R.layout.savemasterdown_channel_header, itemsList, false);
		headerChannelBanner = rootView.findViewById(R.id.channel_banner_image);
		headerAvatarView = rootView.findViewById(R.id.channel_avatar_view);
		TextView headerTitleView = rootView.findViewById(R.id.channel_title_view);
		headerSubscribersTextView = rootView.findViewById(R.id.channel_subscriber_view);
		headerSubscribeButton = rootView.findViewById(R.id.channel_subscribe_button);
		playlistCtrl = rootView.findViewById(R.id.playlist_control);
		
		headerPlayAllButton = rootView.findViewById(R.id.playlist_ctrl_play_all_button);
		headerPopupButton = rootView.findViewById(R.id.playlist_ctrl_play_popup_button);
		
		Toolbar mToolbar = rootView.findViewById(R.id.default_toolbar);
		activity.getDelegate().setSupportActionBar(mToolbar);
		mToolbar.setTitle(TextUtils.isEmpty(toolbarTitle) ? "" : toolbarTitle);
		headerTitleView.setText(TextUtils.isEmpty(toolbarTitle) ? "" : toolbarTitle);
		
		View headerRootLayout = activity.getLayoutInflater().inflate(R.layout.savemasterdown_native_ad_list_header, itemsList, false);
		nativeAdView = headerRootLayout.findViewById(R.id.template_view);
		infoListAdapter.setHeader(headerRootLayout);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		AppInterstitialAd.getInstance().init(activity);
		// show ad
		showNativeAd();
	}
	
	@Override
	public void onDestroy() {
		
		// destroy ad
//		if (nativeAdView != null) {
//			nativeAdView.destroyNativeAd();
//		}
		
		super.onDestroy();
		if (disposables != null) disposables.clear();
		if (subscribeButtonMonitor != null) subscribeButtonMonitor.dispose();
	}
	
	// Menu
	@Override
	public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
		
		super.onCreateOptionsMenu(menu, inflater);
		
		ActionBar actionBar = activity.getDelegate().getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
		}
		inflater.inflate(R.menu.savemasterdown_menu_channel, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == R.id.menu_item_share) {
			SharedUtils.shareUrl(activity);
		}
		else if (item.getItemId() == android.R.id.home) {
			if (getFragmentManager() != null) {
				getFragmentManager().popBackStack();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void monitorSubscription(final ChannelInfo info) {
		
		final Consumer<Throwable> onError = throwable -> {
			
			AnimationUtils.animateView(headerSubscribeButton, false, 100);
			showSnackBarError(throwable, UserAction.SUBSCRIPTION, NewPipe.getNameOfService(currentInfo.getServiceId()), "Get subscription status", 0);
		};
		
		final Observable<List<SubscriptionEntity>> observable = subscriptionService.subscriptionTable()
				.getSubscription(info.getServiceId(), info.getUrl())
				.toObservable();
		
		disposables.add(observable.observeOn(AndroidSchedulers.mainThread()).subscribe(getSubscribeUpdateMonitor(info), onError));
		
		disposables.add(observable
								// Some updates are very rapid (when calling the updateSubscription(info), for example)
								// so only update the UI for the latest emission ("sync" the subscribe button's state)
								.debounce(100, TimeUnit.MILLISECONDS)
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(subscriptionEntities -> updateSubscribeButton(!subscriptionEntities.isEmpty()), onError));
		
	}
	
	private Function<Object, Object> mapOnSubscribe(final SubscriptionEntity subscription) {
		
		return object -> {
			subscriptionService.subscriptionTable().insert(subscription);
			return object;
		};
	}
	
	private Function<Object, Object> mapOnUnsubscribe(final SubscriptionEntity subscription) {
		
		return object -> {
			subscriptionService.subscriptionTable().delete(subscription);
			return object;
		};
	}
	
	private void updateSubscription(final ChannelInfo info) {
		
		final Action onComplete = () -> {
		};
		
		final Consumer<Throwable> onError = throwable -> onUnrecoverableError(throwable, UserAction.SUBSCRIPTION, NewPipe.getNameOfService(info.getServiceId()), "Updating Subscription for " + info.getUrl(), R.string.savemasterdown_subscription_update_failed);
		
		disposables.add(subscriptionService.updateChannelInfo(info)
								.subscribeOn(Schedulers.io())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(onComplete, onError));
	}
	
	private Disposable monitorSubscribeButton(final Button subscribeButton, final Function<Object, Object> action) {
		
		final Consumer<Object> onNext = object -> {
		};
		
		final Consumer<Throwable> onError = throwable -> onUnrecoverableError(throwable,
																			  UserAction.SUBSCRIPTION,
																			  NewPipe.getNameOfService(currentInfo.getServiceId()),
																			  "Subscription Change", R.string.savemasterdown_subscription_change_failed);
		
		/* Emit clicks from main thread unto io thread */
		return RxView.clicks(subscribeButton)
				.subscribeOn(AndroidSchedulers.mainThread())
				.observeOn(Schedulers.io())
				.debounce(100, TimeUnit.MILLISECONDS) // Ignore rapid clicks
				.map(action)
				.subscribe(onNext, onError);
	}
	
	private Consumer<List<SubscriptionEntity>> getSubscribeUpdateMonitor(final ChannelInfo info) {
		
		return subscriptionEntities -> {
			
			if (subscribeButtonMonitor != null) subscribeButtonMonitor.dispose();
			
			if (subscriptionEntities.isEmpty()) {
				SubscriptionEntity channel = new SubscriptionEntity();
				channel.setServiceId(info.getServiceId());
				channel.setUrl(info.getUrl());
				channel.setData(info.getName(), info.getAvatarUrl(), info.getDescription(), info.getSubscriberCount());
				subscribeButtonMonitor = monitorSubscribeButton(headerSubscribeButton, mapOnSubscribe(channel));
			}
			else {
				final SubscriptionEntity subscription = subscriptionEntities.get(0);
				subscribeButtonMonitor = monitorSubscribeButton(headerSubscribeButton, mapOnUnsubscribe(subscription));
			}
		};
	}
	
	private void updateSubscribeButton(boolean isSubscribed) {
		
		boolean isButtonVisible = headerSubscribeButton.getVisibility() == View.VISIBLE;
		int backgroundDuration = isButtonVisible ? 300 : 0;
		int textDuration = isButtonVisible ? 200 : 0;
		
		int subscribeBackground = ContextCompat.getColor(activity, R.color.savemasterdown_subscribe_background_color);
		int subscribeText = ContextCompat.getColor(activity, R.color.savemasterdown_subscribe_text_color);
		int subscribedBackground = ContextCompat.getColor(activity, R.color.savemasterdown_subscribed_background_color);
		int subscribedText = ContextCompat.getColor(activity, R.color.savemasterdown_subscribed_text_color);
		
		if (!isSubscribed) {
			headerSubscribeButton.setText(R.string.savemasterdown_subscribe_button_title);
			AnimationUtils.animateBackgroundColor(headerSubscribeButton, backgroundDuration, subscribedBackground, subscribeBackground);
			AnimationUtils.animateTextColor(headerSubscribeButton, textDuration, subscribedText, subscribeText);
		}
		else {
			headerSubscribeButton.setText(R.string.savemasterdown_subscribed_button_title);
			AnimationUtils.animateBackgroundColor(headerSubscribeButton, backgroundDuration, subscribeBackground, subscribedBackground);
			AnimationUtils.animateTextColor(headerSubscribeButton, textDuration, subscribeText, subscribedText);
		}
		
		AnimationUtils.animateView(headerSubscribeButton, AnimationUtils.Type.LIGHT_SCALE_AND_ALPHA, true, 100);
	}
	
	// Load and handle
	@Override
	protected Single<ListExtractor.InfoItemsPage> loadMoreItemsLogic() {
		
		return ExtractorHelper.getMoreChannelItems(serviceId, url, currentNextPage);
	}
	
	@Override
	protected Single<ChannelInfo> loadResult(boolean forceLoad) {
		
		return ExtractorHelper.getChannelInfo(serviceId, url, forceLoad);
	}
	
	// Contract
	@Override
	public void showLoading() {
		
		super.showLoading();
		
		ImageLoader.getInstance().cancelDisplayTask(headerChannelBanner);
		ImageLoader.getInstance().cancelDisplayTask(headerAvatarView);
		AnimationUtils.animateView(headerSubscribeButton, false, 100);
	}
	
	@Override
	public void handleResult(@NonNull ChannelInfo result) {
		
		super.handleResult(result);
		
		headerRootLayout.setVisibility(View.VISIBLE);
		GlideUtils.loadChannelBanner(App.getAppContext(), headerChannelBanner, result.getBannerUrl());
		String avatarUrl = TextUtils.isEmpty(result.getAvatarUrl()) ? result.getAvatarUrl() : result.getAvatarUrl().replace("s100", "s720");
		GlideUtils.loadAvatar(App.getAppContext(), headerAvatarView, avatarUrl);
		
		if (result.getSubscriberCount() != -1) {
			headerSubscribersTextView.setText(Localization.localizeSubscribersCount(activity, result.getSubscriberCount()));
			headerSubscribersTextView.setVisibility(View.VISIBLE);
		}
		else headerSubscribersTextView.setVisibility(View.GONE);
		
		if (!result.getErrors().isEmpty()) {
			showSnackBarError(result.getErrors(), UserAction.REQUESTED_CHANNEL, NewPipe.getNameOfService(result.getServiceId()), result.getUrl(), 0);
		}
		
		if (disposables != null) disposables.clear();
		if (subscribeButtonMonitor != null) subscribeButtonMonitor.dispose();
		updateSubscription(result);
		monitorSubscription(result);
		
		headerPlayAllButton.setOnClickListener(view -> AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnMainPlayer(activity, getPlayQueue(), true)));
		headerPopupButton.setOnClickListener(view -> AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnPopupPlayer(activity, getPlayQueue(), false)));
	}
	
	private PlayQueue getPlayQueue() {
		return getPlayQueue(0);
	}
	
	private PlayQueue getPlayQueue(final int index) {
		
		final List<StreamInfoItem> streamItems = new ArrayList<>();
		for (InfoItem i : infoListAdapter.getItemsList()) {
			if (i instanceof StreamInfoItem) {
				streamItems.add((StreamInfoItem) i);
			}
		}
		return new ChannelPlayQueue(currentInfo.getServiceId(), currentInfo.getUrl(), currentInfo.getNextPage(), streamItems, index);
	}
	
	@Override
	public void handleNextItems(ListExtractor.InfoItemsPage result) {
		
		super.handleNextItems(result);
		
		if (!result.getErrors().isEmpty()) {
			showSnackBarError(result.getErrors(), UserAction.REQUESTED_CHANNEL, NewPipe.getNameOfService(serviceId), "Get next page of: " + url, R.string.savemasterdown_error);
		}
	}
	
	// OnError
	@Override
	protected boolean onError(Throwable exception) {
		
		if (super.onError(exception)) return true;
		
		int errorId = exception instanceof ExtractionException ? R.string.savemasterdown_parsing_error : R.string.savemasterdown_error;
		onUnrecoverableError(exception, UserAction.REQUESTED_CHANNEL, NewPipe.getNameOfService(serviceId), url, errorId);
		return true;
	}
	
	@Override
	public void setTitle(String title) {
		toolbarTitle = title;
	}
	
	@Override
	public boolean onBackPressed() {
		
		// pop back stack
		if (getFragmentManager() != null) {
			getFragmentManager().popBackStack();
			return true;
		}
		
		return false;
	}

	MyCommon myCommon = new MyCommon();
	private void showNativeAd() {
		if(getActivity() == null || getActivity().isFinishing()){
			return;
		}
		myCommon.loadBigNative(getActivity(), nativeAdView);
	}
}