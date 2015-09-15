package com.tiagomissiato.spotifystreamer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.tiagomissiato.spotifystreamer.MainActivity;
import com.tiagomissiato.spotifystreamer.PlaySongActivity;
import com.tiagomissiato.spotifystreamer.R;
import com.tiagomissiato.spotifystreamer.TopTenActivity;
import com.tiagomissiato.spotifystreamer.adapter.ArtistTopTrackAdapter;
import com.tiagomissiato.spotifystreamer.helper.PlayerConstants;
import com.tiagomissiato.spotifystreamer.model.Artist;
import com.tiagomissiato.spotifystreamer.model.TrackTree;

import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TopTenFragment extends Fragment{

    public static final String TAG = TopTenFragment.class.getSimpleName();
    public static final String ARTIST = "top.ten.fragment.artist";

    public static String SAVEINSTANCE_LIST = "artist.list.track";
    public static String SAVEINSTANCE_LIST_SIZE = "artist.list.size";
    public static String BUNDLE_ARTIST_ID = "top.ten.artist.id";
    public static String BUNDLE_ARTIST_NAME = "top.ten.artist.name";

    Activity mActivity;

    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView trackList;
    ArtistTopTrackAdapter adapter;

    LinearLayout noResult;

    Artist mArtist;

    ProgressBar loading;
    TrackTree tree;
    int treeNodeSize;

    public static TopTenFragment newInstance(Artist artist) {
        TopTenFragment fragment = new TopTenFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARTIST, artist);
        fragment.setArguments(args);
        return fragment;
    }

    public TopTenFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArtist = (Artist) getArguments().getSerializable(ARTIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_top_ten, container, false);


        mActivity = getActivity();

        trackList = (RecyclerView) layout.findViewById(R.id.album_list);
        loading = (ProgressBar) layout.findViewById(R.id.progressBar);
        noResult = (LinearLayout) layout.findViewById(R.id.no_result);

        mLayoutManager = new LinearLayoutManager(mActivity);
        trackList.setLayoutManager(mLayoutManager);
        trackList.setHasFixedSize(true);
        trackList.setItemAnimator(new DefaultItemAnimator());

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("DEBUG", "onSaveInstanceState");
        outState.putSerializable(SAVEINSTANCE_LIST, tree);
        outState.putInt(SAVEINSTANCE_LIST_SIZE, treeNodeSize);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null){
            tree = (TrackTree) savedInstanceState.getSerializable(SAVEINSTANCE_LIST);
            treeNodeSize = savedInstanceState.getInt(SAVEINSTANCE_LIST_SIZE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(tree == null || tree.track == null)
            searchTopTenTracks();
        else
            populateList();
    }

    public void setArtist(Artist artist){
        mArtist = artist;
        searchTopTenTracks();
    }

    private void searchTopTenTracks() {
        if(mArtist != null) {
            showLoading();
            SpotifyApi api = new SpotifyApi();

            HashMap<String, Object> parameter = new HashMap<>();
            parameter.put("country", "BR");

            SpotifyService spotify = api.getService();
            spotify.getArtistTopTrack(mArtist.id, parameter, new Callback<Tracks>() {
                @Override
                public void success(Tracks tracks, Response response) {

                    tree = new TrackTree();

                    int i = 0;
                    treeNodeSize = tracks.tracks.size();
                    for (Track tk : tracks.tracks) {
                        com.tiagomissiato.spotifystreamer.model.Track current = new com.tiagomissiato.spotifystreamer.model.Track(i, tk);

                        tree.addNode(i, current);
                        if (i > 0 && i < treeNodeSize - 1)
                            tree.addNode(i - 1, tree.findNode(i - 1));

                        i++;
                    }

                    tree.findNode(0).prev = tree.findNode(treeNodeSize - 1);
                    tree.findNode(treeNodeSize - 1).next = tree.findNode(0);

                    PlayerConstants.SONGS_LIST = tree;

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateList();
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.i("DEBUG", error.getMessage());
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                        }
                    });
                }
            });
        }
    }

    public void changeOrientation(){
        if(adapter != null){
            if(getResources().getBoolean(R.bool.has_two_panes))
                adapter.changeListener((MainActivity) mActivity);
            else
                adapter.changeListener((TopTenActivity) mActivity);
        }
    }

    public void populateList(){
        if(tree != null) {
            if(tree.track != null) {
                if(getResources().getBoolean(R.bool.has_two_panes)){
                    adapter = new ArtistTopTrackAdapter(mActivity, tree, treeNodeSize, (MainActivity) mActivity);
                } else {
                    adapter = new ArtistTopTrackAdapter(mActivity, tree, treeNodeSize, (TopTenActivity) mActivity);
                }
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
}
