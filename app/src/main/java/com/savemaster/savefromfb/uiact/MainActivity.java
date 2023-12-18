package com.savemaster.savefromfb.uiact;

import static com.savemaster.savefromfb.App.appCon;
import static com.savemaster.savefromfb.util.NavigationHelper.AUTO_PLAY;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.savemaster.smlib.ASharePreferenceUtils;
import com.savemaster.smlib.MyTFloatTActivity;
import com.savemaster.savefromfb.BuildConfig;
import com.savemaster.savefromfb.ads.AppInterstitialAd;
import com.savemaster.moton.Utils;
import com.savemaster.savefromfb.uifra.BackPressable;
import com.savemaster.savefromfb.uifra.detail.VideoDetailFragment;
import com.savemaster.savefromfb.uifra.discover.DiscoverFragment;
import com.savemaster.savefromfb.uifra.list.main.TrendingFragment;
import com.savemaster.savefromfb.LibraryFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import savemaster.save.master.pipd.StreamingService;
import com.savemaster.smlib.Downloader;
import com.savemaster.smlib.FileItem;
import com.savemaster.smlib.MainLib;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.savemaster.savefromfb.R;

import com.savemaster.moton.AdCenter;
import com.savemaster.moton.AdFullScreenAd;
import com.savemaster.moton.MyCommon;
import com.savemaster.savefromfb.player.VideoPlayer;
import com.savemaster.savefromfb.player.event.OnKeyDownListener;
import com.savemaster.savefromfb.player.playqueue.PlayQueue;
import com.savemaster.savefromfb.util.Constants;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.PermissionHelper;
import com.savemaster.savefromfb.util.SerializedCache;
import com.savemaster.savefromfb.util.StateSaver;
import com.savemaster.savefromfb.util.ThemeHelper;

import java.util.List;
import java.util.Random;

public class MainActivity extends BaseActivity {
	
	@BindView(R.id.coordinator)
	CoordinatorLayout coordinatorLayout;
	@BindView(R.id.bottom_navigation)
	AHBottomNavigation mBottomNavigation;
	
	private BroadcastReceiver broadcastReceiver;

	public static boolean SHOWED_MAIN_FULL_AD = true;
	
	// Activity's LifeCycle
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(ThemeHelper.getSettingsThemeStyle(this));
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.savemasterdown_activity_main);
		ButterKnife.bind(this);
		
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			initFragments();
		}
		setSupportActionBar(findViewById(R.id.toolbar));
		setUpBottomNavigation();

		// init InterstitialAd
		AppInterstitialAd.getInstance().init(this);

		setupBroadcastReceiver();


		AdCenter.Companion.initInmobi(this);
//		initLib();
//		myCommon.loadReward(this);
		loadFirstMainFullScreen();
//		myCommon.loadFullScreenDetail(this);
//		mergeAudio();

