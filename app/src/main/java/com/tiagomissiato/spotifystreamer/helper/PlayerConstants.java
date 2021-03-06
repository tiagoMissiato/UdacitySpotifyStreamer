package com.tiagomissiato.spotifystreamer.helper;

import android.os.Handler;

import com.tiagomissiato.spotifystreamer.MainActivity;
import com.tiagomissiato.spotifystreamer.TopTenActivity;
import com.tiagomissiato.spotifystreamer.interfaces.UIActionsInterface;
import com.tiagomissiato.spotifystreamer.model.TrackTree;

public class PlayerConstants {

	public static MainActivity MAIN_ACTIVITY;
	public static TopTenActivity TOP_TEN_ACTIVITY;

	public static TrackTree SONGS_NEW_LIST;
	public static TrackTree SONGS_LIST;

	//song number which is playing right now from SONGS_LIST
	public static int SONG_NUMBER = 0;
	//song is playing or paused
	public static boolean SONG_PAUSED = true;
	//song changed (next, previous)
	public static boolean SONG_CHANGED = false;
	//handler for song changed(next, previous) defined in service(SongService)
	public static Handler SONG_CHANGE_HANDLER;
	//handler for song play/pause defined in service(SongService)
	public static Handler PLAY_PAUSE_HANDLER;
	//handler for showing song progress defined in Activities(MainActivity, AudioPlayerActivity)
	public static Handler PROGRESSBAR_HANDLER;

	public static Handler CHANGE_PROGRESS;
	public static UIActionsInterface UI_CONTROL_LISTENER;
}
