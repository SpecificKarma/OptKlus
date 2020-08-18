package com.gmail.specifikarma.optimumklus.UI.gallery;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.gmail.specifikarma.optimumklus.R;
import com.gmail.specifikarma.optimumklus.UI.Preferences;
import com.gmail.specifikarma.optimumklus.networking.Settings;

public class GridGalleryAdapter extends BaseAdapter {
    private Context mContext;
    private Settings mSettings;

    public GridGalleryAdapter(Context c) {
        mContext = c;
        loadPreferences();
    }

    public int getCount() {
        return mSettings.getGallery().size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(2, 2, 2, 2);
        } else {
            imageView = (ImageView) convertView;
        }

        Glide.with(mContext)
                .load(mSettings.getGallery().get(position))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .error(R.drawable.gallery_loading)
                .override(300, 300)
                .centerCrop()
                .into(imageView);

        imageView.setTag(mSettings.getGallery().get(position));
        return imageView;
    }

    private void loadPreferences() {
        mSettings = Preferences.loadSettings(mContext);
    }
}