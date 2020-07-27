package com.specifikarma.optimumklus.UI.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.specifikarma.optimumklus.R;
import com.specifikarma.optimumklus.UI.contact.AddContact;

public class ZoomBackground extends AddContact {
    private View main_box, text_anim, welcome_anim;;
    private boolean isPlayed;
    private boolean isHorizontal;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.front, container, false);
    }

    private void savePreferences(String KEY, boolean status) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();
        editor.putBoolean(KEY, status);
        editor.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        savePreferences("IS_HORIZONTAL", false);
        savePreferences("IS_PLAYED", false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savePreferences("IS_PLAYED", true);
        savePreferences("IS_HORIZONTAL", true);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadPreferences();

        text_anim = view.findViewById(R.id.text_anim);
        welcome_anim = view.findViewById(R.id.welcome_anim);
        main_box = view.findViewById(R.id.box_01);
        main_box.setScaleX(Float.parseFloat(getString(R.string.zoom)));
        main_box.setScaleY(Float.parseFloat(getString(R.string.zoom)));

        if (!isPlayed) {
            view.findViewById(R.id.scale_anim).setScaleX(400f);
            view.findViewById(R.id.scale_anim).setScaleY(400f);
        } else {
            view.findViewById(R.id.scale_anim).setScaleX(Float.parseFloat(getString(R.string.zoom)));
            view.findViewById(R.id.scale_anim).setScaleY(Float.parseFloat(getString(R.string.zoom)));
            text_anim.setVisibility(View.INVISIBLE);
            welcome_anim.setVisibility(View.INVISIBLE);
        }
        if (isHorizontal) {
            main_box.setScaleX(Float.parseFloat(getString(R.string.zoom)));
            main_box.setScaleY(Float.parseFloat(getString(R.string.zoom)));
            view.findViewById(R.id.scale_anim).setScaleX(Float.parseFloat(getString(R.string.zoom)));
            view.findViewById(R.id.scale_anim).setScaleY(Float.parseFloat(getString(R.string.zoom)));
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPlayed) {
                    scaleView(view.findViewById(R.id.scale_anim), Float.parseFloat(getString(R.string.zoom)));
                    savePreferences("IS_PLAYED", true);
                }
            }
        }, 50);
    }

    private void loadPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        isPlayed = sharedPreferences.getBoolean("IS_PLAYED", false);
        isHorizontal = sharedPreferences.getBoolean("IS_HORIZONTAL", false);
    }

    private void scaleView(final View view, float endScale) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", endScale);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", endScale);
        scaleDownX.setInterpolator(new FastOutSlowInInterpolator());
        scaleDownY.setInterpolator(new FastOutSlowInInterpolator());
        scaleDownX.setDuration(2500);
        scaleDownY.setDuration(2500);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);
        scaleDownX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeView((ViewGroup) text_anim);
                fadeView((ViewGroup) welcome_anim);
            }
        });
        scaleDownX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                View p = (View) view.getParent();
                p.invalidate();
            }
        });
        scaleDown.start();
    }

    private void fadeView(ViewGroup view) {
        TransitionSet set = new TransitionSet()
                .addTransition(new AutoTransition())
                .setDuration(500)
                .setInterpolator(new LinearOutSlowInInterpolator());
        TransitionManager.beginDelayedTransition(view, set);
        view.setVisibility(View.INVISIBLE);
    }
}