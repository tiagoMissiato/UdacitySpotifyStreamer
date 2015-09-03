package com.tiagomissiato.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tiagomissiato.spotifystreamer.fragment.SearchFragment;


public class MainActivity extends AppCompatActivity /*implements ArtistAdapter.OnItemClicked*/ {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        handleFragment();
    }

    public void handleFragment(){

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentByTag(SearchFragment.TAG);
        if(fragment == null)
            fragment = SearchFragment.newInstance();

        fm.beginTransaction()
                .replace(R.id.search_container, fragment, SearchFragment.TAG)
                .commit();

    }
    
}
