package com.tiagomissiato.spotifystreamer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by trigoleto on 6/9/15.
 * t.m.rigoleto@gmail.com
 */
public class Artist implements Serializable{

    public String id;
    public String name;
    public List<Image> images;

    public Artist(kaaes.spotify.webapi.android.models.Artist artist) {
        this.id = artist.id;
        this.name = artist.name;
        images = new ArrayList<>();
        for(kaaes.spotify.webapi.android.models.Image img : artist.images){
            images.add(new Image(img));
        }
    }
}
