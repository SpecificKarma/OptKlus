package com.specifikarma.optimumklus.UI.gallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.specifikarma.optimumklus.R;
import com.specifikarma.optimumklus.networking.Settings;
import com.specifikarma.optimumklus.networking.Util;

public class GridGalleryAdapter extends BaseAdapter {
    private Context mContext;
    private Settings settings;
    private SharedPreferences sharedPreferences;

    public GridGalleryAdapter(Context c) {
        mContext = c;
        loadPreferences();
    }

    public int getCount() {
        return settings.getGallery().size();
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
            imageView.setPadding(5, 5, 5, 5);
        } else {
            imageView = (ImageView) convertView;
        }

        Glide.with(mContext)
                .load(settings.getGallery().get(position))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .error(R.drawable.loading)
                .override(300, 300)
                .centerCrop()
                .into(imageView);

        imageView.setTag(settings.getGallery().get(position));
        return imageView;
    }

    private void loadPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        settings = Util.getGsonParser().fromJson(sharedPreferences.getString("SETTINGS", ""), Settings.class);
    }
}