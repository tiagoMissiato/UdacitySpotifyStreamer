package com.tiagomissiato.spotifystreamer.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tiagomissiato.spotifystreamer.R;
import com.tiagomissiato.spotifystreamer.helper.Controls;
import com.tiagomissiato.spotifystreamer.helper.PlayerConstants;
import com.tiagomissiato.spotifystreamer.helper.UtilFunctions;
import com.tiagomissiato.spotifystreamer.interfaces.UIActionsInterface;
import com.tiagomissiato.spotifystreamer.model.Track;
import com.tiagomissiato.spotifystreamer.model.TrackPalette;
import com.tiagomissiato.spotifystreamer.service.SongService;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by tiagomissiato on 9/3/15.
 */
public class PlaySongDialogFragment extends DialogFragment implements View.OnClickListener, UIActionsInterface {

    public static String INSTANCE_PROGRESS_POSITION = "play.song.progress.position";

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
    OnPlayPause onPlayPause;

    View divider, songInfo;//, vibrant, vibrantD, vibrantL, muted, mutedD, mutedL;
    String imageUrl;
    HashMap<String, Integer> paletteHash;

    int pauseRs = R.mipmap.ic_pause;
    int playRs = R.mipmap.ic_play;
    int startProgressPosition = 0;

    boolean seekBarDragging = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_play_song, container);

        Dialog dialog = getDialog();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout((int) getResources().getDimension(R.dimen.play_song_dialog_size), ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        songImage = (ImageView) view.findViewById(R.id.song_image);
        imgContainer = (LinearLayout) view.findViewById(R.id.image_container);

        prev = (ImageButton) view.findViewById(R.id.prev);
        playPause = (FloatingActionButton) view.findViewById(R.id.play_pause);
        next = (ImageButton) view.findViewById(R.id.next);
        buffering = (ProgressBar) view.findViewById(R.id.buffering_progress);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int newProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarDragging = false;
                Controls.chageProgress(getActivity(), newProgress);
            }
        });

        songProgress = (TextView) view.findViewById(R.id.song_progress);
        songTime = (TextView) view.findViewById(R.id.song_time);
        bufferingText = (TextView) view.findViewById(R.id.buffering);
        songName = (TextView) view.findViewById(R.id.song_name);
        songAlbum = (TextView) view.findViewById(R.id.song_album);

        divider = view.findViewById(R.id.divider);
        songInfo = view.findViewById(R.id.song_info);

        prev.setOnClickListener(this);
        playPause.setOnClickListener(this);
        next.setOnClickListener(this);



        Bundle extra = getArguments();
        if(extra != null) {
            mTrack = (Track) extra.getSerializable(TRACK);
            paletteHash = (HashMap<String, Integer>) extra.getSerializable(PALETTE);
            imageUrl = extra.getString("IMAGE_URL");
            ViewCompat.setTransitionName(songImage, extra.getString("TRANSITION_KEY"));
        }

        imgContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                setupImage();
                return false;
            }
        });

        songName.setText(mTrack.name);
        songAlbum.setText(mTrack.album.name);

