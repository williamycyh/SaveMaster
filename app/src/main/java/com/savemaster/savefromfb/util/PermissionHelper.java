package com.savemaster.savefromfb.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.savemaster.savefromfb.R;

public class PermissionHelper {
	
	/**
	 * In order to be able to draw over other apps, the permission android.permission.SYSTEM_ALERT_WINDOW have to be granted.
	 * <p>
	 * On < API 23 (MarshMallow) the permission was granted when the user installed the application (via AndroidManifest),
	 * on > 23, however, it have to start a activity asking the user if he agrees.
	 * <p>
	 * This method just return if the app has permission to draw over other apps, and if it doesn't, it will try to get the permission.
	 *
	 * @return returns {@link Settings#canDrawOverlays(Context)}
	 **/
	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean checkCanDrawOverlays(Context context) {
		if (!Settings.canDrawOverlays(context)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			return false;
		}
		return true;
	}
	
	public static boolean isPopupEnabled(Context context) {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PermissionHelper.checkCanDrawOverlays(context);
	}
	
	public static void showPopupEnableToast(Context context) {
		try{
			Toast toast = Toast.makeText(context, R.string.savemasterdown_msg_popup_permission, Toast.LENGTH_LONG);
//			TextView messageView = toast.getView().findViewById(android.R.id.message);
//
//			if (messageView != null) messageView.setGravity(Gravity.CENTER);
			toast.show();
		}catch (Exception e){
		}

	}
	
	public static final int DOWNLOAD_DIALOG_REQUEST_CODE = 9001;
	public static final int DOWNLOADS_REQUEST_CODE = 9002;
	
	public static boolean checkStoragePermissions(Activity activity, int requestCode) {
		
		if (!checkReadStoragePermissions(activity, requestCode)) return false;
		return checkWriteStoragePermissions(activity, requestCode);
	}
	
	public static boolean checkReadStoragePermissions(Activity activity, int requestCode) {
		
		if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			
			ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
			
			return false;
		}
		return true;
	}
	
	public static boolean checkWriteStoragePermissions(Activity activity, int requestCode) {
		
		// Here, thisActivity is the current activity
		if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			
			// Should we show an explanation?
            /*if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {*/
			
			// No explanation needed, we can request the permission.
			ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
			
			// PERMISSION_WRITE_STORAGE is an
			// app-defined int constant. The callback method gets the
			// result of the request.
			/*}*/
			return false;
		}
		return true;
	}
}
