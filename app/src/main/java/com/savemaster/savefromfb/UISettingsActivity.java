package com.savemaster.savefromfb;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import butterknife.ButterKnife;
import com.savemaster.savefromfb.uiact.BaseActivity;
import com.savemaster.savefromfb.util.ThemeHelper;

public class UISettingsActivity extends BaseActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
	
	@Override
	protected void onCreate(Bundle savedInstanceBundle) {
		
		setTheme(ThemeHelper.getSettingsThemeStyle(this));
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.savemasterdown_activity_settings);
		ButterKnife.bind(this);
		
		Toolbar toolbar = findViewById(R.id.default_toolbar);
		setSupportActionBar(toolbar);
		
		if (savedInstanceBundle == null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, new MainSettingsFragment()).commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setTitle(R.string.savemasterdown_settings);
		}
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == android.R.id.home) {
			
			// end here
			finish();
		}
		return true;
	}
	
	@Override
	public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference preference) {
		
		Fragment fragment = Fragment.instantiate(this, preference.getFragment(), preference.getExtras());
		getSupportFragmentManager().beginTransaction().setCustomAnimations(R.animator.savemasterdown_custom_fade_in, R.animator.savemasterdown_custom_fade_out, R.animator.savemasterdown_custom_fade_in, R.animator.savemasterdown_custom_fade_out)
				.replace(R.id.fragment_holder, fragment)
				.addToBackStack(null)
				.commit();
		return true;
	}
}