//		setDownloadListener();
		if(appCon.dialog_type > 0 && appCon.f_type != 1){
			showCloudDialog();
		}
		PermissionHelper.checkStoragePermissions(this, 0);

		initLib();
	}

	private void setUpBottomNavigation() {
		mBottomNavigation.setBehaviorTranslationEnabled(false);
		mBottomNavigation.setTranslucentNavigationEnabled(false);
		
		// Force to tint the drawable (useful for font with icon for example)
		mBottomNavigation.setForceTint(true);
		// always show title and icon
		mBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
		
		// Change colors
		mBottomNavigation.setAccentColor(ThemeHelper.isLightThemeSelected(this) ? ContextCompat.getColor(this, R.color.light_bottom_navigation_accent_color) : ContextCompat.getColor(this, R.color.savemasterdown_white));
		mBottomNavigation.setDefaultBackgroundColor(ThemeHelper.isLightThemeSelected(this) ? ContextCompat.getColor(this, R.color.light_bottom_navigation_background_color) : ContextCompat.getColor(this, R.color.dark_bottom_navigation_background_color));
		
		AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.savemasterdown_navigation);
		navigationAdapter.setupWithBottomNavigation(mBottomNavigation);
		
		// onTabSelected listener
		mBottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
			final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
			switch (position) {
				case 0:
					/*if (!(fragment instanceof TrendingFragment)) {
						NavigationHelper.gotoMainFragment(getSupportFragmentManager());
					}*/
					if (!(fragment instanceof DiscoverFragment)) {
						NavigationHelper.openDiscoverFragment(getSupportFragmentManager());
					}
					googleRate();
					return true;
				
				case 1:
					if (!(fragment instanceof LibraryFragment)) {
						NavigationHelper.openLibraryFragment(getSupportFragmentManager());
					}
//					AppInterstitialAd.getInstance().showInterstitialAd(MainActivity.this, () -> {
//					});
					return true;
				
//				case 2:
//					if (!(fragment instanceof LibraryFragment)) {
//						NavigationHelper.openLibraryFragment(getSupportFragmentManager());
//					}
////					AppInterstitialAd.getInstance().showInterstitialAd(MainActivity.this, () -> {
////					});
//					return true;
			}
			return false;
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!isChangingConfigurations()) {
			StateSaver.clearStateFiles();
		}
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
	}
	
	@Override
	protected void onResume() {
		Localization.init();
		super.onResume();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (sharedPreferences.getBoolean(Constants.KEY_THEME_CHANGE, false)) {
			sharedPreferences.edit().putBoolean(Constants.KEY_THEME_CHANGE, false).apply();
			NavigationHelper.recreateActivity(this);
		}
		
		if (sharedPreferences.getBoolean(Constants.KEY_CONTENT_CHANGE, false)) {
			sharedPreferences.edit().putBoolean(Constants.KEY_CONTENT_CHANGE, false).apply();
			NavigationHelper.recreateActivity(this);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		if (intent != null) {
			// Return if launched from a launcher (e.g. Nova Launcher, Pixel Launcher ...)
			// to not destroy the already created backstack
			final String action = intent.getAction();
			if ((action != null && action.equals(Intent.ACTION_MAIN)) && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
				return;
			}
		}
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}
	
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_player_holder);
		if (fragment instanceof OnKeyDownListener && !bottomSheetHiddenOrCollapsed()) {
			// Provide keyDown event to fragment which then sends this event
			// to the main player service
			return ((OnKeyDownListener) fragment).onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onBackPressed() {
		// In case bottomSheet is not visible on the screen or collapsed we can assume that the user
		// interacts with a fragment inside fragment_holder so all back presses should be handled by it
		if (bottomSheetHiddenOrCollapsed()) {
			final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
			// If current fragment implements BackPressable (i.e. can/wanna handle back press)
			// delegate the back press to it
			if (fragment instanceof BackPressable) {
				if (((BackPressable) fragment).onBackPressed()) {
					return;
				}
			}
		} else {
			final Fragment fragmentPlayer = getSupportFragmentManager().findFragmentById(R.id.fragment_player_holder);
			// If current fragment implements BackPressable (i.e. can/wanna handle back press)
			// delegate the back press to it
			if (fragmentPlayer instanceof BackPressable) {
				if (!((BackPressable) fragmentPlayer).onBackPressed()) {
					final FrameLayout bottomSheetLayout = findViewById(R.id.fragment_player_holder);
					BottomSheetBehavior.from(bottomSheetLayout).setState(BottomSheetBehavior.STATE_COLLAPSED);
					setBottomNavigationVisibility(View.VISIBLE);
				}
				return;
			}
		}
		
		// if has only fragment in activity
		if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
			finish();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		for (int i : grantResults) {
			if (i == PackageManager.PERMISSION_DENIED) {
				return;
			}
		}

		switch (requestCode) {
			case PermissionHelper.DOWNLOADS_REQUEST_CODE:
				NavigationHelper.openDownloads(this);
				break;

			case PermissionHelper.DOWNLOAD_DIALOG_REQUEST_CODE:
				final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_player_holder);
				if (fragment instanceof VideoDetailFragment) {
					((VideoDetailFragment) fragment).openDownloadDialog();
				}
				break;
		}
	}
	
	private void onHomeButtonPressed() {
		// If search fragment wasn't found in the backstack...
		if (!NavigationHelper.hasSearchFragmentInBackstack(getSupportFragmentManager())) {
			// go to the main fragment
			NavigationHelper.gotoMainFragment(getSupportFragmentManager());
			mBottomNavigation.setCurrentItem(0);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onHomeButtonPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// Init fragments
	private void initFragments() {
		StateSaver.clearStateFiles();
		if (getIntent() != null && getIntent().hasExtra(Constants.KEY_LINK_TYPE)) {
			// When user watch a video inside popup and then tries to open the video in main player
			// while the app is closed he will see a blank fragment on place of kiosk.
			// Let's open it first
			if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
				NavigationHelper.openMainFragment(getSupportFragmentManager());
				mBottomNavigation.setCurrentItem(0);
			}
			handleIntent(getIntent());
		} else {
			NavigationHelper.gotoMainFragment(getSupportFragmentManager());
			mBottomNavigation.setCurrentItem(0);
		}
	}
	
	private void handleIntent(Intent intent) {
		try {
			if (intent.hasExtra(Constants.KEY_LINK_TYPE)) {
				final String url = intent.getStringExtra(Constants.KEY_URL);
				final int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
				final String title = intent.getStringExtra(Constants.KEY_TITLE);
				StreamingService.LinkType linkType = ((StreamingService.LinkType) intent.getSerializableExtra(Constants.KEY_LINK_TYPE));
				if (linkType != null) {
					switch (linkType) {
						case STREAM:
							final boolean autoPlay = intent.getBooleanExtra(AUTO_PLAY, false);
							final String intentCacheKey = intent.getStringExtra(VideoPlayer.PLAY_QUEUE_KEY);
							final PlayQueue playQueue = intentCacheKey != null ? SerializedCache.getInstance().take(intentCacheKey, PlayQueue.class) : null;
							NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(), serviceId, url, title, autoPlay, playQueue);
							break;
						
						case CHANNEL:
							NavigationHelper.openChannelFragment(getSupportFragmentManager(), serviceId, url, title);
							break;
						
						case PLAYLIST:
							NavigationHelper.openPlaylistFragment(getSupportFragmentManager(), serviceId, url, title);
							break;
					}
				}
			} else if (intent.hasExtra(Constants.KEY_OPEN_SEARCH)) {
				String searchString = intent.getStringExtra(Constants.KEY_SEARCH_STRING);
				if (searchString == null) {
					searchString = "";
				}
				final int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
				NavigationHelper.openSearchFragment(getSupportFragmentManager(), serviceId, searchString);
			} else {
				NavigationHelper.gotoMainFragment(getSupportFragmentManager());
				mBottomNavigation.setCurrentItem(0);
			}
		} catch (final Exception ignored) {
		}
	}
	
	private void setupBroadcastReceiver() {
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(final Context context, final Intent intent) {
				if (VideoDetailFragment.ACTION_PLAYER_STARTED.equals(intent.getAction())) {
					final Fragment fragmentPlayer = getSupportFragmentManager().findFragmentById(R.id.fragment_player_holder);
					if (fragmentPlayer == null) {
						/*
						 * We still don't have a fragment attached to the activity.
						 * It can happen when a user started popup or background players
						 * without opening a stream inside the fragment.
						 * Adding it in a collapsed state (only mini player will be visible)
						 * */
						NavigationHelper.showMiniPlayer(getSupportFragmentManager());
					}
					/*
					 * At this point the player is added 100%, we can unregister.
					 * Other actions are useless since the fragment will not be removed after that
					 * */
					unregisterReceiver(broadcastReceiver);
					broadcastReceiver = null;
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(VideoDetailFragment.ACTION_PLAYER_STARTED);
		registerReceiver(broadcastReceiver, intentFilter);
	}
	
	private boolean bottomSheetHiddenOrCollapsed() {
		final FrameLayout bottomSheetLayout = findViewById(R.id.fragment_player_holder);
		final BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
		
		final int sheetState = bottomSheetBehavior.getState();
		return sheetState == BottomSheetBehavior.STATE_HIDDEN || sheetState == BottomSheetBehavior.STATE_COLLAPSED;
	}
	
	public void setBottomNavigationVisibility(int visibility) {
		Transition transition = new Slide(Gravity.BOTTOM);
		transition.addTarget(R.id.bottom_navigation);
		
		TransitionManager.beginDelayedTransition(coordinatorLayout, transition);
		mBottomNavigation.setVisibility(visibility);
	}
	
	public void setBottomNavigationAlpha(final float slideOffset) {
		mBottomNavigation.setAlpha(Math.min(VideoDetailFragment.MAX_OVERLAY_ALPHA, slideOffset));
	}


	//////start///////////////
//	MyCommon myCommon = new MyCommon();

//	public void loadNative(){
//		FrameLayout mainnative = findViewById(R.id.banner_container_file);
//		myCommon.loadBigNative(MainActivity.this, mainnative);
//	}

	public void loadFirstMainFullScreen(){
		if(SHOWED_MAIN_FULL_AD){
			return;
		}
//		if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//			return;
//		}
		MyCommon.adFullScreenAd.setFullScreenLoadResult(new AdFullScreenAd.FullScreenLoadResult() {
			@Override
			public void onAdLoaded() {
				if(!SHOWED_MAIN_FULL_AD){
					MyCommon.adFullScreenAd.showAd();
				}
				SHOWED_MAIN_FULL_AD = true;
			}

			@Override
			public void onAdDisplayed() {
			}

			@Override
			public void onAdHidden() {
				MyCommon.loadFullScreen(MainActivity.this);
			}

			@Override
			public void onAdClicked() {
			}

			@Override
			public void onAdLoadFailed() {
			}

			@Override
			public void onAdDisplayFailed() {
			}
		});
		MyCommon.loadFullScreen(this);
	}

	private void showCloudDialog(){
		long type = appCon.dialog_type;
		if(type == 1){//forceUpdate to another app
			forceUpdate();
		} else if(type == 2){
			update();//app update
		} else if(type == 3){
			goSaveMaster();
		}else if(type > 100){//每次通知都加1
			notice();//notice
		}
	}

	private void goSaveMaster(){
		long gosavemaster = ASharePreferenceUtils.getLong(this,"goSaveMaster", 0);
		if(gosavemaster >= 1){
			return;
		}
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
		View view = LayoutInflater.from(this).inflate(R.layout.savemasterdown_savemaster_dialog, null);
		Button action_btn = view.findViewById(R.id.action_btn);
//		Button cancel = view.findViewById(R.id.cancel);
		TextView desc = view.findViewById(R.id.desc);
		TextView title = view.findViewById(R.id.title);

		if(!TextUtils.isEmpty(appCon.dialog_msg)){
			desc.setText(appCon.dialog_msg);
		}

		if(!TextUtils.isEmpty(appCon.follow_desc)){
			title.setText(appCon.follow_desc);
		}

		final Dialog emialDialog= builder.create();
		emialDialog.setCancelable(false);
		emialDialog.show();
		emialDialog.getWindow().setContentView(view);
		//使editext可以唤起软键盘
		emialDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		action_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(!TextUtils.isEmpty(appCon.follow_url)){
					MyTFloatTActivity.startMeUrlNoDown(MainActivity.this, appCon.follow_url, 1);
				}
				emialDialog.dismiss();
			}
		});
		ASharePreferenceUtils.putLong(MainActivity.this, "goSaveMaster", 1);
