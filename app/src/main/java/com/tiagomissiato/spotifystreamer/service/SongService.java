package com.tiagomissiato.spotifystreamer.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RemoteControlClient;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tiagomissiato.spotifystreamer.R;
import com.tiagomissiato.spotifystreamer.helper.Controls;
import com.tiagomissiato.spotifystreamer.helper.PlayerConstants;
import com.tiagomissiato.spotifystreamer.helper.UtilFunctions;
import com.tiagomissiato.spotifystreamer.model.Track;
import com.tiagomissiato.spotifystreamer.receiver.NotificationBroadcast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SongService extends Service implements AudioManager.OnAudioFocusChangeListener{
	String LOG_CLASS = "SongService";
	private MediaPlayer mp;
	int NOTIFICATION_ID = 1111;
	public static final String NOTIFY_PREVIOUS = "com.tiagomissiato.spotifystreamer.previous";
	public static final String NOTIFY_DELETE = "com.tiagomissiato.spotifystreamer.delete";
	public static final String NOTIFY_PAUSE = "com.tiagomissiato.spotifystreamer.pause";
	public static final String NOTIFY_PLAY = "com.tiagomissiato.spotifystreamer.play";
	public static final String NOTIFY_NEXT = "com.tiagomissiato.spotifystreamer.next";
	
	private ComponentName remoteComponentName;
	private RemoteControlClient remoteControlClient;
	AudioManager audioManager;
	Bitmap mDummyAlbumArt;
	private static Timer timer;
	private static boolean currentVersionSupportBigNotification = false;

    OnCompletionListener completionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
//				Controls.nextControl(getApplicationContext());
            Log.i("DEBUG","onCompletion");
        }
    };

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mp = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        currentVersionSupportBigNotification = UtilFunctions.currentVersionSupportBigNotification();
        timer = new Timer();
        mp.setOnCompletionListener(completionListener);
		super.onCreate();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);

		stopSelf();
	}

	/**
	 * Send message from timer
	 * @author jonty.ankit
	 */
	private class MainTask extends TimerTask {
        public void run(){
            handler.sendEmptyMessage(0);
        }
    } 
	
	 private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
        	if(mp != null ){
        		int progress = mp.getDuration() > 0 ? (mp.getCurrentPosition()*100) / mp.getDuration() : 0;

        		Integer i[] = new Integer[3];
        		i[0] = mp.getCurrentPosition();
        		i[1] = mp.getDuration();
        		i[2] = progress;

        		try{
        			PlayerConstants.PROGRESSBAR_HANDLER.sendMessage(PlayerConstants.PROGRESSBAR_HANDLER.obtainMessage(0, i));
        		}catch(Exception e){
                    Log.i("DEBUG", e.getMessage());
                }
        	}
    	}
    }; 
	    
    @SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			Track data = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER);

			PlayerConstants.CHANGE_PROGRESS = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					int newProgress = (int) msg.obj;
					if(mp != null) {
						int progress = (newProgress * mp.getDuration()) / 100;
						mp.seekTo(progress);
					}
					return false;
				}
			});

			PlayerConstants.SONG_CHANGE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					Track data = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER);
					try{
						playSong(data);
					}catch(Exception e){
						e.printStackTrace();
					}
					if(PlayerConstants.UI_CONTROL_LISTENER != null)
						PlayerConstants.UI_CONTROL_LISTENER.changeSongControl();
					newNotification();
					return false;
				}
			});

			PlayerConstants.PLAY_PAUSE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					String message = (String)msg.obj;
					if(mp == null)
						return false;
					if(message.equalsIgnoreCase(getResources().getString(R.string.play))){
						PlayerConstants.SONG_PAUSED = false;
						mp.start();
					}else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
						PlayerConstants.SONG_PAUSED = true;
						mp.pause();
					}
                    if(PlayerConstants.UI_CONTROL_LISTENER != null)
                        PlayerConstants.UI_CONTROL_LISTENER.pausePlay();
                    newNotification();
					return false;
				}
			});
