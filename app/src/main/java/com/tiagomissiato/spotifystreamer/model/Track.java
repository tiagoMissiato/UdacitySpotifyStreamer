package com.tiagomissiato.spotifystreamer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.AlbumSimple;

/**
 * Created by trigoleto on 6/22/15.
 * t.m.rigoleto@gmail.com
 */
public class Track implements Serializable{

    public int pos;
    public String name;
    public Album album;
    public String preview_url;
    public String uri;
    public TrackPalette palette;
    public Track prev;
    public Track next;

    public Track(int pos, kaaes.spotify.webapi.android.models.Track track) {
        this.pos = pos;
        this.name = track.name;
        this.album = new Album(track.album);
        this.preview_url = track.preview_url;
        this.uri = track.uri;
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
