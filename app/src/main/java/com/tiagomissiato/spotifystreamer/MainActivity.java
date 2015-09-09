package com.tiagomissiato.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
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
            searchFragment.setTwoPaneListener(new ArtistAdapter.OnItemClicked() {
                @Override
                public void onClicked(Artist item) {
                    topTenFragment.setArtist(item);
                }
            });
        }
//        handleFragment();
    }

    @Override
    public void onClicked(Track item, Palette palette, View image, String url) {

        PlaySongDialogFragment dialog = new PlaySongDialogFragment();
        Bundle bnd = new Bundle();
        bnd.putSerializable(PlaySongActivity.TRACK, item);

        bnd.putString("TRANSITION_KEY", ViewCompat.getTransitionName(image));
        bnd.putString("IMAGE_URL", url);

        dialog.setArguments(bnd);

        dialog.show(getSupportFragmentManager(), TAG);

//        PlayerConstants.SONG_PAUSED = false;
//        PlayerConstants.SONG_NUMBER = item.pos;
//        boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
//        if (!isServiceRunning) {
//            Intent i = new Intent(getApplicationContext(),SongService.class);
//            startService(i);
//        } else {
//            PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
//        }
    }

//    public void handleFragment(){
//
//        FragmentManager fm = getSupportFragmentManager();
//
//        Fragment fragment = fm.findFragmentByTag(SearchFragment.TAG);
//        if(fragment == null)
//            fragment = SearchFragment.newInstance();
//
//        fm.beginTransaction()
//                .replace(R.id.search_container, fragment, SearchFragment.TAG)
//                .commit();
//
//    }

    
}