//			firstPlay(data);
			newNotification();
			playSong(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}

	/**
	 * Notification
	 * Custom Bignotification is available from API 16
	 */
	@SuppressLint("NewApi")
	private void newNotification() {
		String songName = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER).name;
		String albumName = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER).album.name;
		RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.custom_notification);
		RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);
		 
		final Notification notification = new NotificationCompat.Builder(getApplicationContext())
		.setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(songName).build();

		setListeners(simpleContentView);
		setListeners(expandedView);
		
		notification.contentView = simpleContentView;
		if(currentVersionSupportBigNotification){
			notification.bigContentView = expandedView;
		}
		
		try{
			final Track tck = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER);
			Glide.with(this)
				.load(UtilFunctions.getSmallImageUrl(tck.album.images))
					.asBitmap()
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(new SimpleTarget<Bitmap>(200, 200) {
						@Override
						public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
							if (bitmap != null) {
								Bitmap albumArt = null;
								try {
									if (bitmap.isRecycled()) {
										albumArt = bitmap.copy(bitmap.getConfig(), false);
									} else {
										albumArt = bitmap;
									}
								} catch (Exception ignored) {
									Log.i("NOTIFICATION", ignored.getMessage());
								}

								notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, bitmap);
								if (currentVersionSupportBigNotification) {
									notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, bitmap);
								}
							} else {
								notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
								if (currentVersionSupportBigNotification) {
									notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
								}
							}
						}
					});
		}catch(Exception e){
			e.printStackTrace();
		}
		if(PlayerConstants.SONG_PAUSED){
			notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
			}
		}else{
			notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
			}
		}

		notification.contentView.setTextViewText(R.id.textSongName, songName);
		notification.contentView.setTextViewText(R.id.textAlbumName, albumName);
		if(currentVersionSupportBigNotification){
			notification.bigContentView.setTextViewText(R.id.textSongName, songName);
			notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
		}
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		startForeground(NOTIFICATION_ID, notification);
	}
	
	/**
	 * Notification click listeners
	 * @param view
	 */
	public void setListeners(RemoteViews view) {
		Intent previous = new Intent(NOTIFY_PREVIOUS);
		Intent delete = new Intent(NOTIFY_DELETE);
		Intent pause = new Intent(NOTIFY_PAUSE);
		Intent next = new Intent(NOTIFY_NEXT);
		Intent play = new Intent(NOTIFY_PLAY);
		
		PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

		PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnDelete, pDelete);
		
		PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPause, pPause);
		
		PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnNext, pNext);
		
		PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

	}
	
	@Override
	public void onDestroy() {
		if(mp != null){
			mp.stop();
			mp = null;
		}
		super.onDestroy();
	}

	/**
	 * Play song, Update Lockscreen fields
	 * @param data
	 */
	@SuppressLint("NewApi")
	private void playSong(Track data) {
		try {
			mp.reset();
			mp.setDataSource(data.preview_url);
			if(PlayerConstants.UI_CONTROL_LISTENER != null)
				PlayerConstants.UI_CONTROL_LISTENER.startBuffering();
			timer.cancel();
			mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					if(PlayerConstants.UI_CONTROL_LISTENER != null)
						PlayerConstants.UI_CONTROL_LISTENER.stopBuffering();
					if(!PlayerConstants.SONG_PAUSED)
						mp.start();

					if(PlayerConstants.UI_CONTROL_LISTENER != null)
						PlayerConstants.UI_CONTROL_LISTENER.pausePlay();
					timer = new Timer();
                    timer.scheduleAtFixedRate(new MainTask(), 0, 100);
				}
			});
			mp.prepareAsync();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	private void RegisterRemoteClient(){
		remoteComponentName = new ComponentName(getApplicationContext(), new NotificationBroadcast().ComponentName());
		 try {
		   if(remoteControlClient == null) {
			   audioManager.registerMediaButtonEventReceiver(remoteComponentName);
			   Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			   mediaButtonIntent.setComponent(remoteComponentName);
			   PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
			   remoteControlClient = new RemoteControlClient(mediaPendingIntent);
			   audioManager.registerRemoteControlClient(remoteControlClient);
		   }
		   remoteControlClient.setTransportControlFlags(
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
				   RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_STOP |
				   RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
				   RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
	  }catch(Exception ex) {
	  }
	}

	@Override
	public void onAudioFocusChange(int focusChange) {}
}