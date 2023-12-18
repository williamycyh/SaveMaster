package com.savemaster.savefromfb.uifra.list;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.savemaster.savefromfb.uiact.MainActivity;
import com.savemaster.savefromfb.uifra.BaseStateFragment;
import com.savemaster.savefromfb.uifra.OnScrollBelowItemsListener;
import com.savemaster.savefromfb.uifra.list.channel.ChannelFragment;

import org.jetbrains.annotations.NotNull;
import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.channel.ChannelInfoItem;
import savemaster.save.master.pipd.playlist.PlaylistInfoItem;
import savemaster.save.master.pipd.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import com.savemaster.savefromfb.R;
import com.savemaster.savefromfb.ads.AppInterstitialAd;
import com.savemaster.savefromfb.info_list.InfoListAdapter;
import com.savemaster.savefromfb.local.dialog.PlaylistAppendDialog;
import com.savemaster.savefromfb.player.playqueue.PlayQueue;
import com.savemaster.savefromfb.player.playqueue.SinglePlayQueue;
import com.savemaster.savefromfb.util.AnimationUtils;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.OnClickGesture;
import com.savemaster.savefromfb.util.SharedUtils;
import com.savemaster.savefromfb.util.StateSaver;

public abstract class BaseListFragment<I, N> extends BaseStateFragment<I> implements ListViewContract<I, N>, StateSaver.WriteRead {
	
	private static final String TAG = BaseListFragment.class.getSimpleName();
	
	// Views
	protected InfoListAdapter infoListAdapter;
	protected RecyclerView itemsList;
	
	// LifeCycle
	@Override
	public void onAttach(@NotNull Context context) {
		
		super.onAttach(context);
		
		// second param to unsubscribe channel in ChannelFragment
		infoListAdapter = new InfoListAdapter(activity, this instanceof ChannelFragment);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		AppInterstitialAd.getInstance().init(activity);
	}
	
	@Override
	public void onDestroy() {
		
		super.onDestroy();
		
		StateSaver.onDestroy(savedState);
	}
	
	// State Saving
	protected StateSaver.SavedState savedState;
	
	@Override
	public String generateSuffix() {
		
		// Naive solution, but it's good for now (the items don't change)
		return "." + infoListAdapter.getItemsList().size() + ".list";
	}
	
	@Override
	public void writeTo(Queue<Object> objectsToSave) {
		
		objectsToSave.add(infoListAdapter.getItemsList());
	}
	