//        PlayerConstants.UI_CONTROL_LISTENER = this;
//        PlayerConstants.PROGRESSBAR_HANDLER = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                Integer i[] = (Integer[]) msg.obj;
//                songTime.setText(UtilFunctions.getDuration(i[1]));
//                songProgress.setText(UtilFunctions.getDuration(i[0]));
//                seekBar.setProgress(i[2]);
//            }
//        };
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PlayerConstants.UI_CONTROL_LISTENER = this;
        PlayerConstants.PROGRESSBAR_HANDLER = new Handler(){
            @Override
            public void handleMessage(Message msg){
                Integer i[] = (Integer[])msg.obj;
                songTime.setText(UtilFunctions.getDuration(i[1]));
                songProgress.setText(UtilFunctions.getDuration(i[0]));
                if(!seekBarDragging)
                    seekBar.setProgress(i[2]);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        //autoplay
        boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getActivity());
        if(PlayerConstants.SONG_PAUSED) {
            if (!isServiceRunning) {
                startBuffering();
                playPause.performClick();
            } else {
                startBuffering();
                PlayerConstants.SONG_NUMBER = mTrack.pos;
                PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                PlayerConstants.SONG_PAUSED = false;
            }
        } else if (isServiceRunning) {
            Track track = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER);
            if(!track.id.equals(mTrack.id)){
                startBuffering();
                PlayerConstants.SONG_NUMBER = mTrack.pos;
                PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                PlayerConstants.SONG_PAUSED = false;
            }
        }
        pausePlay();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


    }

    public void setupImage(){
        //Set image half of screen
        int size = imgContainer.getWidth();
        LinearLayout.LayoutParams paramsSongImg = (LinearLayout.LayoutParams) songImage.getLayoutParams();
        paramsSongImg.width = size;
        paramsSongImg.height = size;
        songImage.setLayoutParams(paramsSongImg);

        Glide.with(this).load(UtilFunctions.getBigImageUrl(mTrack.album.images))
                .asBitmap()
                .placeholder(R.drawable.place_holder)
                .error(R.drawable.place_holder)
                .into(new SimpleTarget<Bitmap>(200, 200) {
                          @Override
                          public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                              songImage.setImageBitmap(bitmap);
                          }
                      }
                );

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
                            with(getActivity()).
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

        //check if service is running to change the play pause icon
        pausePlay();
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

        //check if service is running to change the play pause icon
        pausePlay();
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.prev:
                prev();
                break;
            case R.id.play_pause:
                boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getActivity());
                if (!isServiceRunning) {
                    PlayerConstants.SONG_PAUSED = false;
                    PlayerConstants.SONG_NUMBER = mTrack.pos;
                    Intent i = new Intent(getActivity(),SongService.class);
                    getActivity().startService(i);
                    playPause.setImageResource(pauseRs);
                } else {
                    if(!PlayerConstants.SONG_PAUSED){
                        Controls.pauseControl(getActivity());
                        playPause.setImageResource(playRs);
                    }else{
                        if(mTrack.pos == PlayerConstants.SONG_NUMBER) {
                            Controls.playControl(getActivity());
                            playPause.setImageResource(pauseRs);
                        } else {
                            Controls.playSong(getActivity(), mTrack);
                        }
                    }
                }
                break;
            case R.id.next:
                next();
                break;
        }

    }

    private void next(){
        Controls.nextControl(getActivity());
        mTrack = mTrack.next;

        reloadTrackInfo();
    }

    private void prev() {
        Controls.previousControl(getActivity());
        mTrack = mTrack.prev;
        reloadTrackInfo();
    }

    private void reloadTrackInfo() {

        imageUrl = mTrack.album.images.get(0).url;
        setupImage();
        songName.setText(mTrack.name);
        songAlbum.setText(mTrack.album.name);
        seekBar.setProgress(0);
        songProgress.setText("");
        songTime.setText("");
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
        if(isVisible()) {
            playPause.setClickable(false);
            buffering.setVisibility(View.VISIBLE);
            bufferingText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void stopBuffering() {
        if(isVisible()) {
            playPause.setClickable(true);
            buffering.setVisibility(View.GONE);
            bufferingText.setVisibility(View.GONE);
        }
    }

    @Override
    public void changeSongControl() {
        if(isVisible()) {
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

    }

    @Override
    public void pausePlay() {
//        if(isVisible()) {
        if (PlayerConstants.SONG_PAUSED) {
            playPause.setImageResource(playRs);
        } else {
            playPause.setImageResource(pauseRs);
        }
        if(onPlayPause != null)
            onPlayPause.onPlayPause();
//        }
    }

    public void setOnPlayPause(OnPlayPause onPlayPause) {
        this.onPlayPause = onPlayPause;
    }

    public interface OnPlayPause{
        public void onPlayPause();
    }
}
