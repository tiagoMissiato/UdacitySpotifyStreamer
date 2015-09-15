package com.tiagomissiato.spotifystreamer;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tiagomissiato.spotifystreamer.helper.Controls;
import com.tiagomissiato.spotifystreamer.helper.PlayerConstants;
import com.tiagomissiato.spotifystreamer.helper.UtilFunctions;
import com.tiagomissiato.spotifystreamer.interfaces.UIActionsInterface;
import com.tiagomissiato.spotifystreamer.model.Track;
import com.tiagomissiato.spotifystreamer.model.TrackPalette;
import com.tiagomissiato.spotifystreamer.service.SongService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlaySongActivity extends AppCompatActivity implements View.OnClickListener, UIActionsInterface{

    public static String TRACK = "play.song.track";
    public static String PALETTE = "play.song.palette";

    LinearLayout imgContainer;

    Track mTrack;
    ImageView songImage;
    ImageButton prev;
    FloatingActionButton playPause;
    ImageButton next;
    TextView songProgress;
    TextView songTime;
    TextView bufferingText;
    TextView songName;
    TextView songAlbum;
    SeekBar seekBar;
    ProgressBar buffering;

    View divider, songInfo;//, vibrant, vibrantD, vibrantL, muted, mutedD, mutedL;
    String imageUrl;
    HashMap<String, Integer> paletteHash;

    int pauseRs = R.mipmap.ic_pause;
    int playRs = R.mipmap.ic_play;
    boolean loadBigImage = false;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        songImage = (ImageView) findViewById(R.id.song_image);
        imgContainer = (LinearLayout) findViewById(R.id.image_container);

        prev = (ImageButton) findViewById(R.id.prev);
        playPause = (FloatingActionButton) findViewById(R.id.play_pause);
        next = (ImageButton) findViewById(R.id.next);
        buffering = (ProgressBar) findViewById(R.id.buffering_progress);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setClickable(false);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        seekBar.setProgress(0);

        songProgress = (TextView) findViewById(R.id.song_progress);
        songTime = (TextView) findViewById(R.id.song_time);
        bufferingText = (TextView) findViewById(R.id.buffering);
        songName = (TextView) findViewById(R.id.song_name);
        songAlbum = (TextView) findViewById(R.id.song_album);

        divider = findViewById(R.id.divider);
        songInfo = findViewById(R.id.song_info);

        prev.setOnClickListener(this);
        playPause.setOnClickListener(this);
        next.setOnClickListener(this);

        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            mTrack = (Track) extra.getSerializable(TRACK);
            paletteHash = (HashMap<String, Integer>) extra.getSerializable(PALETTE);
            imageUrl = extra.getString("IMAGE_URL");
            ViewCompat.setTransitionName(songImage, extra.getString("TRANSITION_KEY"));
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportPostponeEnterTransition();
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                    super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                    //load the big image in background;
                    Glide.with(PlaySongActivity.this).load(UtilFunctions.getBigImageUrl(mTrack.album.images))
                            .into(new SimpleTarget<GlideDrawable>() {
                                @Override
                                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                    songImage.setImageDrawable(resource.getCurrent());
                                    loadBigImage = true;
                                }
                            });
                }
            });
        }

        imgContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                setupImageBig();
                return false;
            }
        });

        setupToolbar();

        songName.setText(mTrack.name);
        songAlbum.setText(mTrack.album.name);

    }

    private void setupToolbar() {
        //Set up toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
    }

    public void setupImageBig(){
          //Set image half of screen
        int size = imgContainer.getWidth();
        LinearLayout.LayoutParams paramsSongImg = (LinearLayout.LayoutParams) songImage.getLayoutParams();
        paramsSongImg.width = size;
        paramsSongImg.height = size;
        songImage.setLayoutParams(paramsSongImg);

        Glide.with(PlaySongActivity.this).load(imageUrl)
        .into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                //only place image if the big one os not yet loaded
                if(!loadBigImage)
                    songImage.setImageDrawable(resource.getCurrent());

                supportStartPostponedEnterTransition();
            }
        });

        if(mTrack.palette != null)
            configureColors(mTrack.palette);
        else
            setupColors();
    }

    public void setupImage(){
          //Set image half of screen
        int size = imgContainer.getWidth();
        LinearLayout.LayoutParams paramsSongImg = (LinearLayout.LayoutParams) songImage.getLayoutParams();
        paramsSongImg.width = size;
        paramsSongImg.height = size;
        songImage.setLayoutParams(paramsSongImg);

        Glide.with(this).load(imageUrl)
                .placeholder(R.drawable.place_holder)
                .error(R.drawable.place_holder)
                .override(size, size)
                .into(new GlideDrawableImageViewTarget(songImage) {
                    @Override
                    public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                        super.onResourceReady(drawable, anim);
                        supportStartPostponedEnterTransition();
                    }
                });

        if(mTrack.palette != null)
            configureColors(mTrack.palette);
        else
            setupColors();
    }

    public void setupColors(){

        new AsyncTask<Void, Void, Void>() {
            Bitmap bitmap;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    bitmap = Glide.
                            with(PlaySongActivity.this).
                            load(imageUrl).
                            asBitmap().
                            into(-1,-1).
                            get();
                } catch (final ExecutionException | InterruptedException e) {
                    Log.e("DEBUG", e.getMessage());
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void dummy) {
                if (null != bitmap) {
                    // The full bitmap should be available here
                    configureColors(bitmap);
                };
            }
        }.execute();
    }

    private void configureColors(TrackPalette palette) {

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[] {
                palette.muted,
                palette.muted,
                palette.muted,
                palette.muted
        };
        int[] colorsSeekBar = new int[] {
                palette.vibrant,
                palette.vibrant,
                palette.vibrant,
                palette.vibrant
        };

        ColorStateList fabList = new ColorStateList(states, colors);
        ColorStateList seekBarList = new ColorStateList(states, colorsSeekBar);

        if(isColorDark(palette.muted)){
            playRs = R.mipmap.ic_play_white;
            pauseRs = R.mipmap.ic_pause_white;
        }
        playPause.setImageResource(playRs);

        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
        thumb.getPaint().setColor(palette.vibrant);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setThumbTintList(seekBarList);
            seekBar.setProgressTintList(seekBarList);
            buffering.setIndeterminateTintList(seekBarList);
        }

        playPause.setBackgroundTintList(fabList);
        playPause.setRippleColor(palette.muted);
        divider.setBackgroundColor(palette.muted);
        songInfo.setBackgroundColor(palette.muted);

        songName.setTextColor(palette.textColor);
        songAlbum.setTextColor(palette.textColor);
    }

    private void configureColors(HashMap<String, Integer> palette) {

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[] {
                palette.get("muted"),
                palette.get("muted"),
                palette.get("muted"),
                palette.get("muted")
        };
        int[] colorsSeekBar = new int[] {
                palette.get("vibrant"),
                palette.get("vibrant"),
                palette.get("vibrant"),
                palette.get("vibrant")
        };

        ColorStateList fabList = new ColorStateList(states, colors);
        ColorStateList seekBarList = new ColorStateList(states, colorsSeekBar);

        if(isColorDark(palette.get("muted"))){
            playRs = R.mipmap.ic_play_white;
            pauseRs = R.mipmap.ic_pause_white;
        }
        playPause.setImageResource(playRs);

        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
        thumb.getPaint().setColor(palette.get("vibrant"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setThumbTintList(seekBarList);
            seekBar.setProgressTintList(seekBarList);
            buffering.setIndeterminateTintList(seekBarList);
        }

        playPause.setBackgroundTintList(fabList);
        playPause.setRippleColor(palette.get("muted"));
        divider.setBackgroundColor(palette.get("muted"));
        songInfo.setBackgroundColor(palette.get("muted"));

        songName.setTextColor(palette.get("textColor"));
        songAlbum.setTextColor(palette.get("textColor"));
    }

    private void configureColors(Bitmap bitmap) {
        Palette p = Palette.generate(bitmap);

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[] {
                p.getMutedColor(Color.parseColor("#000000")),
                p.getMutedColor(Color.parseColor("#000000")),
                p.getMutedColor(Color.parseColor("#000000")),
                p.getMutedColor(Color.parseColor("#000000"))
        };
        int[] colorsSeekBar = new int[] {
                p.getVibrantColor(Color.parseColor("#000000")),
                p.getVibrantColor(Color.parseColor("#000000")),
                p.getVibrantColor(Color.parseColor("#000000")),
                p.getVibrantColor(Color.parseColor("#000000"))
        };

        ColorStateList fabList = new ColorStateList(states, colors);
        ColorStateList seekBarList = new ColorStateList(states, colorsSeekBar);

        if(isColorDark(p.getMutedColor(Color.parseColor("#000000")))){
            playRs = R.mipmap.ic_play_white;
            pauseRs = R.mipmap.ic_pause_white;
        }
        playPause.setImageResource(playRs);

        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
        thumb.getPaint().setColor(p.getVibrantColor(Color.parseColor("#000000")));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setThumbTintList(seekBarList);
            seekBar.setProgressTintList(seekBarList);
            buffering.setIndeterminateTintList(seekBarList);
        }

        playPause.setBackgroundTintList(fabList);
        playPause.setRippleColor(p.getDarkMutedColor(Color.parseColor("#000000")));
        divider.setBackgroundColor(p.getMutedColor(Color.parseColor("#000000")));
        songInfo.setBackgroundColor(p.getMutedColor(Color.parseColor("#000000")));

        Palette.Swatch swatch = p.getMutedSwatch();
        songName.setTextColor(swatch.getTitleTextColor());
        songAlbum.setTextColor(swatch.getTitleTextColor());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_song, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home){
            supportFinishAfterTransition();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.prev:
                prev();
                break;
            case R.id.play_pause:
                boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), this);
                if (!isServiceRunning) {
                    PlayerConstants.UI_CONTROL_LISTENER = this;
                    PlayerConstants.PROGRESSBAR_HANDLER = new Handler(){
                        @Override
                        public void handleMessage(Message msg){
                            Integer i[] = (Integer[])msg.obj;
                            songTime.setText(UtilFunctions.getDuration(i[1]));
                            songProgress.setText(UtilFunctions.getDuration(i[0]));
                            seekBar.setProgress(i[2]);
                        }
                    };

                    PlayerConstants.SONG_PAUSED = false;
                    PlayerConstants.SONG_NUMBER = mTrack.pos;
                    Intent i = new Intent(this,SongService.class);
                    startService(i);
                } else {
                    if(!PlayerConstants.SONG_PAUSED){
                        Controls.pauseControl(this);
                    }else{
                        Controls.playControl(this);
                    }
                }

                break;
            case R.id.next:
                next();
                break;
        }

    }

    private void next(){
        Controls.nextControl(this);
        mTrack = mTrack.next;

        reloadTrackInfo();
    }

    private void prev() {
        Controls.previousControl(this);
        mTrack = mTrack.prev;
        reloadTrackInfo();
    }

    private void reloadTrackInfo() {

        imageUrl = UtilFunctions.getBigImageUrl(mTrack.album.images);
        setupImage();
        songName.setText(mTrack.name);
        songAlbum.setText(mTrack.album.name);
        seekBar.setProgress(0);
        songProgress.setText("");
        songTime.setText("");
        playPause.setImageResource(pauseRs);

    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.4){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

    @Override
    public void startBuffering() {
        playPause.setClickable(false);
        buffering.setVisibility(View.VISIBLE);
        bufferingText.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopBuffering() {
        playPause.setClickable(true);
        buffering.setVisibility(View.GONE);
        bufferingText.setVisibility(View.GONE);
    }

    @Override
    public void changeSongControl() {
        mTrack = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER);
        imageUrl = UtilFunctions.getBigImageUrl(mTrack.album.images);
        setupImage();
        songName.setText(mTrack.name);
        songAlbum.setText(mTrack.album.name);
        seekBar.setProgress(0);
        songProgress.setText("");
        songTime.setText("");
        playPause.setImageResource(pauseRs);

    }

    @Override
    public void pausePlay() {
        if(PlayerConstants.SONG_PAUSED){
            playPause.setImageResource(playRs);
        } else {
            playPause.setImageResource(pauseRs);
        }
    }


}
