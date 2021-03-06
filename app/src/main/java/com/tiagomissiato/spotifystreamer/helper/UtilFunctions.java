package com.tiagomissiato.spotifystreamer.helper;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.tiagomissiato.spotifystreamer.model.Artist;
import com.tiagomissiato.spotifystreamer.model.Image;
import com.tiagomissiato.spotifystreamer.model.Track;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class UtilFunctions {
	static String LOG_CLASS = "UtilFunctions";

	/**
	 * Check if service is running or not
	 * @param serviceName
	 * @param context
	 * @return
	 */
	public static boolean isServiceRunning(String serviceName, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if(serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert milliseconds into time hh:mm:ss
	 * @param milliseconds
	 * @return time in String
	 */
	public static String getDuration(long milliseconds) {
		long sec = (milliseconds / 1000) % 60;
		long min = (milliseconds / (60 * 1000))%60;
		long hour = milliseconds / (60 * 60 * 1000);

		String s = (sec < 10) ? "0" + sec : "" + sec;
		String m = (min < 10) ? "0" + min : "" + min;
		String h = "" + hour;
		
		String time = "";
		if(hour > 0) {
			time = h + ":" + m + ":" + s;
		} else {
			time = m + ":" + s;
		}
		return time;
	}
	
	public static boolean currentVersionSupportBigNotification() {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if(sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN){
			return true;
		}
		return false;
	}
	
	public static boolean currentVersionSupportLockScreenControls() {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if(sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			return true;
		}
		return false;
	}

	public static String getSmallImageUrl(List<Image> images){
		String correctImage = "";

		correctImage = images.get(0).url;
		for(Image img : images){
			// put add and sub 5 to compare because in the URL there was some width 199, soh just in case;
			if(img.width >= 195 && img.width <= 305)
				correctImage = img.url;

		}

		return correctImage;
	}

	public static String getBigImageUrl(List<Image> images){
		String correctImage = "";

		correctImage = images.get(0).url;
		for(Image img : images){
			// put add and sub 5 to compare because in the URL there was some width 199, soh just in case;
			if(img.width >= 630 && img.width <= 650)
				correctImage = img.url;

		}

		return correctImage;
	}

	public static void showKeyboard(Context context) {
		InputMethodManager manager = (InputMethodManager)context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public static void hideKeyboard(Context context, View view) {
		if (view != null) {
			InputMethodManager manager = (InputMethodManager)context
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
}