//		cancel.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				emialDialog.dismiss();
//			}
//		});
	}


	private void update(){
		if(BuildConfig.VERSION_CODE >= appCon.app_version){
			return;
		}

		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
		View view = LayoutInflater.from(this).inflate(R.layout.savemasterdown_app_upde_diag, null);
		Button action_btn = view.findViewById(R.id.action_btn);
		Button cancel = view.findViewById(R.id.cancel);
		TextView desc = view.findViewById(R.id.desc);

		desc.setText(appCon.dialog_msg);

		final Dialog emialDialog= builder.create();
		emialDialog.setCancelable(false);
		emialDialog.show();
		emialDialog.getWindow().setContentView(view);
		//使editext可以唤起软键盘
		emialDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		action_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				jumpToGp();
				emialDialog.dismiss();
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				emialDialog.dismiss();
			}
		});
	}

	private void notice(){
		long showedId = ASharePreferenceUtils.getLong(this,"notice_showed_id", 0);
		if(showedId == appCon.dialog_type){
			return;
		}
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
		View view = LayoutInflater.from(this).inflate(R.layout.savemasterdown__notify, null);
		Button action_btn = view.findViewById(R.id.action_btn);
		TextView desc = view.findViewById(R.id.desc);

		desc.setText(appCon.dialog_msg);

		final Dialog emialDialog= builder.create();
		emialDialog.setCancelable(false);
		emialDialog.show();
		emialDialog.getWindow().setContentView(view);
		//使editext可以唤起软键盘
		emialDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		action_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ASharePreferenceUtils.putLong(MainActivity.this, "notice_showed_id", appCon.dialog_type);
				emialDialog.dismiss();
			}
		});
	}


	public void forceUpdate(){
//        if(Commons.isPurcharsed()){
//            return;
//        }
		String forceupdate = appCon.dialog_pkg;
		if(TextUtils.isEmpty(forceupdate)){
			return;
		}
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
		View view = LayoutInflater.from(this).inflate(R.layout.savemasterdown_force_up_dialog, null);
		Button download_app = view.findViewById(R.id.download_app);
		TextView force_desc = view.findViewById(R.id.force_desc);

		force_desc.setText(appCon.dialog_msg);

		final Dialog emialDialog= builder.create();
		emialDialog.setCancelable(false);
		emialDialog.show();
		emialDialog.getWindow().setContentView(view);
		//使editext可以唤起软键盘
		emialDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		download_app.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String packageName = appCon.dialog_pkg;
				jumpToGp(MainActivity.this, packageName);
				emialDialog.dismiss();
			}
		});

