package com.tiagomissiato.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.tiagomissiato.spotifystreamer.adapter.ArtistAdapter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity implements ArtistAdapter.OnItemClicked {

    public static String URL_SPOTIFY_ENDPOINT = "https://api.spotify.com/v1/search?type=artist&q=*%s*";

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView albumList;

    ProgressBar loading;

    SearchSpotifyTask task = new SearchSpotifyTask();

    List<Artist> artists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        albumList = (RecyclerView) findViewById(R.id.album_list);
        loading = (ProgressBar) findViewById(R.id.progressBar);

        final EditText artistName = (EditText) findViewById(R.id.artist_name);
        artistName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(artistName.getText().toString().length() >= 3) {
                    if(task != null)
                        task.cancel(true);

                    showLoading();
                    task = new SearchSpotifyTask();
                    task.execute(artistName.getText().toString());
                }
            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        albumList.setLayoutManager(mLayoutManager);
        albumList.setHasFixedSize(true);
        albumList.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putSerializable("teste", (ArrayList) artists);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        artists = (List<Artist>) savedInstanceState.getSerializable("teste");
    }

    @Override
    protected void onResume() {
        super.onResume();

        showLoading();
        populateList();
    }

    private class SearchSpotifyTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... artist) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.i("SearchSpotifyTask", e.getMessage());
            }

            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();
            spotify.searchArtists(artist[0], new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    artists = artistsPager.artists.items;

                    if(!isCancelled() && artistsPager.artists.items != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        populateList();
                                    }
                                });
                            }
                        }).start();

                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    Log.i("SPOTIFY", "error: " + error.getMessage());
                }
            });
            return null;
        }
    }

    public void populateList(){
        if(artists != null) {
            ArtistAdapter adapter = new ArtistAdapter(this, artists, this);
            albumList.setAdapter(adapter);
        }
        hideLoading();
    }

    public void showLoading(){
        loading.setVisibility(View.VISIBLE);
        albumList.setVisibility(View.GONE);
    }

    public void hideLoading(){
        loading.setVisibility(View.GONE);
        albumList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClicked(Artist item) {
        Bundle bnd = new Bundle();
        bnd.putString(TopTenActivity.BUNDLE_ARTIST_ID, item.id);
        bnd.putString(TopTenActivity.BUNDLE_ARTIST_NAME, item.name);

        Intent intent = new Intent(this, TopTenActivity.class);
        intent.putExtras(bnd);

        startActivity(intent);
    }
}
