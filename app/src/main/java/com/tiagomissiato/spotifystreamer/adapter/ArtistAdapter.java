package com.tiagomissiato.spotifystreamer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tiagomissiato.spotifystreamer.R;

import java.util.List;

import com.tiagomissiato.spotifystreamer.model.Artist;
import com.tiagomissiato.spotifystreamer.model.Image;

/**
 * Created by trigoleto on 11/27/14.
 */
public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {
    private static final String TAG = ArtistAdapter.class.getSimpleName();

    Context mContext;
    private static List<Artist> items;
    OnItemClicked onItemClicked;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView albumTitle;
        TextView albumSubtitle;
        ImageView albumImage;
        OnItemClicked onItemClicked;

        public ViewHolder(View iteView, OnItemClicked onItemClicked) {
            super(iteView);
            this.albumTitle = (TextView) iteView.findViewById(R.id.album_title);
            this.albumImage = (ImageView) iteView.findViewById(R.id.album_image);
            iteView.setOnClickListener(this);

            this.onItemClicked = onItemClicked;
        }

        @Override
        public void onClick(View view) {
            if (onItemClicked != null){
                onItemClicked.onClicked(items.get(getPosition()));
            }
        }
    }

    public ArtistAdapter(Context mContext, List<Artist> items, OnItemClicked onItemClicked) {
        ArtistAdapter.items = items;
        this.mContext = mContext;
        this.onItemClicked = onItemClicked;
    }

    @Override
    public int getItemCount() {
        return ArtistAdapter.items.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View layout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_album, null);

        return new ViewHolder(layout, onItemClicked);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Artist item = ArtistAdapter.items.get(position);

        viewHolder.albumTitle.setText(item.name);
        String correctImage = null;
        for(Image img : item.images){
            // put add and sub 5 to compare because in the URL there was some width 199, soh just in case;
            if(img.width >= 195 && img.width <= 305)
                correctImage = img.url;

        }

        if(correctImage != null) {
            Glide.with(mContext).load(correctImage)
                    .placeholder(R.drawable.place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.place_holder)
                    .override(200, 200)
                    .into(viewHolder.albumImage);
        } else {
            Glide.with(mContext).load(R.drawable.place_holder)
                    .placeholder(R.drawable.place_holder)
                    .override(200, 200)
                    .into(viewHolder.albumImage);
        }
    }

    public interface OnItemClicked{
        void onClicked(Artist item);
    }
}
