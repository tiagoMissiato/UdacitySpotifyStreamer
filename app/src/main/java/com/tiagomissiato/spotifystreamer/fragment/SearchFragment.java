package com.tiagomissiato.spotifystreamer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.tiagomissiato.spotifystreamer.MainActivity;
import com.tiagomissiato.spotifystreamer.R;
import com.tiagomissiato.spotifystreamer.TopTenActivity;
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

public class SearchFragment extends Fragment implements ArtistAdapter.OnItemClicked{

    public static final String TAG = SearchFragment.class.getSimpleName();

    public static String SAVEINSTANCE_LIST = "artist.list";
    public static String SAVEINSTANCE_SAD_FACE = "artist.sad.face";
    public static String SAVEINSTANCE_FROM_INSTANCE = "artist.from.instance";

    MainActivity mActivity;

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView albumList;

    LinearLayout noResult;

    ProgressBar loading;
    EditText artistName;

    ArtistAdapter.OnItemClicked twoPaneListener;

    SearchSpotifyTask task = new SearchSpotifyTask();

    List<Artist> artists = new ArrayList<>();
    List<com.tiagomissiato.spotifystreamer.model.Artist> artistsTest = new ArrayList<>();

    boolean mShowSadFace, mFromInstance = false;

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_search, container, false);

        mActivity = (MainActivity) getActivity();

        albumList = (RecyclerView) layout.findViewById(R.id.album_list);
        loading = (ProgressBar) layout.findViewById(R.id.progressBar);
        noResult = (LinearLayout) layout.findViewById(R.id.no_result);

        artistName = (EditText) layout.findViewById(R.id.artist_name);

        mLayoutManager = new LinearLayoutManager(mActivity);
        albumList.setLayoutManager(mLayoutManager);
        albumList.setHasFixedSize(true);
        albumList.setItemAnimator(new DefaultItemAnimator());

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SAVEINSTANCE_LIST, (ArrayList<com.tiagomissiato.spotifystreamer.model.Artist>) artistsTest);
        outState.putBoolean(SAVEINSTANCE_SAD_FACE, mShowSadFace);
        outState.putBoolean(SAVEINSTANCE_FROM_INSTANCE, true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            artistsTest = (List<com.tiagomissiato.spotifystreamer.model.Artist>) savedInstanceState.getSerializable(SAVEINSTANCE_LIST);
            mShowSadFace = savedInstanceState.getBoolean(SAVEINSTANCE_SAD_FACE, false);
            mFromInstance = savedInstanceState.getBoolean(SAVEINSTANCE_FROM_INSTANCE, false);
            Log.i("DEBUG", "onRestoreInstanceState");
        }

    }

    @Override
    public void onResume() {
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

    public void populateList(){
        if(artistsTest != null) {
            if(artistsTest.size() > 0) {
                ArtistAdapter adapter = new ArtistAdapter(mActivity, artistsTest, this);
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
        if(twoPaneListener != null){
            twoPaneListener.onClicked(item);
        } else {
            Bundle bnd = new Bundle();
            bnd.putSerializable(TopTenFragment.ARTIST, item);

            Intent intent = new Intent(mActivity, TopTenActivity.class);
            intent.putExtras(bnd);

            startActivity(intent);
        }
    }

    public void setTwoPaneListener(ArtistAdapter.OnItemClicked twoPaneListener) {
        this.twoPaneListener = twoPaneListener;
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
                                mActivity.runOnUiThread(new Runnable() {
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

}
