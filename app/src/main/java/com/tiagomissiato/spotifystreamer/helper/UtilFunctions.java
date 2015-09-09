package com.tiagomissiato.spotifystreamer.helper;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.graphics.Bitmap;

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

		for(Image img : images){
			// put add and sub 5 to compare because in the URL there was some width 199, soh just in case;
			if(img.width >= 195 && img.width <= 305)
				correctImage = img.url;

		}

		return correctImage;
	}

	public static String getBigImageUrl(List<Image> images){
		String correctImage = "";

		for(Image img : images){
			// put add and sub 5 to compare because in the URL there was some width 199, soh just in case;
			if(img.width >= 630 && img.width <= 650)
				correctImage = img.url;

		}

		return correctImage;
	}

	public static Bitmap getSmallImage(Context context, Track track){

		Bitmap theBitmap = null;
//		try {
//			theBitmap = Glide.
//                    with(context).
//                    load(getSmallImageUrl(track.album.images)).
//                    asBitmap().
//                    into(200, 200).
//					get();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}

		return theBitmap;
	}
}
