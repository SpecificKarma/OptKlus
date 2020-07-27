package com.specifikarma.optimumklus.UI.card;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.specifikarma.optimumklus.R;
import com.specifikarma.optimumklus.UI.animation.CycleCard;
import com.specifikarma.optimumklus.networking.RetrieveSettingsTask;
import com.specifikarma.optimumklus.networking.Settings;
import com.specifikarma.optimumklus.networking.Util;

import java.net.URL;

import pl.droidsonroids.gif.GifImageButton;

public class Cards extends CycleCard implements View.OnClickListener{

    private boolean isPlayed, isSwitched;
    private Settings settings;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private AnimatorSet currentAnimator;
    private ImageView TR1, TL1, TL2, TR2, TL3, TL3_COPY, TR3, BR1, BL1, BL2, BR2, BL3, BR3;
    private GifImageButton menu;

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraSetup(view);

//      Do not use view from constructor to run post() postDelay() directly, instantiate it with other resources
//      Do not use post() postDelay() more that one time for one resource
        menu = view.findViewById(R.id.menu);
        menu.post(new Runnable() {
            @Override
            public void run() {
                if (!isPlayed) {
                    loadSettings();
                }
            }
        });
        menu.postDelayed(new Runnable() {
            @Override
            public void run() {

                loadPreferences();

                populateTopImageLine(view, isSwitched);
                populateBottomImageLine(view, isSwitched);
                savePreferences("IS_SWITCHED", !isSwitched);
            }
        }, 50);

