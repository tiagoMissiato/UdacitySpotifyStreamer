package com.tiagomissiato.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.EditText;

import com.tiagomissiato.spotifystreamer.adapter.AlbumAdapter;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    public static String URL_SPOTIFY_ENDPOINT = "https://api.spotify.com/v1/search?type=artist&q=*%s*";

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView albumList;

    SearchSpotifyTask task = new SearchSpotifyTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.pop_toolbar);
        setSupportActionBar(mToolbar);

        albumList = (RecyclerView) findViewById(R.id.album_list);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

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
        }

        return super.onOptionsItemSelected(item);
    }

    private class SearchSpotifyTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... artist) {

            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();
            spotify.searchArtists(artist[0], new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    final List<Artist> artists = artistsPager.artists.items;

                    if(artistsPager.artists.items != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for(Artist a : artists)
                                            Log.i("DEBUG", a.name);
                                        populateList(artists);
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

    public void populateList(List<Artist> artists){
        AlbumAdapter adapter = new AlbumAdapter(this, artists);
        albumList.setAdapter(adapter);
    }
}
