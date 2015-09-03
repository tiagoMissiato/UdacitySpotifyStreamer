package com.tiagomissiato.spotifystreamer.helper;

import android.content.Context;

/**
 * Created by tiagomissiato on 9/3/15.
 */
public class PreferenceHelper {

    private static String PREFERENCE = "app.preference";
    private static String PREFERENCE_USER = "edoc.preference.user";

    protected static PreferenceHelper INSTANCE;
    Context mContext;

    public static PreferenceHelper getInstance(Context mContext){
        if(INSTANCE == null)
            INSTANCE = new PreferenceHelper();

        INSTANCE.mContext = mContext;
        return INSTANCE;
    }

}
