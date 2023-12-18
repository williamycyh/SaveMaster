package com.savemaster.savefromfb.uifra.list.main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdLoader;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.VideoOptions;
//import com.google.android.gms.ads.nativead.NativeAdOptions;

import org.jetbrains.annotations.NotNull;
import savemaster.save.master.pipd.ListExtractor;
import savemaster.save.master.pipd.NewPipe;
import savemaster.save.master.pipd.StreamingService;
import savemaster.save.master.pipd.exceptions.ExtractionException;
import savemaster.save.master.pipd.kis.KioskInfo;
import savemaster.save.master.pipd.linkhandler.ListLinkHandlerFactory;
import com.savemaster.smlib.ASharePreferenceUtils;
import com.savemaster.smlib.BaseCommon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.savemaster.moton.Utils;
import com.savemaster.savefromfb.uifra.list.BaseListInfoFragment;
import com.savemaster.moton.MyCommon;
import com.savemaster.savefromfb.util.UserAction;

import butterknife.ButterKnife;
import butterknife.OnClick;
import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.util.AnimationUtils;
import com.savemaster.savefromfb.util.ExtractorHelper;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.ServiceHelper;
import io.reactivex.Single;

public class TrendingFragment extends BaseListInfoFragment<KioskInfo> {
	
	// NativeAd
	private FrameLayout nativeAdView;
	
	@NonNull
	public static TrendingFragment getInstance(int serviceId) {
		
		try {
			return getInstance(serviceId, NewPipe.getService(serviceId).getKioskList().getDefaultKioskId());
		}
		catch (ExtractionException e) {
			return new TrendingFragment();
		}
	}
	
	@NonNull
	public static TrendingFragment getInstance(int serviceId, String kioskId) {
		
		try {
			TrendingFragment instance = new TrendingFragment();
			StreamingService service = NewPipe.getService(serviceId);
			
			ListLinkHandlerFactory kioskLinkHandlerFactory = service.getKioskList().getListLinkHandlerFactoryByType(kioskId);
			instance.setInitialData(serviceId, kioskLinkHandlerFactory.fromId(kioskId).getUrl(), kioskId);
			
			return instance;
		}
		catch (ExtractionException e) {
			return new TrendingFragment();
		}
	}
	
	// LifeCycle
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		name = getString(R.string.savemasterdown_trending);
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.savemasterdown_fragment_trending, container, false);
		ButterKnife.bind(this, view);

		showIntroduce();
		return view;
	}
	
	@Override
	protected void initViews(View rootView, Bundle savedInstanceState) {
		
		super.initViews(rootView, savedInstanceState);
		
		View headerRootLayout = activity.getLayoutInflater().inflate(R.layout.savemasterdown_native_ad_list_header, itemsList, false);
		nativeAdView = headerRootLayout.findViewById(R.id.template_view);
		infoListAdapter.setHeader(headerRootLayout);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// show ad
		showNativeAd();
	}
	
	// Menu
	@Override
	public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
		
		super.onCreateOptionsMenu(menu, inflater);
		
		ActionBar supportActionBar = activity.getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(false);
		}
	}
	
	// Load and handle
	@Override
	public Single<KioskInfo> loadResult(boolean forceReload) {
		
		return ExtractorHelper.getKioskInfo(serviceId, url, forceReload);
	}
	
	@Override
	public Single<ListExtractor.InfoItemsPage> loadMoreItemsLogic() {
		
		return ExtractorHelper.getMoreKioskItems(serviceId, url, currentNextPage);
	}
	
	// Contract
	@Override
	public void showLoading() {
		
		super.showLoading();
		
		AnimationUtils.animateView(itemsList, false, 100);
	}
	
	@Override
	public void handleResult(@NonNull final KioskInfo result) {
		
		super.handleResult(result);
		
		if (!result.getErrors().isEmpty()) {
			showSnackBarError(result.getErrors(), UserAction.REQUESTED_MAIN_CONTENT, NewPipe.getNameOfService(result.getServiceId()), result.getUrl(), 0);
		}
	}
	
	@Override
	public void handleNextItems(ListExtractor.InfoItemsPage result) {
		
		super.handleNextItems(result);
		
		if (!result.getErrors().isEmpty()) {
			showSnackBarError(result.getErrors(), UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(serviceId), "Get next page of: " + url, 0);
		}
	}

	MyCommon myCommon = new MyCommon();
	private void showNativeAd() {
		if(getActivity() == null || getActivity().isFinishing()){
			return;
		}
		myCommon.loadBigNative(getActivity(), nativeAdView);
		// ad options
	}
	
	@OnClick(R.id.action_search)
	void onSearch() {
		// open SearchFragment
		NavigationHelper.openSearchFragment(getFM(), ServiceHelper.getSelectedServiceId(activity), "");
	}
	
	@OnClick(R.id.action_settings)
	void onSettings() {
		// open Settings
		NavigationHelper.openSettings(activity);
	}

	@OnClick(R.id.action_link)
	void onLink(){
		Utils.showLinkDialog(getActivity(), getFM());
	}


	@Override
	public void onDestroy() {
		
		// destroy ad
//		if (nativeAdView != null) {
//			nativeAdView.destroyNativeAd();
//		}
		
		super.onDestroy();
	}

	/**
	 * 简介
	 */
	private void showIntroduce(){
		try {
			boolean showed = ASharePreferenceUtils.getBoolean(getActivity(),"introduce_showed", false);
			if(showed){
				return;
			}
			android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
			View view = LayoutInflater.from(getActivity()).inflate(R.layout.main_savemasterdown__jieshaoye, null);
			Button action_btn = view.findViewById(R.id.action_btn);
			TextView desc = view.findViewById(R.id.desc);

			desc.setText(BaseCommon.decodeToString("MS4gUGFzdGUgWW91dHViZS9GYWNlYm9vayBMaW5rIHRvIGRvd25sb2FkCjIuIFdhdGNoICYgRG93bmxvYWQgYWxsIFlvdXR1YmUgdmlkZW8gJiBtdXNpYwozLiBTZWFyY2ggWW91dHViZSB2aWRlbyAmIG11c2ljCg=="));

			final Dialog emialDialog= builder.create();
			emialDialog.setCancelable(false);
			emialDialog.show();
			emialDialog.getWindow().setContentView(view);
			//使editext可以唤起软键盘
			emialDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			action_btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ASharePreferenceUtils.putBoolean(getActivity(), "introduce_showed", true);
//					NavigationHelper.openSearchFragment(getFM(), ServiceHelper.getSelectedServiceId(activity), "");
					emialDialog.dismiss();
				}
			});
		}catch (Exception e){

		}
	}


}
