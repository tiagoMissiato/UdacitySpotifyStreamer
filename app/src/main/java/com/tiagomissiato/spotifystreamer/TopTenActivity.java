package com.tiagomissiato.spotifystreamer;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tiagomissiato.spotifystreamer.adapter.ArtistAdapter;
import com.tiagomissiato.spotifystreamer.adapter.ArtistTopTrackAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTenActivity extends AppCompatActivity implements ArtistTopTrackAdapter.OnItemClicked {

    public static String BUNDLE_ARTIST_ID = "top.ten.artist.id";
    public static String BUNDLE_ARTIST_NAME = "top.ten.artist.name";

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView trackList;

    LinearLayout noResult;

    String artistId;
    String artistName;

    ProgressBar loading;
    List<Track> topTracks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten);

        Bundle extra = getIntent().getExtras();
        if(extra != null){
            artistId = extra.getString(BUNDLE_ARTIST_ID);
            artistName = extra.getString(BUNDLE_ARTIST_NAME);
            Toast.makeText(this, artistId, Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();

        showLoading();
        SpotifyApi api = new SpotifyApi();

        HashMap<String, Object> parameter = new HashMap<>();
        parameter.put("country", "BR");

        SpotifyService spotify = api.getService();
        spotify.getArtistTopTrack(artistId, parameter, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                topTracks = tracks.tracks;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TopTenActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                populateList();
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i("DEBUG", error.getMessage());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TopTenActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoading();
                            }
                        });
                    }
                }).start();
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
    public void onClicked(Track item) {
        Toast.makeText(this, item.name, Toast.LENGTH_SHORT).show();
    }
}
