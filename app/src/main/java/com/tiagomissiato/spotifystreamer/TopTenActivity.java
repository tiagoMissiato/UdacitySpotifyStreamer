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
import com.tiagomissiato.spotifystreamer.model.Artist;


public class TopTenActivity extends AppCompatActivity implements ArtistTopTrackAdapter.OnItemClicked {

//    public static String SAVEINSTANCE_LIST = "artist.list.track";
//    public static String SAVEINSTANCE_LIST_SIZE = "artist.list.size";
//    public static String BUNDLE_ARTIST_ID = "top.ten.artist.id";
//    public static String BUNDLE_ARTIST_NAME = "top.ten.artist.name";
//
//    RecyclerView.LayoutManager mLayoutManager;
//    RecyclerView trackList;
//
//    LinearLayout noResult;
//
//    String artistId;
//    String artistName;
//
//    ProgressBar loading;
//
//    TrackTree tree;
//    int treeNodeSize;

    Artist mArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setSharedElementExitTransition(new TransitionSet().
                addTransition(new ChangeImageTransform().addTarget("song_album_image")));

        Bundle extra = getIntent().getExtras();
        if(extra != null){
            mArtist = (Artist) extra.getSerializable(TopTenFragment.ARTIST);
//            artistId = extra.getString(BUNDLE_ARTIST_ID);
//            artistName = extra.getString(BUNDLE_ARTIST_NAME);
        }
//
//        trackList = (RecyclerView) findViewById(R.id.album_list);
//        loading = (ProgressBar) findViewById(R.id.progressBar);
//        noResult = (LinearLayout) findViewById(R.id.no_result);
//
//        mLayoutManager = new LinearLayoutManager(this);
//        trackList.setLayoutManager(mLayoutManager);
//        trackList.setHasFixedSize(true);
//        trackList.setItemAnimator(new DefaultItemAnimator());

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

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        Log.i("DEBUG", "onSaveInstanceState");
//        outState.putSerializable(SAVEINSTANCE_LIST, tree);
//        outState.putInt(SAVEINSTANCE_LIST_SIZE, treeNodeSize);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        Log.i("DEBUG", "onRestoreInstanceState");
//        super.onRestoreInstanceState(savedInstanceState);
//
//        tree = (TrackTree) savedInstanceState.getSerializable(SAVEINSTANCE_LIST);
//        treeNodeSize = savedInstanceState.getInt(SAVEINSTANCE_LIST_SIZE);
//    }

//    @Override
//    protected void onResume() {
//        Log.i("DEBUG", "onResume");
//        super.onResume();
//
//        if(tree == null || tree.track == null)
//            searchTopTenTracks();
//        else
//            populateList();
//    }

//    private void searchTopTenTracks() {
//        showLoading();
//        SpotifyApi api = new SpotifyApi();
//
//        HashMap<String, Object> parameter = new HashMap<>();
//        parameter.put("country", "BR");
//
//        SpotifyService spotify = api.getService();
//        spotify.getArtistTopTrack(artistId, parameter, new Callback<Tracks>() {
//            @Override
//            public void success(Tracks tracks, Response response) {
//
//                tree = new TrackTree();
//
//                int i = 0;
//                treeNodeSize = tracks.tracks.size();
//                for (Track tk : tracks.tracks) {
//                    com.tiagomissiato.spotifystreamer.model.Track current = new com.tiagomissiato.spotifystreamer.model.Track(i, tk);
//
//                    tree.addNode(i, current);
//                    if(i > 0 && i < treeNodeSize -1)
//                        tree.addNode(i-1, tree.findNode(i-1));
//
//                    i++;
//                }
//
//                tree.findNode(0).prev = tree.findNode(treeNodeSize -1);
//                tree.findNode(treeNodeSize -1).next = tree.findNode(0);
//
//                TopTenActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        populateList();
//                    }
//                });
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.i("DEBUG", error.getMessage());
//                TopTenActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        hideLoading();
//                    }
//                });
//            }
//        });
//    }
//
//    public void populateList(){
//        if(tree != null) {
//            if(tree.track != null) {
//                ArtistTopTrackAdapter adapter = new ArtistTopTrackAdapter(this, tree, treeNodeSize, this);
//                trackList.setAdapter(adapter);
//                hideLoading();
//            } else {
//                showNoResult();
//            }
//
//        } else {
//            showNoResult();
//        }
//    }
//
//    private void showNoResult() {
//        noResult.setVisibility(View.VISIBLE);
//        loading.setVisibility(View.GONE);
//        trackList.setVisibility(View.GONE);
//    }
//
//    public void showLoading(){
//        loading.setVisibility(View.VISIBLE);
//        trackList.setVisibility(View.GONE);
//        noResult.setVisibility(View.GONE);
//    }
//
//    public void hideLoading(){
//        loading.setVisibility(View.GONE);
//        noResult.setVisibility(View.GONE);
//        trackList.setVisibility(View.VISIBLE);
//    }

    @Override
    public void onClicked(com.tiagomissiato.spotifystreamer.model.Track item, Palette palette, View image, String url) {
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
