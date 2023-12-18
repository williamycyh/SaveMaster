package com.savemaster.savefromfb.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.savemaster.savefromfb.R;

public class GlideUtils {
	
	public static void loadAvatar(Context context, ImageView imageView, String imageUrl) {
		Glide.with(context).load(imageUrl)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.savemasterdown_u_default)
				.error(R.drawable.savemasterdown_u_default)
				.fallback(R.drawable.savemasterdown_u_default)
				.into(imageView);
	}
	
	public static void loadThumbnail(Context context, ImageView imageView, String imageUrl) {
		Glide.with(context).load(imageUrl)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.savemasterdown_n_img)
				.error(R.drawable.savemasterdown_n_img)
				.fallback(R.drawable.savemasterdown_n_img)
				.into(imageView);
	}
	
	public static void loadChannelBanner(Context context, ImageView imageView, String imageUrl) {
		Glide.with(context).load(imageUrl)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.savemasterdown_achnel_banner)
				.error(R.drawable.savemasterdown_achnel_banner)
				.fallback(R.drawable.savemasterdown_achnel_banner)
				.into(imageView);
	}
}
