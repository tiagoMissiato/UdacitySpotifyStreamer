package com.tiagomissiato.spotifystreamer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tiagomissiato.spotifystreamer.R;
import com.tiagomissiato.spotifystreamer.model.Track;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by trigoleto on 11/27/14.
 */
public class ArtistTopTrackAdapter extends RecyclerView.Adapter<ArtistTopTrackAdapter.ViewHolder> {
    private static final String TAG = ArtistTopTrackAdapter.class.getSimpleName();

    Context mContext;
    private static List<com.tiagomissiato.spotifystreamer.model.Track> items;
    OnItemClicked onItemClicked;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView albumTitle;
        TextView albumSubtitle;
        ImageView albumImage;
        Palette palette;
        String imageUrl;
        OnItemClicked onItemClicked;

        public ViewHolder(View iteView, OnItemClicked onItemClicked) {
            super(iteView);
            this.albumTitle = (TextView) iteView.findViewById(R.id.album_title);
            this.albumSubtitle = (TextView) iteView.findViewById(R.id.album_subtitle);
            this.albumImage = (ImageView) iteView.findViewById(R.id.album_image);
            iteView.setOnClickListener(this);

            this.onItemClicked = onItemClicked;
        }

        @Override
        public void onClick(View view) {
            if (onItemClicked != null){
                onItemClicked.onClicked(items.get(getPosition()), palette, albumImage, imageUrl);
            }
        }
    }

    public ArtistTopTrackAdapter(Context mContext, List<com.tiagomissiato.spotifystreamer.model.Track> items, OnItemClicked onItemClicked) {
        ArtistTopTrackAdapter.items = items;
        this.mContext = mContext;
        this.onItemClicked = onItemClicked;
    }

    @Override
    public int getItemCount() {
        return ArtistTopTrackAdapter.items.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View layout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_top_track, null);

        return new ViewHolder(layout, onItemClicked);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        com.tiagomissiato.spotifystreamer.model.Track item = ArtistTopTrackAdapter.items.get(position);

        viewHolder.albumTitle.setText(item.name);
        viewHolder.albumSubtitle.setText(item.album.name);
        String correctImage = null;
        for(Track.Image img : item.album.images){
            // put add and sub 5 to compare because in the URL there was some width 199, soh just in case;
            if(img.width >= 295 && img.width <= 305)
                correctImage = img.url;

        }

        viewHolder.imageUrl = correctImage;
        if(correctImage != null) {
            Glide.with(mContext).load(correctImage)
//                    .placeholder(R.drawable.place_holder)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .override(200, 200)
//                    .error(R.drawable.place_holder)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(200, 200) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            // Do something with bitmap here.
                            viewHolder.albumImage.setImageBitmap(bitmap);
                            viewHolder.palette = Palette.generate(bitmap);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            viewHolder.albumImage.setImageResource(R.drawable.place_holder);
                        }
                    });
//                    .into(viewHolder.albumImage);

        } else {
            Glide.with(mContext).load(R.drawable.place_holder)
                    .override(200, 200)
                    .into(viewHolder.albumImage);
        }
    }

    public interface OnItemClicked{
        void onClicked(com.tiagomissiato.spotifystreamer.model.Track item, Palette palette, View imageView, String url);
    }
}
