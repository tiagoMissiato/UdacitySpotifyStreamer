package com.tiagomissiato.spotifystreamer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

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

    public class Image implements Serializable{
        public Integer width;
        public Integer height;
        public String url;

        public Image(kaaes.spotify.webapi.android.models.Image img) {
            this.height = img.height;
            this.width = img.width;
            this.url = img.url;
        }
    }
}
