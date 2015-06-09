package com.tiagomissiato.spotifystreamer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tiagomissiato.spotifystreamer.R;
import com.tiagomissiato.spotifystreamer.model.Album;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by trigoleto on 11/27/14.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();

    Context mContext;
    private List<Artist> items;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView albumTitle;
        TextView albumSubtitle;
        ImageView albumImage;

        public ViewHolder(View iteView) {
            super(iteView);
            this.albumTitle = (TextView) iteView.findViewById(R.id.album_title);
            this.albumSubtitle = (TextView) iteView.findViewById(R.id.album_subtitle);
            this.albumImage = (ImageView) iteView.findViewById(R.id.album_image);
        }
    }

    public AlbumAdapter(Context mContext, List<Artist> items) {
        this.items = items;
        this.mContext = mContext;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View layout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_album, null);

        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Artist item = this.items.get(position);

        viewHolder.albumTitle.setText(item.name);
        viewHolder.albumSubtitle.setVisibility(View.GONE);
        String correctImage = null;
        for(Image img : item.images){
            if(img.width >= 195 && img.width <= 205)// put add and sub 5 to compare because in the URL there was some width 199, soh just in case;
                correctImage = img.url;

        }

        if(correctImage != null) {
            Glide.with(mContext).load(correctImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.acdc_cover)
                    .into(viewHolder.albumImage);
        }
    }

}
