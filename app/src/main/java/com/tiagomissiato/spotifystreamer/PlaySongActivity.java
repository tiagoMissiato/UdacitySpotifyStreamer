package com.tiagomissiato.spotifystreamer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.tiagomissiato.spotifystreamer.model.Track;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlaySongActivity extends AppCompatActivity implements View.OnClickListener{

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
    MediaPlayer mediaPlayer;
    private Handler durationHandler = new Handler();
    HashMap<String, Integer> paletteHash;

    int pauseRs = R.mipmap.ic_pause;
    int playRs = R.mipmap.ic_play;

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
//        vibrant = findViewById(R.id.c_vibrant);
//        vibrantD = findViewById(R.id.c_vibrant_dark);
//        vibrantL = findViewById(R.id.c_vibrant_light);
//        muted = findViewById(R.id.c_muted);
//        mutedD = findViewById(R.id.c_muted_dark);
//        mutedL = findViewById(R.id.c_muted_light);


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

        supportPostponeEnterTransition();

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                Glide.with(PlaySongActivity.this).load(mTrack.album.images.get(0).url)
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                songImage.setImageDrawable(resource.getCurrent());
                            }
                        });
            }
        });

        imgContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                setupImage();
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

        if(paletteHash != null)
            configureColors(paletteHash);
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

//                    vibrant.setBackgroundColor(p.getVibrantColor(Color.parseColor("#000000")));
//                    vibrantD.setBackgroundColor(p.getDarkVibrantColor(Color.parseColor("#000000")));
//                    vibrantL.setBackgroundColor(p.getLightVibrantColor(Color.parseColor("#000000")));
//                    muted.setBackgroundColor(p.getMutedColor(Color.parseColor("#000000")));
//                    mutedD.setBackgroundColor(p.getDarkMutedColor(Color.parseColor("#000000")));
//                    mutedL.setBackgroundColor(p.getLightMutedColor(Color.parseColor("#000000")));

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
        if(mediaPlayer != null)
            mediaPlayer.stop();
    }

    public void loadBigImage(){
        Log.i("DEBUG", "loadBigImage");
        Glide.with(this).load(mTrack.album.images.get(0).url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new GlideDrawableImageViewTarget(songImage) {
                    @Override
                    public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                        super.onResourceReady(drawable, anim);
//                        loadBigImage();
                    }
                });
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
                break;
            case R.id.play_pause:
                try {
                    if(mediaPlayer != null && mediaPlayer.isPlaying()){
                        playPause.setImageResource(playRs);
                        mediaPlayer.pause();
                    } else {
                        if(mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(mTrack.preview_url);
//                            mediaPlayer.prepare(); // might take long! (for buffering, etc)
                            playPause.setClickable(false);
                            buffering.setVisibility(View.VISIBLE);
                            bufferingText.setVisibility(View.VISIBLE);
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                public void onPrepared(MediaPlayer mp) {
                                    mediaPlayer.start();
                                    playPause.setImageResource(pauseRs);
                                    playPause.setClickable(true);
                                    buffering.setVisibility(View.GONE);
                                    bufferingText.setVisibility(View.GONE);

                                    int finalTime = mediaPlayer.getDuration();
                                    long min = TimeUnit.MILLISECONDS.toMinutes((long) finalTime);
                                    long sec = TimeUnit.MILLISECONDS.toSeconds((long) finalTime);

                                    seekBar.setMax(finalTime);
                                    seekBar.setClickable(false);

                                    mediaPlayer.start();
                                    int timeElapsed = mediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(timeElapsed);

                                    long minP = TimeUnit.MILLISECONDS.toMinutes((long) timeElapsed);
                                    long secP = TimeUnit.MILLISECONDS.toSeconds((long) timeElapsed);

                                    songProgress.setText(String.format("%02d:%02d", minP, secP));
                                    songTime.setText(String.format("%02d:%02d", min, sec));
                                    durationHandler.postDelayed(updateSeekBarTime, 100);
                                }
                            });
                            mediaPlayer.prepareAsync();
                        } else {
                            mediaPlayer.start();
                            playPause.setImageResource(pauseRs);
                            int timeElapsed = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(timeElapsed);
                            durationHandler.postDelayed(updateSeekBarTime, 100);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.next:
                break;
        }

    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            int timeElapsed = mediaPlayer.getCurrentPosition();

            long min = TimeUnit.MILLISECONDS.toMinutes((long) timeElapsed);
            long sec = TimeUnit.MILLISECONDS.toSeconds((long) timeElapsed);
            songProgress.setText(String.format("%02d:%02d", min, sec));

            seekBar.setProgress(timeElapsed);
            durationHandler.postDelayed(this, 100);
        }
    };

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
}
