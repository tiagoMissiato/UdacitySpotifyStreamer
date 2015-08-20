package com.tiagomissiato.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
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
    public static String SAVEINSTANCE_LIST = "artist.list";
    public static String SAVEINSTANCE_SAD_FACE = "artist.sad.face";
    public static String SAVEINSTANCE_FROM_INSTANCE = "artist.from.instance";

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView albumList;

    LinearLayout noResult;

    ProgressBar loading;
    EditText artistName;

    SearchSpotifyTask task = new SearchSpotifyTask();

    List<Artist> artists = new ArrayList<>();
    List<com.tiagomissiato.spotifystreamer.model.Artist> artistsTest = new ArrayList<>();

    boolean mShowSadFace, mFromInstance = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        albumList = (RecyclerView) findViewById(R.id.album_list);
        loading = (ProgressBar) findViewById(R.id.progressBar);
        noResult = (LinearLayout) findViewById(R.id.no_result);

        artistName = (EditText) findViewById(R.id.artist_name);

        mLayoutManager = new LinearLayoutManager(this);
        albumList.setLayoutManager(mLayoutManager);
        albumList.setHasFixedSize(true);
        albumList.setItemAnimator(new DefaultItemAnimator());
//        Glide.get(this).clearMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SAVEINSTANCE_LIST, (ArrayList<com.tiagomissiato.spotifystreamer.model.Artist>) artistsTest);
        outState.putBoolean(SAVEINSTANCE_SAD_FACE, mShowSadFace);
        outState.putBoolean(SAVEINSTANCE_FROM_INSTANCE, true);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        artistsTest = (List<com.tiagomissiato.spotifystreamer.model.Artist>) savedInstanceState.getSerializable(SAVEINSTANCE_LIST);
        mShowSadFace = savedInstanceState.getBoolean(SAVEINSTANCE_SAD_FACE, false);
        mFromInstance = savedInstanceState.getBoolean(SAVEINSTANCE_FROM_INSTANCE, false);
        Log.i("DEBUG", "onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();

        showLoading();
        populateList();

        artistName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (artistName.getText().toString().length() >= 3) {
                    if (task != null)
                        task.cancel(true);

                    showLoading();
                    task = new SearchSpotifyTask();
                    task.execute(artistName.getText().toString());
                }
                mFromInstance = false;
            }
        });
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

                    artistsTest = new ArrayList<>();
                    for (Artist spotifyArtist : artists)
                        artistsTest.add(new com.tiagomissiato.spotifystreamer.model.Artist(spotifyArtist));

                    if (!isCancelled() && artistsPager.artists.items != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mShowSadFace = true;
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
        if(artistsTest != null) {
            if(artistsTest.size() > 0) {
                ArtistAdapter adapter = new ArtistAdapter(this, artistsTest, this);
                albumList.setAdapter(adapter);
                hideLoading();
            } else {
                showNoResult();
            }
        } else {
            showNoResult();
        }
    }

    private void showNoResult() {
        if(mShowSadFace)
            noResult.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        albumList.setVisibility(View.GONE);
    }

    public void showLoading(){
        loading.setVisibility(View.VISIBLE);
        albumList.setVisibility(View.GONE);
        noResult.setVisibility(View.GONE);
    }

    public void hideLoading(){
        loading.setVisibility(View.GONE);
        noResult.setVisibility(View.GONE);
        albumList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClicked(com.tiagomissiato.spotifystreamer.model.Artist item) {
        Bundle bnd = new Bundle();
        bnd.putString(TopTenActivity.BUNDLE_ARTIST_ID, item.id);
        bnd.putString(TopTenActivity.BUNDLE_ARTIST_NAME, item.name);

        Intent intent = new Intent(this, TopTenActivity.class);
        intent.putExtras(bnd);

        startActivity(intent);
    }
}
