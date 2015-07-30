package com.tiagomissiato.spotifystreamer;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeImageTransform;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tiagomissiato.spotifystreamer.adapter.ArtistTopTrackAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTenActivity extends AppCompatActivity implements ArtistTopTrackAdapter.OnItemClicked {

    public static String SAVEINSTANCE_LIST = "artist.list.track";
    public static String BUNDLE_ARTIST_ID = "top.ten.artist.id";
    public static String BUNDLE_ARTIST_NAME = "top.ten.artist.name";

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView trackList;

    LinearLayout noResult;

    String artistId;
    String artistName;

    ProgressBar loading;
    List<com.tiagomissiato.spotifystreamer.model.Track> topTracks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setSharedElementExitTransition(new TransitionSet().
                addTransition(new ChangeImageTransform().addTarget("song_album_image")));

        Bundle extra = getIntent().getExtras();
        if(extra != null){
            artistId = extra.getString(BUNDLE_ARTIST_ID);
            artistName = extra.getString(BUNDLE_ARTIST_NAME);
        }

        trackList = (RecyclerView) findViewById(R.id.album_list);
        loading = (ProgressBar) findViewById(R.id.progressBar);
        noResult = (LinearLayout) findViewById(R.id.no_result);

        mLayoutManager = new LinearLayoutManager(this);
        trackList.setLayoutManager(mLayoutManager);
        trackList.setHasFixedSize(true);
        trackList.setItemAnimator(new DefaultItemAnimator());

        //Set up toolbar
        setUpToolbar();
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
            getSupportActionBar().setSubtitle(artistName);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("DEBUG", "onSaveInstanceState");
        outState.putSerializable(SAVEINSTANCE_LIST, (ArrayList) topTracks);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.i("DEBUG", "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);

        topTracks = (List<com.tiagomissiato.spotifystreamer.model.Track>) savedInstanceState.getSerializable(SAVEINSTANCE_LIST);
    }

    @Override
    protected void onResume() {
        Log.i("DEBUG", "onResume");
        super.onResume();

        showLoading();
        if(topTracks == null || topTracks.size() <= 0)
            searchTopTenTracks();
        else
            populateList();
    }

    private void searchTopTenTracks() {
        SpotifyApi api = new SpotifyApi();

        HashMap<String, Object> parameter = new HashMap<>();
        parameter.put("country", "BR");

        SpotifyService spotify = api.getService();
        spotify.getArtistTopTrack(artistId, parameter, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                for(Track tk : tracks.tracks){
                    topTracks.add(new com.tiagomissiato.spotifystreamer.model.Track(tk));
                }

                TopTenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        populateList();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i("DEBUG", error.getMessage());
                TopTenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                    }
                });
            }
        });
    }

    public void populateList(){
        if(topTracks != null) {
            if(topTracks.size() > 0) {
                ArtistTopTrackAdapter adapter = new ArtistTopTrackAdapter(this, topTracks, this);
                trackList.setAdapter(adapter);
                hideLoading();
            } else {
                showNoResult();
            }

        } else {
            showNoResult();
        }
    }

    private void showNoResult() {
        noResult.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        trackList.setVisibility(View.GONE);
    }

    public void showLoading(){
        loading.setVisibility(View.VISIBLE);
        trackList.setVisibility(View.GONE);
        noResult.setVisibility(View.GONE);
    }

    public void hideLoading(){
        loading.setVisibility(View.GONE);
        noResult.setVisibility(View.GONE);
        trackList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClicked(com.tiagomissiato.spotifystreamer.model.Track item, View image, String url) {
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
//            postponeEnterTransition();
        } else {
            startActivity(playSong);
        }
    }
}
