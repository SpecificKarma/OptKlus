package com.gmail.specifikarma.optimumklus.UI.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.gmail.specifikarma.optimumklus.R;
import com.gmail.specifikarma.optimumklus.UI.Preferences;
import com.gmail.specifikarma.optimumklus.UI.contact.AddContact;

public class HintAndCard extends AddContact {

    private boolean isPlayed, isShown;
    private ConstraintLayout main;
    private ConstraintSet constraintSet = new ConstraintSet();

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadPreferences();

        main = view.findViewById(R.id.base);

        if (!isShown) {
            runMoveAnim(view, R.layout.frame_logo, 2000, 500);
            galleryAnim(view, 2000);
            estimationAnim(view, 5000);
            contactsAnim(view, 8000);
            servicesAnim(view, 11000);
            Preferences.save("IS_SHOWN", true, getContext());
        }

        if (!isPlayed && isShown) {
            runMoveAnim(view, R.layout.frame_logo, 2000, 500);
            finishedAnim(view);
        }
        if (isPlayed && isShown) {
            constraintSet.clone(getContext(), R.layout.frame_finish);
            constraintSet.applyTo(main);
            for (int id : new int[]{R.id.gallery_fade, R.id.gallery_desc_fade, R.id.estimation_fade,
                    R.id.estimation_desc_fade, R.id.contacts_fade, R.id.contacts_desc_fade, R.id.services_fade}) {
                try {
                    view.findViewById(id).setVisibility(View.VISIBLE);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadPreferences() {
        isShown = Preferences.load("IS_SHOWN", getContext());
        isPlayed = Preferences.load("IS_PLAYED", getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Preferences.save("IS_PLAYED", true, getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        Preferences.save("IS_PLAYED", false, getContext());
    }

    private void galleryAnim(@NonNull View view, int startTime) {
        runFadeAnim(view, new int[]{R.id.gallery_title_fade, R.id.gallery_hint_fade, R.id.gallery_fade},
                startTime + 500, 500, true);

        runMoveAnim(view, R.layout.frame_gallery_1,
                startTime + 500, 500);

        runFadeAnim(view, new int[]{R.id.gallery_title_fade, R.id.gallery_hint_fade},
                startTime + 2500, 500, false);

        runMoveAnim(view, R.layout.frame_gallery_2,
                startTime + 2500, 500);

        runFadeAnim(view, new int[]{R.id.gallery_desc_fade},
                startTime + 3000, 500, true);
    }

    private void estimationAnim(@NonNull View view, int startTime) {
        runFadeAnim(view, new int[]{R.id.estimation_title_fade, R.id.estimation_hint_fade, R.id.estimation_fade},
                startTime + 500, 500, true);

        runMoveAnim(view, R.layout.frame_estimation_1,
                startTime + 500, 500);

        runFadeAnim(view, new int[]{R.id.estimation_title_fade, R.id.estimation_hint_fade},
                startTime + 2500, 500, false);

        runMoveAnim(view, R.layout.frame_estimation_2,
                startTime + 2500, 500);

        runFadeAnim(view, new int[]{R.id.estimation_desc_fade},
                startTime + 3000, 500, true);
    }

    private void contactsAnim(@NonNull View view, int startTime) {
        runFadeAnim(view, new int[]{R.id.contacts_title_fade, R.id.contacts_hint_fade, R.id.contacts_fade},
                startTime + 500, 500, true);

        runMoveAnim(view, R.layout.frame_contacs_1,
                startTime + 500, 500);

        runFadeAnim(view, new int[]{R.id.contacts_title_fade, R.id.contacts_hint_fade},
                startTime + 2500, 500, false);

        runMoveAnim(view, R.layout.frame_constacs_2,
                startTime + 2500, 500);

        runFadeAnim(view, new int[]{R.id.contacts_desc_fade},
                startTime + 3000, 500, true);
    }

    private void servicesAnim(@NonNull View view, int startTime) {
        runMoveAnim(view, R.layout.frame_services,
                startTime + 500, 500);

        runFadeAnim(view, new int[]{ R.id.services_fade},
                startTime + 500, 500, true);

    }

    private void finishedAnim(@NonNull View view) {
        runMoveAnim(view, R.layout.frame_finish, 2000, 500);
        runFadeAnim(view, new int[]{R.id.gallery_fade, R.id.gallery_desc_fade, R.id.estimation_fade,
                        R.id.estimation_desc_fade, R.id.contacts_fade, R.id.contacts_desc_fade, R.id.services_fade},
                2000 + 500, 500, true);
    }

    private void runMoveAnim(final View view, final int layoutId, int delay, int duration) {
        ValueAnimator anim1 = ValueAnimator.ofInt(0, 10);
        anim1.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                super.onAnimationStart(animation);
                constraintSet.clone(getContext(), layoutId);

                TransitionSet set = new TransitionSet()
                        .setInterpolator(new AnticipateOvershootInterpolator(0.5f))
                        .addTransition(new ChangeBounds())
                        .setDuration(duration);

                TransitionManager.beginDelayedTransition(main, set);
                constraintSet.applyTo(main);
            }
        });
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setStartDelay(delay);
        anim1.setDuration(100);
        anim1.start();
    }

    private void runFadeAnim(final View view, int[] fadeUs, int delay, int duration, boolean isVisible) {
        ValueAnimator anim1 = ValueAnimator.ofInt(0, 10);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                super.onAnimationStart(animation);
                for (int id : fadeUs) {
                    fadeTransition(view, id, duration, isVisible);
                }
            }
        });
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setStartDelay(delay);
        anim1.setDuration(duration);
        anim1.start();
    }

    private void fadeTransition(@NonNull View view, int id, int duration, boolean isVisible) {
        TransitionSet set = new TransitionSet()
                .addTransition(new Fade())
                .setDuration(duration)
                .setInterpolator(new FastOutLinearInInterpolator());
        TransitionManager.beginDelayedTransition((ViewGroup) view.findViewById(id), set);

        view.findViewById(id).setVisibility(!isVisible ? View.INVISIBLE : View.VISIBLE);
    }
}
