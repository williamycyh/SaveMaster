
package com.savemaster.savefromfb.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import savemaster.save.master.pipd.NewPipe;
import savemaster.save.master.pipd.StreamingService;
import savemaster.save.master.pipd.exceptions.ExtractionException;

import androidx.annotation.AttrRes;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import com.savemaster.savefromfb.R;

public class ThemeHelper {
	
	/**
	 * Apply the selected theme (on NewPipe settings) in the context
	 * with the default style (see {@link #setTheme(Context, int)}).
	 *
	 * @param context context that the theme will be applied
	 */
	public static void setTheme(Context context) {
		setTheme(context, -1);
	}
	
	/**
	 * Apply the selected theme (on NewPipe settings) in the context,
	 * themed according with the styles defined for the service .
	 *
	 * @param context   context that the theme will be applied
	 * @param serviceId the theme will be styled to the service with this id,
	 *                  pass -1 to get the default style
	 */
	public static void setTheme(Context context, int serviceId) {
		context.setTheme(getThemeForService(context, serviceId));
	}
	
	/**
	 * Return true if the selected theme (on NewPipe settings) is the Light theme
	 *
	 * @param context context to get the preference
	 */
	public static boolean isLightThemeSelected(Context context) {
		return getSelectedThemeString(context).equals(context.getResources().getString(R.string.light_theme_key));
	}
	
	/**
	 * Create and return a wrapped context with the default selected theme set.
	 *
	 * @param baseContext the base context for the wrapper
	 * @return a wrapped-styled context
	 */
	public static Context getThemedContext(Context baseContext) {
		return new ContextThemeWrapper(baseContext, getThemeForService(baseContext, -1));
	}
	
	/**
	 * Return the selected theme without being styled to any service (see {@link #getThemeForService(Context, int)}).
	 *
	 * @param context context to get the selected theme
	 * @return the selected style (the default one)
	 */
	@StyleRes
	public static int getDefaultTheme(Context context) {
		return getThemeForService(context, -1);
	}
	
	/**
	 * Return a dialog theme styled according to the (default) selected theme.
	 *
	 * @param context context to get the selected theme
	 * @return the dialog style (the default one)
	 */
	@StyleRes
	public static int getDialogTheme(Context context) {
		return isLightThemeSelected(context) ? R.style.LightDialogTheme : R.style.DarkDialogTheme;
	}
	
	@StyleRes
	public static int getBottomSheetDialogThem(Context context) {
		return isLightThemeSelected(context) ? R.style.LightBottomSheetDialog : R.style.DarkBottomSheetDialog;
	}
	
	/**
	 * Return the selected theme styled according to the serviceId.
	 *
	 * @param context   context to get the selected theme
	 * @param serviceId return a theme styled to this service,
	 *                  -1 to get the default
	 * @return the selected style (styled)
	 */
	@StyleRes
	public static int getThemeForService(Context context, int serviceId) {
		
		String lightTheme = context.getResources().getString(R.string.light_theme_key);
		String darkTheme = context.getResources().getString(R.string.dark_theme_key);
		
		String selectedTheme = getSelectedThemeString(context);
		
		int defaultTheme = R.style.LightTheme;
		if (selectedTheme.equals(lightTheme)) defaultTheme = R.style.LightTheme;
		else if (selectedTheme.equals(darkTheme)) defaultTheme = R.style.DarkTheme;
		
		if (serviceId <= -1) {
			return defaultTheme;
		}
		
		final StreamingService service;
		try {
			service = NewPipe.getService(serviceId);
		}
		catch (ExtractionException ignored) {
			return defaultTheme;
		}
		
		String themeName = "DarkTheme";
		if (selectedTheme.equals(lightTheme)) themeName = "LightTheme";
		else if (selectedTheme.equals(darkTheme)) themeName = "DarkTheme";
		
		themeName += "." + service.getServiceInfo().getName();
		int resourceId = context.getResources().getIdentifier(themeName, "style", context.getPackageName());
		
		if (resourceId > 0) {
			return resourceId;
		}
		
		return defaultTheme;
	}
	
	@StyleRes
	public static int getSettingsThemeStyle(Context context) {
		
		String lightTheme = context.getResources().getString(R.string.light_theme_key);
		String darkTheme = context.getResources().getString(R.string.dark_theme_key);
		
		String selectedTheme = getSelectedThemeString(context);
		
		if (selectedTheme.equals(lightTheme)) return R.style.LightSettingsTheme;
		else if (selectedTheme.equals(darkTheme)) return R.style.DarkSettingsTheme;
		else return R.style.DarkSettingsTheme;
	}
	
	/**
	 * Get a resource id from a resource styled according to the the context's theme.
	 */
	public static int resolveResourceIdFromAttr(Context context, @AttrRes int attr) {
		
		TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
		int attributeResourceId = a.getResourceId(0, 0);
		a.recycle();
		return attributeResourceId;
	}
	
	/**
	 * Get a color from an attr styled according to the the context's theme.
	 */
	public static int resolveColorFromAttr(Context context, @AttrRes int attrColor) {
		
		final TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(attrColor, value, true);
		
		if (value.resourceId != 0) {
			return ContextCompat.getColor(context, value.resourceId);
		}
		
		return value.data;
	}
	
	private static String getSelectedThemeString(Context context) {
		
		String themeKey = context.getString(R.string.theme_key);
		String defaultTheme = context.getResources().getString(R.string.default_theme_value);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(themeKey, defaultTheme);
	}
	
	/**
	 * This will get the R.drawable.* resource to which attr is currently pointing to.
	 *
	 * @param attr    a R.attribute.* resource value
	 * @param context the context to use
	 * @return a R.drawable.* resource value
	 */
	public static int getIconByAttr(final int attr, final Context context) {
		
		return context.obtainStyledAttributes(new int[]{attr}).getResourceId(0, -1);
	}
}
