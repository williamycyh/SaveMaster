package com.savemaster.savefromfb.util;

import android.content.Context;
import android.content.Intent;

import com.savemaster.savefromfb.R;
import com.savemaster.savefromfb.util.dialog.DialogUtils;

public class SharedUtils {
	
	public static void shareUrl(Context context) {
		
		String sharedText = context.getString(R.string.app_name) + " is an application allow you watch YouTube videos on Floating Window mode!\n\n" + "Customize it now!\nhttps://codecanyon.net/item/protube-android-floating-tube-player-for-youtube/25787951";
		
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "ProTube - Watch YouTube video on Floating mode");
		intent.putExtra(Intent.EXTRA_TEXT, sharedText);
		context.startActivity(Intent.createChooser(intent, context.getString(R.string.savemasterdown_share_dialog_title)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
	
	public static void rateApp(Context context) {
		DialogUtils.showEnjoyAppDialog(context,
									   // positive
									   (dialog, which) -> {
										   // dismiss dialog
										   dialog.dismiss();
										   // show dialog ask to rate
										   DialogUtils.showAskRatingAppDialog(context,
																			  // positive
																			  (dialog1, which1) -> {
																				  // open play store
																				  NavigationHelper.openGooglePlayStore(context, context.getPackageName());
																				  // dismiss dialog
																				  dialog1.dismiss();
																			  },
																			  // negative
																			  (dialog1, which1) -> {
																				  // dismiss dialog
																				  dialog1.dismiss();
																			  });
									   },
									   // negative
									   (dialog, which) -> {
										   // dismiss dialog
										   dialog.dismiss();
										   // show dialog feedback
										   DialogUtils.showFeedBackDialog(context,
																		  // positive
																		  (dialog2, which2) -> {
																			  // open email app
																			  NavigationHelper.composeEmail(context, context.getString(R.string.app_name) + " Android Feedback");
																			  // dismiss dialog
																			  dialog2.dismiss();
																		  },
																		  // negative
																		  (dialog2, which2) -> {
																			  // dismiss dialog
																			  dialog2.dismiss();
																		  });
									   });
	}
}