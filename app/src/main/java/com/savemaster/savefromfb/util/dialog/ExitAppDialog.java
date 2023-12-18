package com.savemaster.savefromfb.util.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdLoader;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.VideoOptions;
//import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.savemaster.savefromfb.ads.nativead.AppNativeAdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.util.NavigationHelper;

public final class ExitAppDialog extends DialogFragment {
	
	@BindView(R.id.template_view)
    AppNativeAdView nativeAdView;
	
	private Activity activity;
	private Runnable callback;
	
	public ExitAppDialog(Runnable callback) {
		this.callback = callback;
	}
	
	public static ExitAppDialog newInstance(Runnable callback) {
		return new ExitAppDialog(callback);
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = getActivity();
	}
	
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		
		View dialogView = View.inflate(activity, R.layout.savemasterdown_dialog_exit_app, null);
		ButterKnife.bind(this, dialogView);
		
		// show ad
		showNativeAd();
		
		@SuppressLint("CheckResult") final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(activity)
				.setTitle(R.string.savemasterdown_dialog_exit_app_msg)
				.setView(dialogView)
				.setCancelable(true)
				.setNeutralButton(R.string.savemasterdown_setting_rate_me_now, (dialog, which) -> NavigationHelper.openGooglePlayStore(activity, activity.getPackageName()))
				.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
				.setPositiveButton(R.string.savemasterdown_yes, (dialog, which) -> callback.run());
		
		return dialogBuilder.create();
	}
	
	private void showNativeAd() {
		
		// ad options
//		VideoOptions videoOptions = new VideoOptions.Builder()
//				.setStartMuted(true)
//				.build();
//
//		NativeAdOptions adOptions = new NativeAdOptions.Builder()
//				.setVideoOptions(videoOptions)
//				.build();
//
//		AdLoader adLoader = new AdLoader.Builder(activity, AdUtils.getNativeAdId(activity))
//				.forNativeAd(nativeAd -> {
//
//					// show the ad
//					NativeAdStyle styles = new NativeAdStyle.Builder().build();
//					nativeAdView.setStyles(styles);
//					nativeAdView.setNativeAd(nativeAd);
//				})
//				.withAdListener(new AdListener() {
//
//					@Override
//					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//						super.onAdFailedToLoad(loadAdError);
//						// gone
//						nativeAdView.setVisibility(View.GONE);
//					}
//
//					@Override
//					public void onAdLoaded() {
//						super.onAdLoaded();
//						// visible
//						nativeAdView.setVisibility(View.VISIBLE);
//					}
//				})
//				.withNativeAdOptions(adOptions)
//				.build();
//
//		// loadAd
//		AdRequest.Builder builder = new AdRequest.Builder();
//		adLoader.loadAd(builder.build());
	}
}
