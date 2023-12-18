package com.savemaster.savefromfb;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.RequestConfiguration;

import com.savemaster.savefromfb.uiact.UIReCaptchaActivity;
import com.savemaster.download.DownloaderImpl;
import com.savemaster.moton.AppCon;
import com.savemaster.notification.NotificationOreo;

import com.google.firebase.FirebaseApp;
//import com.liulishuo.filedownloader.FileDownloader;
//import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import savemaster.save.master.pipd.NewPipe;
import savemaster.save.master.pipd.downloader.Downloader;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import com.savemaster.savefromfb.util.ExtractorHelper;
import com.savemaster.savefromfb.util.Localization;
import com.savemaster.savefromfb.util.StateSaver;
import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.MissingBackpressureException;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

@SuppressLint("Registered")
public class App extends MultiDexApplication {

	public static AppCon appCon = new AppCon();

	public static Context applicationContext;

	public static Context getAppContext() {
		return applicationContext;
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		applicationContext = this;
		
		// initialize settings first because others inits can use its values
		GAGTubeSettings.initSettings(this);
		
		// initialize localization
		NewPipe.init(getDownloader(),
					 Localization.getPreferredLocalization(this),
					 Localization.getPreferredContentCountry(this));
		Localization.init();
		StateSaver.init(this);
		
		// image loader
		ImageLoader.getInstance().init(getImageLoaderConfigurations());
		
		// initialize notification channels for android-o
		NotificationOreo.init(this);
		initNotificationChannel();
		
		// initialize firebase
		FirebaseApp.initializeApp(this);
		
		configureRxJavaErrorHandler();

		wuyong();

		
//		// AdMob
//		MobileAds.initialize(this, initializationStatus -> {});
//
//		// Test device
//		RequestConfiguration builder = new RequestConfiguration.Builder().setTestDeviceIds(Collections.singletonList("92094EE8DBF3A70B378025A71ED90503")).build();
//		MobileAds.setRequestConfiguration(builder);
	}

	private void wuyong(){
//		try{
//			FileDownloader.setupOnApplicationOnCreate(this)
//					.connectionCreator(new FileDownloadUrlConnection
//							.Creator(new FileDownloadUrlConnection.Configuration()
//							.connectTimeout(15_000) // set connection timeout.
//							.readTimeout(15_000) // set read timeout.
//					))
//					.commit();
//		}catch (Exception e){
//		}
	}
	
	protected Downloader getDownloader() {
		DownloaderImpl downloader = DownloaderImpl.init(null);
		setCookiesToDownloader(downloader);
		return downloader;
	}
	
	protected void setCookiesToDownloader(final DownloaderImpl downloader) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		final String key = getApplicationContext().getString(R.string.recaptcha_cookies_key);
		downloader.setCookie(UIReCaptchaActivity.RECAPTCHA_COOKIES_KEY, prefs.getString(key, ""));
		downloader.updateTubeRestrictedModeCookies(getApplicationContext());
	}
	
	private void configureRxJavaErrorHandler() {
		
		// https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
		RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
			
			@Override
			public void accept(@NonNull Throwable throwable) {
				
				if (throwable instanceof UndeliverableException) {
					// As UndeliverableException is a wrapper, get the cause of it to get the "real" exception
					throwable = throwable.getCause();
				}
				
				final List<Throwable> errors;
				if (throwable instanceof CompositeException) {
					errors = ((CompositeException) throwable).getExceptions();
				}
				else {
					errors = Collections.singletonList(throwable);
				}
				
				for (final Throwable error : errors) {
					if (isThrowableIgnored(error)) return;
					if (isThrowableCritical(error)) {
						reportException(error);
						return;
					}
				}
			}
			
			private boolean isThrowableIgnored(@NonNull final Throwable throwable) {
				
				// Don't crash the application over a simple network problem
				return ExtractorHelper.hasAssignableCauseThrowable(throwable, IOException.class, SocketException.class, // network api cancellation
																   InterruptedException.class, InterruptedIOException.class); // blocking code disposed
			}
			
			private boolean isThrowableCritical(@NonNull final Throwable throwable) {
				
				// Though these exceptions cannot be ignored
				return ExtractorHelper.hasAssignableCauseThrowable(throwable,
																   NullPointerException.class, IllegalArgumentException.class, // bug in app
																   OnErrorNotImplementedException.class, MissingBackpressureException.class,
																   IllegalStateException.class); // bug in operator
			}
			
			private void reportException(@NonNull final Throwable throwable) {
				
				// Throw uncaught exception that will trigger the report system
				Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), throwable);
			}
		});
	}
	
	private ImageLoaderConfiguration getImageLoaderConfigurations() {
		
		return new ImageLoaderConfiguration.Builder(this)
				.memoryCache(new LRULimitedMemoryCache(100 * 1024 * 1024))
				.diskCacheSize(500 * 1024 * 1024)
				.build();
	}
	
	public void initNotificationChannel() {
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			return;
		}
		
		final String id = getString(R.string.savemasterdown_notification_channel_id);
		final CharSequence name = getString(R.string.savemasterdown_notification_channel_name);
		final String description = getString(R.string.savemasterdown_notification_channel_description);
		
		// Keep this below DEFAULT to avoid making noise on every notification update
		final int importance = NotificationManager.IMPORTANCE_LOW;
		
		NotificationChannel mChannel = new NotificationChannel(id, name, importance);
		mChannel.setDescription(description);
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (mNotificationManager != null) {
			mNotificationManager.createNotificationChannel(mChannel);
		}
	}
}
