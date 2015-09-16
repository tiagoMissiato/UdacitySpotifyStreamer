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
import android.view.View;

import com.tiagomissiato.spotifystreamer.adapter.ArtistTopTrackAdapter;
import com.tiagomissiato.spotifystreamer.fragment.TopTenFragment;
import com.tiagomissiato.spotifystreamer.helper.PlayerConstants;
import com.tiagomissiato.spotifystreamer.model.Artist;


public class TopTenActivity extends AppCompatActivity implements ArtistTopTrackAdapter.OnItemClicked {

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
}