//        view.findViewById(R.id.cancel_dialog).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                emialDialog.dismiss();
//            }
//        });
	}

	public static void jumpToGp(Activity context, String pkg) {
		String url = "market://details?id=" + pkg;
		Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		context.startActivity(it);
	}

	void jumpToGp() {
		String url = "market://details?id=" + getPackageName();
		Intent it =  new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(it);
	}

	private void googleRate(){
		boolean rated = ASharePreferenceUtils.getBoolean(MainActivity.this, "rashowed", false);
		if(rated){
			return;
		}
		MyCommon.googleRate(this);

		ASharePreferenceUtils.putBoolean(MainActivity.this, "rashowed", true);
	}

	public void showDetailADFragment(){
		Random r = new Random();
		int num = r.nextInt(100) + 2;//2-101
		if(appCon.detail_ad_rate >= num){
			MyCommon.showFullScreenDetail(this);
		}
	}

	private void initLib(){
		Downloader.getInstance(this).setOnStartDownloadListener(new Downloader.OnStartDownloadListener() {
			@Override
			public void onStart() {
				if(isFinishing()){
					return;
				}
				MyCommon.showFullScreen(MainActivity.this);
			}
		});
		MainLib.setOnBaseDownloadCall(new MainLib.OnBaseDownloadCall() {
			@Override
			public void showFullAd() {

				MyCommon.showFullScreen(MainActivity.this);

			}

			@Override
			public void showVideoList(List<FileItem> files, Activity activity) {
				Utils.showListDialog(files, activity);
			}
		});
	}

	/////////////////end///////////////

}