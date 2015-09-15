package com.tiagomissiato.spotifystreamer.helper;

import android.content.Context;

import com.tiagomissiato.spotifystreamer.R;
import com.tiagomissiato.spotifystreamer.model.Track;
import com.tiagomissiato.spotifystreamer.service.SongService;

public class Controls {
	static String LOG_CLASS = "Controls";
	public static void playControl(Context context) {
		sendMessage(context.getResources().getString(R.string.play));
	}

	public static void pauseControl(Context context) {
		sendMessage(context.getResources().getString(R.string.pause));
	}

	public static void nextControl(Context context) {
		boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), context);
		if (!isServiceRunning)
			return;
		PlayerConstants.SONG_NUMBER = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER).next.pos;
		PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
		PlayerConstants.SONG_PAUSED = false;
	}

	public static void previousControl(Context context) {
		boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), context);
		if (!isServiceRunning)
			return;

		PlayerConstants.SONG_NUMBER = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER).prev.pos;
		PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
		PlayerConstants.SONG_PAUSED = false;
	}

	public static void playSong(Context context, Track track) {
		boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), context);
		if (!isServiceRunning)
			return;

		PlayerConstants.SONG_NUMBER = track.pos;
		PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
		PlayerConstants.SONG_PAUSED = false;
	}

	private static void sendMessage(String message) {
		try{
			PlayerConstants.PLAY_PAUSE_HANDLER.sendMessage(PlayerConstants.PLAY_PAUSE_HANDLER.obtainMessage(0, message));
		}catch(Exception e){}
	}
}
