package com.savemaster.savefromfb.uifra.discover;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.savemaster.savefromfb.R;
import com.savemaster.moton.Utils;
import com.savemaster.savefromfb.uiact.BaseFragment;
import com.savemaster.savefromfb.uifra.discover.adapter.TopViewPagerAdapter;
import com.savemaster.savefromfb.util.NavigationHelper;
import com.savemaster.savefromfb.util.ServiceHelper;
import com.savemaster.smlib.ASharePreferenceUtils;
import com.savemaster.smlib.BaseCommon;

public class DiscoverFragment extends BaseFragment {
	
	@BindView(R.id.tab_layout) TabLayout tabLayout;
	@BindView(R.id.view_pager) ViewPager viewPager;
	@BindView(R.id.fab_play) ExtendedFloatingActionButton fab;
	private TopViewPagerAdapter adapter;
	
	public static DiscoverFragment getInstance() {
		return new DiscoverFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TopViewPagerAdapter
		adapter = new TopViewPagerAdapter(getChildFragmentManager(), activity);
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.savemasterdown_fragment_discover, container, false);
		ButterKnife.bind(this, view);

		showIntroduce();
		return view;
	}
	
	@Override
	protected void initViews(View rootView, Bundle savedInstanceState) {
		super.initViews(rootView, savedInstanceState);
		initAdapter();
	}
	
	private void initAdapter() {
		// set adapter to viewPager
		viewPager.setAdapter(adapter);
		// setup tabLayout with viewPager
		tabLayout.setupWithViewPager(viewPager);
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

	@OnClick(R.id.fab_play)
	void playAll() {
		TopFragment fragment = (TopFragment) adapter.instantiateItem(viewPager, viewPager.getCurrentItem());
		fragment.playAll();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
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
