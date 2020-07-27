package com.specifikarma.optimumklus.UI.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.specifikarma.optimumklus.R;

public class FadeHint extends ZoomBackground {

    private boolean isPlayed;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadPreferences();

        view.findViewById(R.id.gallery_hint_layout).post(new Runnable() {
            @Override
            public void run() {
                if (!isPlayed) {
                    showHintMessages(view, R.id.gallery_hint_layout, 4000, 2000);
                    showHintMessages(view, R.id.contacts_hint_layout, 6000, 2000);
                }
            }
        });
    }

    private void loadPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        isPlayed = sharedPreferences.getBoolean("IS_PLAYED", false);
    }

    private void showHintMessages(@NonNull final View view, final int layoutId, int delay, int duration) {
        ValueAnimator anim1 = ValueAnimator.ofInt(0, 10);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                fadeTransition(view, layoutId, false);
            }
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                fadeTransition(view, layoutId, true);
            }
        });
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setStartDelay(delay);
        anim1.setDuration(duration);
        anim1.start();
    }

    private void fadeTransition(@NonNull View view, int id, boolean isPlayed) {
        TransitionSet set = new TransitionSet()
                .addTransition(new AutoTransition())
                .setDuration(300)
                .setInterpolator(new FastOutLinearInInterpolator());
        TransitionManager.beginDelayedTransition((ViewGroup) view.findViewById(id), set);
        view.findViewById(id).setVisibility(isPlayed ? View.INVISIBLE : View.VISIBLE);
    }
}
