package com.savemaster.savefromfb.uiact;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.savemaster.savefromfb.R;
import com.savemaster.savefromfb.util.ThemeHelper;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setStatusBarGradient(this);
	}
	
	public static void setStatusBarGradient(Activity activity) {
		
		Window window = activity.getWindow();
		window.setStatusBarColor(ThemeHelper.isLightThemeSelected(activity) ? ContextCompat.getColor(activity, R.color.savemasterdown_light_status_bar_color) : ContextCompat.getColor(activity, R.color.savemasterdown_dark_status_bar_color));
	}
}