        setOnClick(view);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                navigate(view);
            }
        });
    }

    private void cameraSetup(@NonNull View view) {
        float scale = getResources().getDisplayMetrics().density;
        view.setCameraDistance(6000 * scale);
    }

    private void loadSettings() {
        try {
//            Host valid by 6/2021
            AsyncTask<URL, Void, String> task = new RetrieveSettingsTask().execute(new URL("https://storage.googleapis.com/image_host/images/settings.json"));

            settings = Util.getGsonParser().fromJson(task.get(), Settings.class);
            savePreferences("SETTINGS", task.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        isPlayed = sharedPreferences.getBoolean("IS_PLAYED", false);
        isSwitched = sharedPreferences.getBoolean("IS_SWITCHED", false);
        settings = Util.getGsonParser().fromJson(sharedPreferences.getString("SETTINGS", ""), Settings.class);
    }

    private void savePreferences(String KEY, String text) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();
        editor.putString(KEY, text);
        editor.apply();
    }

    private void savePreferences(String KEY, boolean text) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();
        editor.putBoolean(KEY, text);
        editor.apply();
    }
    @Override
    public void onClick(View view) {
        zoomImageFromThumb(view, (String) view.getTag(), 300);
    }

    void navigate(View view) {
        Navigation.findNavController(view)
                .navigate(R.id.action_cards_to_backCard);
    }

    private void zoomImageFromThumb(final View thumbView, String imageResId, final int shortAnimationDuration) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        final ImageView expandedImageView = thumbView.getRootView().findViewById(
                R.id.front_expanded_image);

        Glide.with(thumbView)
                .load(imageResId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(expandedImageView);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        thumbView.getRootView().findViewById(R.id.base)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);


        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {

            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumbView.setAlpha(1f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
        });
    }

    private void setOnClick(View view) {
        TR1 = view.findViewById(R.id.TR1);
        TR1.setOnClickListener(this);
        TL1 = view.findViewById(R.id.TL1);
        TL1.setOnClickListener(this);
        TL2 = view.findViewById(R.id.TL2);
        TL2.setOnClickListener(this);
        TR2 = view.findViewById(R.id.TR2);
        TR2.setOnClickListener(this);
        TL3 = view.findViewById(R.id.TL3);
        TL3.setOnClickListener(this);
        TR3 = view.findViewById(R.id.TR3);
        TR3.setOnClickListener(this);
        TL3_COPY = view.findViewById(R.id.TL3_COPY);
        TL3_COPY.setOnClickListener(this);
        BR3 = view.findViewById(R.id.BR3);
        BR3.setOnClickListener(this);
        BL3 = view.findViewById(R.id.BL3);
        BL3.setOnClickListener(this);
        BR2 = view.findViewById(R.id.BR2);
        BR2.setOnClickListener(this);
        BR1 = view.findViewById(R.id.BR1);
        BR1.setOnClickListener(this);
        BL2 = view.findViewById(R.id.BL2);
        BL2.setOnClickListener(this);
        BL1 = view.findViewById(R.id.BL1);
        BL1.setOnClickListener(this);
    }

    private void populateTopImageLine(View view, boolean status) {
        Glide.with(view)
                .load(status ? settings.getInstall() : settings.getDesign())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(TR1);
        TR1.setTag(status ? settings.getInstall() : settings.getDesign());
        ((TextView) view.findViewById(R.id.TR1_t)).setText(status ? getString(R.string.install): getString(R.string.design));

        Glide.with(view)
                .load(status ? settings.getInstall() : settings.getDesign())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(TL1);
        TL1.setTag(status ? settings.getInstall() : settings.getDesign());
        ((TextView) view.findViewById(R.id.TL1_t)).setText(status ? getString(R.string.install): getString(R.string.design));

        Glide.with(view)
                .load(status ? settings.getGarden() : settings.getRepair())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(TL2);
        TL2.setTag(status ? settings.getGarden() : settings.getRepair());
        ((TextView) view.findViewById(R.id.TL2_t)).setText(status ? getString(R.string.garden): getString(R.string.repair));


        Glide.with(view)
                .load(status ? settings.getGarden() : settings.getRepair())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(TR2);
        TR2.setTag(status ? settings.getGarden() : settings.getRepair());
        ((TextView) view.findViewById(R.id.TR2_t)).setText(status ? getString(R.string.garden): getString(R.string.repair));

        Glide.with(view)
                .load(status ? settings.getPaint() : settings.getPlace())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(TL3);
        TL3.setTag(status ? settings.getPaint() : settings.getPlace());
        ((TextView) view.findViewById(R.id.TL3_t)).setText(status ? getString(R.string.paint): getString(R.string.place));

        Glide.with(view)
                .load(status ? settings.getPaint() : settings.getPlace())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(TL3_COPY);
        TL3_COPY.setTag(status ? settings.getPaint() : settings.getPlace());
        ((TextView) view.findViewById(R.id.TL3_COPY_t)).setText(status ? getString(R.string.paint): getString(R.string.place));
        
        Glide.with(view)
                .load(status ? settings.getPaint() : settings.getPlace())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(TR3);
        TR3.setTag(status ? settings.getPaint() : settings.getPlace());
        ((TextView) view.findViewById(R.id.TR3_t)).setText(status ? getString(R.string.paint): getString(R.string.place));
    }

    private void populateBottomImageLine(View view, boolean status) {
        Glide.with(view)
                .load(status ?  settings.getPlace() : settings.getPaint())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(BR3);
        BR3.setTag(status ?  settings.getPlace() : settings.getPaint());
        ((TextView) view.findViewById(R.id.BR3_t)).setText(status ? getString(R.string.place): getString(R.string.paint));

        Glide.with(view)
                .load(status ?  settings.getPlace() : settings.getPaint())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(BL3);
        BL3.setTag(BR3.getTag());
        ((TextView) view.findViewById(R.id.BL3_t)).setText(status ? getString(R.string.place): getString(R.string.paint));

        Glide.with(view)
                .load(status ?  settings.getRepair() : settings.getGarden())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(BR2);
        BR2.setTag(status ?  settings.getRepair() : settings.getGarden());
        ((TextView) view.findViewById(R.id.BR2_t)).setText(status ? getString(R.string.repair): getString(R.string.garden));

        Glide.with(view)
                .load(status ?  settings.getRepair() : settings.getGarden())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(BL2);
        BL2.setTag(BR2.getTag());
        ((TextView) view.findViewById(R.id.BL2_t)).setText(status ? getString(R.string.repair): getString(R.string.garden));

        Glide.with(view)
                .load(status ?  settings.getDesign() :  settings.getInstall())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(BL1);
        BL1.setTag(status ?  settings.getDesign() :  settings.getInstall());
        ((TextView) view.findViewById(R.id.BR1_t)).setText(status ? getString(R.string.design): getString(R.string.install));

        Glide.with(view)
                .load(status ?  settings.getDesign() :  settings.getInstall())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .format(DecodeFormat.PREFER_ARGB_8888))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(BR1);
        BR1.setTag(BL1.getTag());
        ((TextView) view.findViewById(R.id.BL1_t)).setText(status ? getString(R.string.design): getString(R.string.install));
    }

}