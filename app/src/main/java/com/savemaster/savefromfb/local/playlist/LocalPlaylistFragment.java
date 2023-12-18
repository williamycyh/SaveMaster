package com.savemaster.savefromfb.local.playlist;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.LoadAdError;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import savemaster.save.master.pipd.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.savemaster.savefromfb.ads.AppInterstitialAd;
import com.savemaster.savefromfb.db.GAGTubeDatabase;
import com.savemaster.savefromfb.db.LocalItem;
import com.savemaster.savefromfb.db.playlist.PlaylistStreamEntry;
import com.savemaster.savefromfb.uifra.BackPressable;
import com.savemaster.savefromfb.local.BaseLocalListFragment;
import com.savemaster.moton.MyCommon;
import com.savemaster.savefromfb.util.UserAction;
import com.savemaster.savefromfb.util.AnimationUtils;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.OnClickGesture;
import com.savemaster.savefromfb.util.SharedUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;
import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.player.playqueue.PlayQueue;
import com.savemaster.savefromfb.player.playqueue.SinglePlayQueue;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.subjects.PublishSubject;

public class LocalPlaylistFragment extends BaseLocalListFragment<List<PlaylistStreamEntry>, Void> implements BackPressable {
	
	// Save the list 10 seconds after the last change occurred
	private static final long SAVE_DEBOUNCE_MILLIS = 10000;
	private static final int MINIMUM_INITIAL_DRAG_VELOCITY = 10;
	
	@BindView(R.id.playlist_control) View playlistControl;
	@BindView(R.id.playlist_ctrl_play_all_button) View headerPlayAllButton;
	@BindView(R.id.playlist_ctrl_play_popup_button) View headerPopupButton;
	@BindView(R.id.default_toolbar) Toolbar mToolbar;

	@BindView(R.id.banner_container_file)
	FrameLayout banner_container_file;
	
	@State
	protected Long playlistId;
	@State
	protected String name;
	
	private ItemTouchHelper itemTouchHelper;
	
	private LocalPlaylistManager playlistManager;
	private Subscription databaseSubscription;
	
	private PublishSubject<Long> debouncedSaveSignal;
	private CompositeDisposable disposables;
	
	/* Has the playlist been fully loaded from db */
	private AtomicBoolean isLoadingComplete;
	/* Has the playlist been modified (e.g. items reordered or deleted) */
	private AtomicBoolean isModified;
	
	public static LocalPlaylistFragment getInstance(long playlistId, String name) {
		
		LocalPlaylistFragment instance = new LocalPlaylistFragment();
		instance.setInitialData(playlistId, name);
		return instance;
	}
	
	// Fragment LifeCycle - Creation
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		playlistManager = new LocalPlaylistManager(GAGTubeDatabase.getInstance(activity));
		debouncedSaveSignal = PublishSubject.create();
		
		disposables = new CompositeDisposable();
		
