package com.tiagomissiato.spotifystreamer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import java.util.List;

public class PlaySongActivity extends AppCompatActivity implements View.OnClickListener{

    public static String TRACK = "play.song.track";

    LinearLayout imgContainer;

    Track mTrack;
    ImageView songImage;
    Button prev;
    Button playPause;
    Button next;

    String imageUrl;
    MediaPlayer mediaPlayer;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        songImage = (ImageView) findViewById(R.id.song_image);
        imgContainer = (LinearLayout) findViewById(R.id.image_container);

        prev = (Button) findViewById(R.id.prev);
        playPause = (Button) findViewById(R.id.play_pause);
        next = (Button) findViewById(R.id.next);
        prev.setOnClickListener(this);
        playPause.setOnClickListener(this);
        next.setOnClickListener(this);

        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            mTrack = (Track) extra.getSerializable(TRACK);
            imageUrl = extra.getString("IMAGE_URL");
            ViewCompat.setTransitionName(songImage, extra.getString("TRANSITION_KEY"));
        }

        supportPostponeEnterTransition();

        songImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                loadBigImage();
            }
        });
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
    }

    @Override
    protected void onResume() {
        super.onResume();


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
                        mediaPlayer.pause();
                    } else {
                        if(mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(mTrack.preview_url);
                            mediaPlayer.prepare(); // might take long! (for buffering, etc)
                            mediaPlayer.start();
                        } else {
                            mediaPlayer.start();
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
}
