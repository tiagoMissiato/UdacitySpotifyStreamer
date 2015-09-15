package com.tiagomissiato.spotifystreamer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.tiagomissiato.spotifystreamer.adapter.ArtistAdapter;
import com.tiagomissiato.spotifystreamer.adapter.ArtistTopTrackAdapter;
import com.tiagomissiato.spotifystreamer.fragment.SearchFragment;
import com.tiagomissiato.spotifystreamer.fragment.TopTenFragment;
import com.tiagomissiato.spotifystreamer.helper.PlayerConstants;
import com.tiagomissiato.spotifystreamer.helper.UtilFunctions;
import com.tiagomissiato.spotifystreamer.model.Artist;
import com.tiagomissiato.spotifystreamer.model.Track;
import com.tiagomissiato.spotifystreamer.service.SongService;
import com.tiagomissiato.spotifystreamer.view.PlaySongDialogFragment;


public class MainActivity extends AppCompatActivity implements ArtistTopTrackAdapter.OnItemClicked /*implements ArtistAdapter.OnItemClicked*/ {

    private String TAG = MainActivity.class.getSimpleName();

    SearchFragment searchFragment;
    TopTenFragment topTenFragment;
    PlaySongDialogFragment playDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        //Set up toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_container);
        topTenFragment = (TopTenFragment) getSupportFragmentManager().findFragmentById(R.id.top_ten_container);

        if(getResources().getBoolean(R.bool.has_two_panes)){
            Log.i("ORIENTAION", "has_two_panes[onCreate]" + String.valueOf(getResources().getBoolean(R.bool.has_two_panes)));

            searchFragment.setTwoPaneListener(new ArtistAdapter.OnItemClicked() {
                @Override
                public void onClicked(Artist item) {
                    topTenFragment.setArtist(item);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("DEBUG", "onResume");

        //means that is possible to get song info
        boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), this);

        if(isServiceRunning && PlayerConstants.SONGS_LIST != null) {
            Track track = PlayerConstants.SONGS_LIST.findNode(PlayerConstants.SONG_NUMBER);
            if (getResources().getBoolean(R.bool.has_two_panes)) {
                openSongDialog(track, null, UtilFunctions.getBigImageUrl(track.album.images));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("DEBUG", "onPause");

        if(playDialog != null)
            playDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClicked(Track item, Palette palette, View image, String url) {

        openSongDialog(item, image, url);
    }

    private void openSongDialog(Track item, View image, String url) {
        if(playDialog == null || !playDialog.isVisible()) {
            playDialog = new PlaySongDialogFragment();
            Bundle bnd = new Bundle();
            bnd.putSerializable(PlaySongActivity.TRACK, item);

            bnd.putString("TRANSITION_KEY", image != null ? ViewCompat.getTransitionName(image) : null);
            bnd.putString("IMAGE_URL", url);

            playDialog.setArguments(bnd);

            playDialog.show(getSupportFragmentManager(), TAG);
        }
    }
}
