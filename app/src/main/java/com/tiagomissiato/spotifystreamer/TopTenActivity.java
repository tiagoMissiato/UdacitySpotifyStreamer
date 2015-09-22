package com.tiagomissiato.spotifystreamer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeImageTransform;
import android.transition.TransitionSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tiagomissiato.spotifystreamer.adapter.ArtistTopTrackAdapter;
import com.tiagomissiato.spotifystreamer.fragment.TopTenFragment;
import com.tiagomissiato.spotifystreamer.helper.PlayerConstants;
import com.tiagomissiato.spotifystreamer.helper.UtilFunctions;
import com.tiagomissiato.spotifystreamer.interfaces.UIActionsInterface;
import com.tiagomissiato.spotifystreamer.model.Artist;
import com.tiagomissiato.spotifystreamer.model.Track;
import com.tiagomissiato.spotifystreamer.service.SongService;


public class TopTenActivity extends AppCompatActivity implements ArtistTopTrackAdapter.OnItemClicked, UIActionsInterface {

    Artist mArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten);

        if(getResources().getBoolean(R.bool.has_two_panes))
            finish();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setSharedElementExitTransition(new TransitionSet().
                addTransition(new ChangeImageTransform().addTarget("song_album_image")));

        Bundle extra = getIntent().getExtras();
        if(extra != null)
            mArtist = (Artist) extra.getSerializable(TopTenFragment.ARTIST);

        //Set up toolbar
        setUpToolbar();

        handleFragment();
    }

    private void setUpToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setSubtitle(mArtist.name);
        }
    }

    public void handleFragment(){

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentByTag(TopTenFragment.TAG);
        if(fragment == null)
            fragment = TopTenFragment.newInstance(mArtist);

        fm.beginTransaction()
                .replace(R.id.top_ten_container, fragment, TopTenFragment.TAG)
                .commit();

    }

    @Override
    protected void onResume() {
        super.onResume();

        PlayerConstants.TOP_TEN_ACTIVITY = this;

        //to check if should show now playing button
        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PlayerConstants.UI_CONTROL_LISTENER = null;
    }

    @Override
    public void onClicked(com.tiagomissiato.spotifystreamer.model.Track item, Palette palette, View image, String url) {

        PlayerConstants.SONGS_LIST = PlayerConstants.SONGS_NEW_LIST;
        Intent playSong = new Intent(this, PlaySongActivity.class);

        Bundle bnd = new Bundle();
        bnd.putSerializable(PlaySongActivity.TRACK, item);

        bnd.putString("TRANSITION_KEY", ViewCompat.getTransitionName(image));
        bnd.putString("IMAGE_URL", url);
        playSong.putExtras(bnd);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, image, "song_album_image");

            startActivity(playSong, options.toBundle());
        } else {
            startActivity(playSong);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), this);
        if(isServiceRunning && !PlayerConstants.SONG_PAUSED)
            getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_now_playing) {
            Track track = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER);

            Intent playSong = new Intent(this, PlaySongActivity.class);

            Bundle bnd = new Bundle();
            bnd.putSerializable(PlaySongActivity.TRACK, track);

            bnd.putString("IMAGE_URL", UtilFunctions.getBigImageUrl(track.album.images));
            playSong.putExtras(bnd);

            startActivity(playSong);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startBuffering() {

    }

    @Override
    public void stopBuffering() {

    }

    @Override
    public void changeSongControl() {

    }

    @Override
    public void pausePlay() {
        //to check if should show now playing button
        invalidateOptionsMenu();
    }
}
