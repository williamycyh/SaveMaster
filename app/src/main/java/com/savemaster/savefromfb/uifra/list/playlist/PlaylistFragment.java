package com.savemaster.savefromfb.uifra.list.playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdLoader;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.VideoOptions;
//import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.savemaster.savefromfb.db.GAGTubeDatabase;
import com.savemaster.savefromfb.db.playlist.model.PlaylistRemoteEntity;
import com.savemaster.savefromfb.uifra.BackPressable;
import com.savemaster.moton.MyCommon;
import com.savemaster.savefromfb.util.UserAction;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.ListExtractor;
import savemaster.save.master.pipd.NewPipe;
import savemaster.save.master.pipd.exceptions.ExtractionException;
import savemaster.save.master.pipd.playlist.PlaylistInfo;
import savemaster.save.master.pipd.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.ads.AppInterstitialAd;
import com.savemaster.savefromfb.uifra.list.BaseListInfoFragment;
import com.savemaster.savefromfb.local.playlist.RemotePlaylistManager;
import com.savemaster.savefromfb.player.playqueue.PlayQueue;
import com.savemaster.savefromfb.player.playqueue.PlaylistPlayQueue;
import com.savemaster.savefromfb.util.AnimationUtils;
import com.savemaster.savefromfb.util.ExtractorHelper;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.SharedUtils;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class PlaylistFragment extends BaseListInfoFragment<PlaylistInfo> implements BackPressable {
	
	private CompositeDisposable disposables;
	private Subscription bookmarkReactor;
	private AtomicBoolean isBookmarkButtonReady;
	
	private RemotePlaylistManager remotePlaylistManager;
	private PlaylistRemoteEntity playlistEntity;
	
	// Views
	private Toolbar mToolbar;
	private MaterialButton headerPlayAllButton;
	private MaterialButton headerPopupButton;
	private View headerShareButton;
	private MenuItem playlistBookmarkButton;
	
	// NativeAd
	private FrameLayout nativeAdView;

	@BindView(R.id.banner_container_file) FrameLayout banner_container_file;
	
	public static PlaylistFragment getInstance(int serviceId, String url, String name) {
		
		PlaylistFragment instance = new PlaylistFragment();
		instance.setInitialData(serviceId, url, name);
		return instance;
	}
	
	// LifeCycle
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		disposables = new CompositeDisposable();
		isBookmarkButtonReady = new AtomicBoolean(false);
		remotePlaylistManager = new RemotePlaylistManager(GAGTubeDatabase.getInstance(activity));
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.savemasterdown_fragment_playlist, container, false);
		ButterKnife.bind(this, view);
		
		return view;
	}
	
	@Override
	protected void initViews(View rootView, Bundle savedInstanceState) {
		
		super.initViews(rootView, savedInstanceState);
		
		headerPlayAllButton = rootView.findViewById(R.id.playlist_ctrl_play_all_button);
		headerPopupButton = rootView.findViewById(R.id.playlist_ctrl_play_popup_button);
		headerShareButton = rootView.findViewById(R.id.playlist_ctrl_share);
		
		infoListAdapter.useMiniItemVariants(true);
		
		mToolbar = rootView.findViewById(R.id.default_toolbar);
		activity.getDelegate().setSupportActionBar(mToolbar);
		
		View headerRootLayout = activity.getLayoutInflater().inflate(R.layout.savemasterdown_native_ad_list_header, itemsList, false);
		nativeAdView = headerRootLayout.findViewById(R.id.template_view);
		infoListAdapter.setHeader(headerRootLayout);
		
		// show ad
		showBannerAd();
	}
	
	@Override
	public void onPause() {
//		if (adView != null) {
//			adView.pause();
//		}
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		AppInterstitialAd.getInstance().init(activity);
		// show ad
		showNativeAd();
//		if (adView != null) {
//			adView.resume();
//		}
		// show ad
		showBannerAd();
	}
	
	@Override
	public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
		
		super.onCreateOptionsMenu(menu, inflater);
		
		ActionBar actionBar = activity.getDelegate().getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
		}
		
		inflater.inflate(R.menu.savemasterdown_menu_playlist, menu);
		
		playlistBookmarkButton = menu.findItem(R.id.menu_item_bookmark);
		updateBookmarkButtons();
	}
	
	@Override
	public void onViewCreated(@NonNull View rootView, Bundle savedInstanceState) {
		
		super.onViewCreated(rootView, savedInstanceState);
		
		mToolbar.setNavigationOnClickListener(view -> onPopBackStack());
	}
	
	@Override
	public void onDestroyView() {
//		if (adView != null) {
//			adView.destroy();
//		}
		super.onDestroyView();
		
		if (isBookmarkButtonReady != null) isBookmarkButtonReady.set(false);
		
		if (disposables != null) disposables.clear();
		if (bookmarkReactor != null) bookmarkReactor.cancel();
		
		bookmarkReactor = null;
	}
	
	@Override
	public void onDestroy() {
		
//		// destroy ad
//		if (nativeAdView != null) {
//			nativeAdView.destroyNativeAd();
//		}
		
		super.onDestroy();
		
		if (disposables != null) disposables.dispose();
		
		disposables = null;
		remotePlaylistManager = null;
		playlistEntity = null;
		isBookmarkButtonReady = null;
	}
	
	// Load and handle
	@Override
	protected Single<ListExtractor.InfoItemsPage> loadMoreItemsLogic() {
		return ExtractorHelper.getMorePlaylistItems(serviceId, url, currentNextPage);
	}
	
	@Override
	protected Single<PlaylistInfo> loadResult(boolean forceLoad) {
		return ExtractorHelper.getPlaylistInfo(serviceId, url, forceLoad);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == R.id.menu_item_bookmark) {
			onBookmarkClicked();
		}
		return super.onOptionsItemSelected(item);
	}
	
	// Contract
	@Override
	public void showLoading() {
		
		super.showLoading();
		
		AnimationUtils.animateView(itemsList, false, 100);
	}
	
	@Override
	public void handleResult(@NonNull final PlaylistInfo result) {
		
		super.handleResult(result);

		String streamCount = result.getStreamCount() <= 0 ? "∞" : Localization.localizeStreamCount(activity, result.getStreamCount());
		mToolbar.setSubtitle(streamCount);
		
		if (!result.getErrors().isEmpty()) {
			showSnackBarError(result.getErrors(), UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(result.getServiceId()), result.getUrl(), 0);
		}
		
		remotePlaylistManager.getPlaylist(result)
				.flatMap(lists -> getUpdateProcessor(lists, result), (lists, id) -> lists)
				.onBackpressureLatest()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(getPlaylistBookmarkSubscriber());
		
		headerPlayAllButton.setOnClickListener(view -> AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnMainPlayer(activity, getPlayQueue(), true)));
		
		headerPopupButton.setOnClickListener(view -> AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnPopupPlayer(activity, getPlayQueue(), false)));
		
		headerShareButton.setOnClickListener(view -> SharedUtils.shareUrl(activity));
	}
	
	private PlayQueue getPlayQueue() {
		return getPlayQueue(0);
	}
	
	private PlayQueue getPlayQueue(final int index) {
		final List<StreamInfoItem> infoItems = new ArrayList<>();
		for (InfoItem i : infoListAdapter.getItemsList()) {
			if (i instanceof StreamInfoItem) {
				infoItems.add((StreamInfoItem) i);
			}
		}
		return new PlaylistPlayQueue(
				currentInfo.getServiceId(),
				currentInfo.getUrl(),
				currentInfo.getNextPage(),
				infoItems,
				index
		);
	}
	
	@Override
	public void handleNextItems(ListExtractor.InfoItemsPage result) {
		
		super.handleNextItems(result);
		
		if (!result.getErrors().isEmpty()) {
			showSnackBarError(result.getErrors(), UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(serviceId), "Get next page of: " + url, 0);
		}
	}
	
	// OnError
	@Override
	protected boolean onError(Throwable exception) {
		
		if (super.onError(exception)) return true;
		
		int errorId = exception instanceof ExtractionException ? R.string.savemasterdown_parsing_error : R.string.savemasterdown_error;
		onUnrecoverableError(exception, UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(serviceId), url, errorId);
		return true;
	}
	
	// Utils
	private Flowable<Integer> getUpdateProcessor(@NonNull List<PlaylistRemoteEntity> playlists, @NonNull PlaylistInfo result) {
		
		final Flowable<Integer> noItemToUpdate = Flowable.just(-1);
		if (playlists.isEmpty()) return noItemToUpdate;
		
		final PlaylistRemoteEntity playlistEntity = playlists.get(0);
		if (playlistEntity.isIdenticalTo(result)) return noItemToUpdate;
		
		return remotePlaylistManager.onUpdate(playlists.get(0).getUid(), result).toFlowable();
	}
	
	private Subscriber<List<PlaylistRemoteEntity>> getPlaylistBookmarkSubscriber() {
		
		return new Subscriber<List<PlaylistRemoteEntity>>() {
			
			@Override
			public void onSubscribe(Subscription s) {
				
				if (bookmarkReactor != null) bookmarkReactor.cancel();
				bookmarkReactor = s;
				bookmarkReactor.request(1);
			}
			
			@Override
			public void onNext(List<PlaylistRemoteEntity> playlist) {
				
				playlistEntity = playlist.isEmpty() ? null : playlist.get(0);
				
				updateBookmarkButtons();
				isBookmarkButtonReady.set(true);
				
				if (bookmarkReactor != null) bookmarkReactor.request(1);
			}
			
			@Override
			public void onError(Throwable throwable) {
				PlaylistFragment.this.onError(throwable);
			}
			
			@Override
			public void onComplete() {
				
			}
		};
	}
	
	private void onBookmarkClicked() {
		
		if (isBookmarkButtonReady == null || !isBookmarkButtonReady.get() || remotePlaylistManager == null) return;
		
		final Disposable action;
		
		if (currentInfo != null && playlistEntity == null) {
			action = remotePlaylistManager.onBookmark(currentInfo)
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(ignored -> Toast.makeText(getContext(), getString(R.string.savemasterdown_added_playlist_to_bookmark), Toast.LENGTH_SHORT).show(), this::onError);
		}
		else if (playlistEntity != null) {
			action = remotePlaylistManager.deletePlaylist(playlistEntity.getUid())
					.observeOn(AndroidSchedulers.mainThread())
					.doFinally(() -> playlistEntity = null)
					.subscribe(ignored -> Toast.makeText(getContext(), getString(R.string.savemasterdown_removed_playlist_from_bookmark), Toast.LENGTH_SHORT).show(), this::onError);
		}
		else {
			action = Disposables.empty();
		}
		
		disposables.add(action);
	}
	
	private void updateBookmarkButtons() {
		
		if (playlistBookmarkButton == null || activity == null) return;
		
		final int iconAttr = playlistEntity == null ? R.drawable.savemasterdown_ic_playlist_add_white_24dp : R.drawable.savemasterdown_ic_playlist_add_check_white;
		
		final int titleRes = playlistEntity == null ? R.string.savemasterdown_bookmark_playlist : R.string.savemasterdown_removed_playlist_from_bookmark;
		
		playlistBookmarkButton.setIcon(iconAttr);
		playlistBookmarkButton.setTitle(titleRes);
	}

	MyCommon myCommon = new MyCommon();
	private void showNativeAd() {
		if(getActivity() == null || getActivity().isFinishing()){
			return;
		}
		myCommon.loadBigNative(getActivity(), nativeAdView);
	}
	
	private void showBannerAd() {
		myCommon.loadMinNative(getActivity(), banner_container_file);
	}
	
	private void onPopBackStack() {
		
		// pop back stack
		if (getFragmentManager() != null) {
			getFragmentManager().popBackStack();
		}
	}
	
	@Override
	public boolean onBackPressed() {
		
		onPopBackStack();
		return true;
	}
}