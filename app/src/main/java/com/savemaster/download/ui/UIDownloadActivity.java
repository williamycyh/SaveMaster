package com.savemaster.download.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import com.savemaster.savefromfb.R;
import com.savemaster.savefromfb.uiact.BaseActivity;
import com.savemaster.download.service.DownloadManagerService;
import com.savemaster.savefromfb.util.ThemeHelper;

public class UIDownloadActivity extends BaseActivity {
	
	private static final String MISSIONS_FRAGMENT_TAG = "fragment_tag";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Service
		Intent i = new Intent();
		i.setClass(this, DownloadManagerService.class);
		startService(i);
		
		ThemeHelper.setTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.savemasterdown_activity_downloader);
		
		Toolbar toolbar = findViewById(R.id.default_toolbar);
		setSupportActionBar(toolbar);
		
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(R.string.download);
			actionBar.setDisplayShowTitleEnabled(true);
		}
		
		getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				updateFragments();
				getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
	}
	
	private void updateFragments() {
		MissionsFragment fragment = new MissionsFragment();
		
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_layout, fragment, MISSIONS_FRAGMENT_TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.savemasterdown_menu_download, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