	@Override
	public void readFrom(@NonNull Queue<Object> savedObjects) throws Exception {
		
		infoListAdapter.getItemsList().clear();
		infoListAdapter.getItemsList().addAll((List<InfoItem>) savedObjects.poll());
	}
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle bundle) {
		
		super.onSaveInstanceState(bundle);
		
		savedState = StateSaver.tryToSave(activity.isChangingConfigurations(), savedState, bundle, this);
	}
	
	@Override
	protected void onRestoreInstanceState(@NonNull Bundle bundle) {
		
		super.onRestoreInstanceState(bundle);
		
		savedState = StateSaver.tryToRestore(bundle, this);
	}
	
	// Init
	protected View getListHeader() {
		return null;
	}
	
	protected View getListFooter() {
		return activity.getLayoutInflater().inflate(R.layout.savemasterdown_pignate_footer, itemsList, false);
	}
	
	protected RecyclerView.LayoutManager getListLayoutManager() {
		return new LinearLayoutManager(activity);
	}
	
	@Override
	protected void initViews(View rootView, Bundle savedInstanceState) {
		
		super.initViews(rootView, savedInstanceState);
		
		itemsList = rootView.findViewById(R.id.items_list);
		itemsList.setLayoutManager(getListLayoutManager());
		
		infoListAdapter.setFooter(getListFooter());
		infoListAdapter.setHeader(getListHeader());
		itemsList.setAdapter(infoListAdapter);
		
	}
	
	protected void onItemSelected(InfoItem selectedItem) {
		
	}
	
	@Override
	protected void initListeners() {
		
		super.initListeners();
		
		infoListAdapter.setOnStreamSelectedListener(new OnClickGesture<StreamInfoItem>() {
			
			@Override
			public void selected(StreamInfoItem selectedItem) {
				onStreamSelected(selectedItem);
				if(getActivity() instanceof MainActivity){
					((MainActivity)getActivity()).showDetailADFragment();
				}
//				AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> onStreamSelected(selectedItem));
			}
			
			@Override
			public void more(StreamInfoItem selectedItem, View view) {
				showPopupMenu(selectedItem, view);
			}
		});
		
		infoListAdapter.setOnChannelSelectedListener(new OnClickGesture<ChannelInfoItem>() {
			
			@Override
			public void selected(ChannelInfoItem selectedItem) {
				
				try {
					onItemSelected(selectedItem);
					NavigationHelper.openChannelFragment(useAsFrontPage ? requireParentFragment().getFragmentManager() : getFragmentManager(),
														 selectedItem.getServiceId(),
														 selectedItem.getUrl(),
														 selectedItem.getName());
				}
				catch (Exception ignored) {
				}
			}
		});
		
		infoListAdapter.setOnPlaylistSelectedListener(new OnClickGesture<PlaylistInfoItem>() {
			
			@Override
			public void selected(PlaylistInfoItem selectedItem) {
				
				try {
					onItemSelected(selectedItem);
					NavigationHelper.openPlaylistFragment(useAsFrontPage ? requireParentFragment().getFragmentManager() : getFragmentManager(),
														  selectedItem.getServiceId(),
														  selectedItem.getUrl(),
														  selectedItem.getName());
				}
				catch (Exception ignored) {
				}
			}
		});
		
		itemsList.clearOnScrollListeners();
		itemsList.addOnScrollListener(new OnScrollBelowItemsListener() {
			@Override
			public void onScrolledDown(RecyclerView recyclerView) {
				onScrollToBottom();
			}
		});
	}
	
	private void onStreamSelected(StreamInfoItem selectedItem) {
		
		onItemSelected(selectedItem);
		NavigationHelper.openVideoDetailFragment(getFM(), selectedItem.getServiceId(), selectedItem.getUrl(), selectedItem.getName());
	}
	
	protected void onScrollToBottom() {
		
		if (hasMoreItems() && !isLoading.get()) {
			loadMoreItems();
		}
	}
	
	protected void showPopupMenu(final StreamInfoItem streamInfoItem, final View view) {
		
		final Context context = getContext();
		if (context == null || context.getResources() == null || getActivity() == null) return;
		
		PopupMenu popup = new PopupMenu(getContext(), view, Gravity.END, 0, R.style.mPopupMenu);
		popup.getMenuInflater().inflate(R.menu.savemasterdown_menu_popup, popup.getMenu());
		popup.show();
		
		popup.setOnMenuItemClickListener(item -> {
			
			int id = item.getItemId();
			final int index = Math.max(infoListAdapter.getItemsList().indexOf(streamInfoItem), 0);
			switch (id) {
				
				case R.id.action_play:
					AppInterstitialAd.getInstance().showInterstitialAd(activity, () -> NavigationHelper.playOnMainPlayer(activity, getPlayQueue(index), true));
					break;
				
				case R.id.action_append_playlist:
					if (getFragmentManager() != null) {
						PlaylistAppendDialog.fromStreamInfoItems(Collections.singletonList(streamInfoItem)).show(getFragmentManager(), TAG);
					}
					break;
				
				case R.id.action_share:
					SharedUtils.shareUrl(getContext());
					break;
			}
			return true;
		});
	}
	
	private PlayQueue getPlayQueue(final int index) {
		
		if (infoListAdapter == null) {
			return new SinglePlayQueue(Collections.emptyList(), 0);
		}
		
		final List<InfoItem> infoItems = infoListAdapter.getItemsList();
		List<StreamInfoItem> streamInfoItems = new ArrayList<>(infoItems.size());
		for (final InfoItem item : infoItems) {
			if (item instanceof StreamInfoItem) {
				streamInfoItems.add((StreamInfoItem) item);
			}
		}
		return new SinglePlayQueue(streamInfoItems, index);
	}
	
	// Menu
	@Override
	public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
		
		super.onCreateOptionsMenu(menu, inflater);
		
		ActionBar supportActionBar = activity.getSupportActionBar();
		if (supportActionBar != null) {
			
			supportActionBar.setDisplayShowTitleEnabled(true);
			if (useAsFrontPage) {
				supportActionBar.setDisplayHomeAsUpEnabled(false);
			}
			else {
				supportActionBar.setDisplayHomeAsUpEnabled(true);
			}
		}
	}
	
	// Load and handle
	protected abstract void loadMoreItems();
	
	protected abstract boolean hasMoreItems();
	
	// Contract
	@Override
	public void showLoading() {
		
		super.showLoading();
	}
	
	@Override
	public void hideLoading() {
		
		super.hideLoading();
		
		AnimationUtils.animateView(itemsList, true, 200);
	}
	
	@Override
	public void showError(String message, boolean showRetryButton) {
		
		super.showError(message, showRetryButton);
		
		showListFooter(false);
		AnimationUtils.animateView(itemsList, false, 200);
	}
	
	@Override
	public void showEmptyState() {
		
		super.showEmptyState();
		
		showListFooter(false);
	}
	
	@Override
	public void showListFooter(final boolean show) {
		
		itemsList.post(() -> {
			
			if (infoListAdapter != null && itemsList != null) {
				infoListAdapter.showFooter(show);
			}
		});
	}
	
	@Override
	public void handleNextItems(N result) {
		isLoading.set(false);
	}
}
