package com.tiagomissiato.spotifystreamer.model;

import android.graphics.Color;
import android.support.v7.graphics.Palette;

import java.io.Serializable;

/**
 * Created by tiagomissiato on 9/3/15.
 */
public class TrackPalette implements Serializable{

    public int vibrant;
    public int darkVibrant;
    public int lightVibrant;
    public int muted;
    public int darkMuted;
    public int lightMuted;
    public int textColor;

    public TrackPalette(Palette palette) {

        Palette.Swatch swatch = palette.getMutedSwatch();

        this.vibrant = palette.getVibrantColor(Color.parseColor("#000000"));
        this.darkVibrant = palette.getDarkVibrantColor(Color.parseColor("#000000"));
        this.lightVibrant = palette.getLightVibrantColor(Color.parseColor("#000000"));
        this.muted = palette.getMutedColor(Color.parseColor("#000000"));
        this.darkMuted = palette.getDarkMutedColor(Color.parseColor("#000000"));
        this.lightMuted = palette.getLightMutedColor(Color.parseColor("#000000"));
        if(swatch != null)
            this.textColor = swatch.getTitleTextColor();
        else
            this.textColor = Color.parseColor("#000000");
    }
}
