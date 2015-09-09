package com.tiagomissiato.spotifystreamer.model;

import java.io.Serializable;

/**
 * Created by tiagomissiato on 9/5/15.
 */
public class Image implements Serializable {

    public Integer width;
    public Integer height;
    public String url;

    public Image(kaaes.spotify.webapi.android.models.Image img) {
        this.height = img.height;
        this.width = img.width;
        this.url = img.url;
    }

}
