package com.tiagomissiato.spotifystreamer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;

/**
 * Created by trigoleto on 6/22/15.
 * t.m.rigoleto@gmail.com
 */
public class Track implements Serializable{

    public String name;
    public Album album;

    public Track(kaaes.spotify.webapi.android.models.Track track) {
        this.name = track.name;
        this.album = new Album(track.album);
    }

    public class Album implements Serializable{
        public String name;
        public List<Image> images;

        public Album(AlbumSimple album) {
            this.name = album.name;

            images = new ArrayList<>();
            for(kaaes.spotify.webapi.android.models.Image img : album.images){
                images.add(new Image(img));
            }
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