		isLoadingComplete = new AtomicBoolean();
		isModified = new AtomicBoolean();
		
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.savemasterdown_fragment_local_playlist, container, false);
		ButterKnife.bind(this, view);
		
		return view;
	}
	
	// Fragment Lifecycle - Views
	@Override
	protected void initViews(View rootView, Bundle savedInstanceState) {
		
		super.initViews(rootView, savedInstanceState);
		
		// toolbar
		activity.getDelegate().setSupportActionBar(mToolbar);
		
		// init InterstitialAd
		AppInterstitialAd.getInstance().init(activity);
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
	}
	
	@Override
	public void onViewCreated(@NonNull View rootView, Bundle savedInstanceState) {
		
		super.onViewCreated(rootView, savedInstanceState);
		
		mToolbar.setNavigationOnClickListener(view -> onPopBackStack());
	}
	
	@Override
	protected View getListHeader() {
		return null;
	}
	
	@Override
	protected void initListeners() {
		
		super.initListeners();
		
		itemTouchHelper = new ItemTouchHelper(getItemTouchCallback());
		itemTouchHelper.attachToRecyclerView(itemsList);
		
		itemListAdapter.setSelectedListener(new OnClickGesture<LocalItem>() {
			
			@Override
			public void selected(LocalItem selectedItem) {
				
				if (selectedItem instanceof PlaylistStreamEntry) {
					
					final PlaylistStreamEntry item = (PlaylistStreamEntry) selectedItem;
					NavigationHelper.openVideoDetailFragment(getFM(), item.serviceId, item.url, item.title);
				}
			}
			
			@Override
			public void held(LocalItem selectedItem, View view) {
				
				if (selectedItem instanceof PlaylistStreamEntry) {
					showPopupMenu((PlaylistStreamEntry) selectedItem, view);
				}
			}
			
			@Override
			public void drag(LocalItem selectedItem, RecyclerView.ViewHolder viewHolder) {
				
				if (itemTouchHelper != null) itemTouchHelper.startDrag(viewHolder);
			}
		});
	}
	
	// Fragment Lifecycle - Loading
	@Override
	public void showLoading() {
		
		super.showLoading();
		
		if (playlistControl != null) AnimationUtils.animateView(playlistControl, false, 200);
	}
	
	@Override
	public void hideLoading() {
		
		super.hideLoading();
		
		if (playlistControl != null) {
			AnimationUtils.animateView(playlistControl, true, 200);
		}
	}
	
	@Override
	public void startLoading(boolean forceLoad) {
		
		super.startLoading(forceLoad);
		
		if (disposables != null) {
			
			disposables.clear();
			disposables.add(getDebouncedSaver());
		}
		
		isLoadingComplete.set(false);
		isModified.set(false);
		
		playlistManager.getPlaylistStreams(playlistId)
				.onBackpressureLatest()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(getPlaylistObserver());
	}
	
	// Fragment Lifecycle - Destruction
	@Override
	public void onPause() {
//		if (adView != null) {
//			adView.pause();
//		}
		super.onPause();
		
		// Save on exit
		saveImmediate();
	}
	
	@Override
	public void onResume() {
		super.onResume();
//		if (adView != null) {
//			adView.resume();
//		}
		// show ad
		showBannerAd();
	}
	
	@Override
	public void onDestroyView() {
//		if (adView != null) {
//			adView.destroy();
//		}
		super.onDestroyView();
		
		if (itemListAdapter != null) itemListAdapter.unsetSelectedListener();
		if (headerPlayAllButton != null) headerPlayAllButton.setOnClickListener(null);
		if (headerPopupButton != null) headerPopupButton.setOnClickListener(null);
		
		if (databaseSubscription != null) databaseSubscription.cancel();
		if (disposables != null) disposables.clear();
		
		databaseSubscription = null;
		itemTouchHelper = null;
		
		mToolbar.setSubtitle(null);
	}
	
	@Override
	public void onDestroy() {
		
		super.onDestroy();
		
		if (debouncedSaveSignal != null) debouncedSaveSignal.onComplete();
		if (disposables != null) disposables.dispose();
		
		debouncedSaveSignal = null;
		playlistManager = null;
		disposables = null;
		
		isLoadingComplete = null;
		isModified = null;
	}
	
	// Playlist Stream Loader
	private Subscriber<List<PlaylistStreamEntry>> getPlaylistObserver() {
		
		return new Subscriber<List<PlaylistStreamEntry>>() {
			
			@Override
			public void onSubscribe(Subscription s) {
				
				showLoading();
				isLoadingComplete.set(false);
				
				if (databaseSubscription != null) databaseSubscription.cancel();
				databaseSubscription = s;
				databaseSubscription.request(1);
			}
			
			@Override
			public void onNext(List<PlaylistStreamEntry> streams) {
				
				// Skip handling the result after it has been modified
				if (isModified == null || !isModified.get()) {
					
					handleResult(streams);
					isLoadingComplete.set(true);
				}
				
				if (databaseSubscription != null) databaseSubscription.request(1);
			}
			
			@Override
			public void onError(Throwable exception) {
				LocalPlaylistFragment.this.onError(exception);
			}
			
			@Override
			public void onComplete() {
			}
		};
	}
	
	@Override
	public void handleResult(@NonNull List<PlaylistStreamEntry> result) {
		
		super.handleResult(result);
		
		if (itemListAdapter == null) return;
		
		itemListAdapter.clearStreamItemList();
		setTitle(name);
		setVideoCount(result.size());
		
		if (result.isEmpty()) {
			showEmptyState();
			return;
		}
		
		itemListAdapter.addItems(result);
		
		headerPlayAllButton.setOnClickListener(view -> {
			if (!itemListAdapter.getItemsList().isEmpty()) {
				NavigationHelper.playOnMainPlayer(activity, getPlayQueue(), true);
			}
		});
		headerPopupButton.setOnClickListener(view -> {
			if (!itemListAdapter.getItemsList().isEmpty()) {
				AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnPopupPlayer(activity, getPlayQueue(), false));
			}
		});
		
		hideLoading();
	}
	
	// Fragment Error Handling
	@Override
	protected void resetFragment() {
		
		super.resetFragment();
		
		if (databaseSubscription != null) databaseSubscription.cancel();
	}
	
	@Override
	protected boolean onError(Throwable exception) {
		
		if (super.onError(exception)) return true;
		
		onUnrecoverableError(exception, UserAction.SOMETHING_ELSE, "none", "Local Playlist", R.string.savemasterdown_error);
		return true;
	}
	
	// Playlist Metadata/Streams Manipulation
	private void changePlaylistName(final String name) {
		
		if (playlistManager == null) return;
		
		this.name = name;
		setTitle(name);
		
		final Disposable disposable = playlistManager.renamePlaylist(playlistId, name)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(longs -> {/*Do nothing on success*/}, this::onError);
		
		disposables.add(disposable);
	}
	
	private void changeThumbnailUrl(final String thumbnailUrl) {
		
		if (playlistManager == null) return;
		
		final Toast successToast = Toast.makeText(getActivity(), R.string.savemasterdown_playlist_thumbnail_change_success, Toast.LENGTH_SHORT);
		
		final Disposable disposable = playlistManager
				.changePlaylistThumbnail(playlistId, thumbnailUrl)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(ignore -> successToast.show(), this::onError);
		
		disposables.add(disposable);
	}
	
	private void deleteItem(final PlaylistStreamEntry item) {
		
		if (itemListAdapter == null) return;
		
		itemListAdapter.removeItem(item);
		
		Toast.makeText(activity, R.string.savemasterdown_msg_delete_successfully, Toast.LENGTH_SHORT).show();
		
		// update count in toolbar
		setVideoCount(itemListAdapter.getItemsList().size());
		saveChanges();
	}
	
	private void saveChanges() {
		
		if (isModified == null || debouncedSaveSignal == null) return;
		
		isModified.set(true);
		debouncedSaveSignal.onNext(System.currentTimeMillis());
	}
	
	private Disposable getDebouncedSaver() {
		
		if (debouncedSaveSignal == null) return Disposables.empty();
		
		return debouncedSaveSignal
				.debounce(SAVE_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(ignored -> saveImmediate(), this::onError);
	}
	
	private void saveImmediate() {
		
		if (playlistManager == null || itemListAdapter == null) return;
		
		// List must be loaded and modified in order to save
		if (isLoadingComplete == null || isModified == null || !isLoadingComplete.get() || !isModified.get()) {
			return;
		}
		
		final List<Object> items = itemListAdapter.getItemsList();
		List<Long> streamIds = new ArrayList<>(items.size());
		for (final Object item : items) {
			
			if (item instanceof PlaylistStreamEntry) {
				streamIds.add(((PlaylistStreamEntry) item).streamId);
			}
		}
		
		final Disposable disposable = playlistManager.updateJoin(playlistId, streamIds)
				.observeOn(AndroidSchedulers.mainThread()).subscribe(
						// onNext
						() -> {
							if (isModified != null) {
								isModified.set(false);
							}
						},
						// onError
						this::onError
				);
		
		disposables.add(disposable);
	}
	
	private ItemTouchHelper.SimpleCallback getItemTouchCallback() {
		
		return new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.ACTION_STATE_IDLE) {
			
			@Override
			public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
				
				final int standardSpeed = super.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll);
				final int minimumAbsVelocity = Math.max(MINIMUM_INITIAL_DRAG_VELOCITY, Math.abs(standardSpeed));
				return minimumAbsVelocity * (int) Math.signum(viewSizeOutOfBounds);
			}
			
			@Override
			public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
				
				if (source.getItemViewType() != target.getItemViewType() || itemListAdapter == null) {
					return false;
				}
				
				final int sourceIndex = source.getBindingAdapterPosition();
				final int targetIndex = target.getBindingAdapterPosition();
				final boolean isSwapped = itemListAdapter.swapItems(sourceIndex, targetIndex);
				if (isSwapped) saveChanges();
				return isSwapped;
			}
			
			@Override
			public boolean isLongPressDragEnabled() {
				return false;
			}
			
			@Override
			public boolean isItemViewSwipeEnabled() {
				return false;
			}
			
			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
			}
		};
	}
	
	// Utils
	protected void showPopupMenu(final PlaylistStreamEntry playlistStreamEntry, final View view) {
		
		final Context context = getContext();
		if (context == null || context.getResources() == null || getActivity() == null) return;
		
		PopupMenu popup = new PopupMenu(getContext(), view, Gravity.END, 0, R.style.mPopupMenu);
		popup.getMenuInflater().inflate(R.menu.savemasterdown_menu_popup_playlist_local, popup.getMenu());
		popup.show();
		
		popup.setOnMenuItemClickListener(item -> {
			
			int id = item.getItemId();
			final int index = Math.max(itemListAdapter.getItemsList().indexOf(playlistStreamEntry), 0);
			
			switch (id) {
				
				case R.id.action_play:
					AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnMainPlayer(activity, getPlayQueue(index), true));
					break;
				
				case R.id.action_set_thumbnail:
					changeThumbnailUrl(playlistStreamEntry.thumbnailUrl);
					break;
				
				case R.id.action_share:
					SharedUtils.shareUrl(getContext());
					break;
				
				case R.id.action_delete:
					deleteItem(playlistStreamEntry);
					if (itemListAdapter.getItemsList().isEmpty()) showEmptyState();
					break;
			}
			return true;
		});
	}
	
	private void setInitialData(long playlistId, String name) {
		
		this.playlistId = playlistId;
		this.name = !TextUtils.isEmpty(name) ? name : "";
	}
	
	private void setVideoCount(final long count) {
		
		if (activity != null) {
			mToolbar.setSubtitle(Localization.localizeStreamCount(activity, count));
		}
	}
	
	private PlayQueue getPlayQueue() {
		return getPlayQueue(0);
	}
	
	private PlayQueue getPlayQueue(final int index) {
		
		if (itemListAdapter == null) {
			return new SinglePlayQueue(Collections.emptyList(), 0);
		}
		
		final List<Object> infoItems = itemListAdapter.getItemsList();
		List<StreamInfoItem> streamInfoItems = new ArrayList<>(infoItems.size());
		
		for (final Object item : infoItems) {
			
			if (item instanceof PlaylistStreamEntry) {
				streamInfoItems.add(((PlaylistStreamEntry) item).toStreamInfoItem());
			}
		}
		return new SinglePlayQueue(streamInfoItems, index);
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
	
	private void showBannerAd() {
		MyCommon myCommon = new MyCommon();
		myCommon.loadMinNative(getActivity(), banner_container_file);
	}
}

