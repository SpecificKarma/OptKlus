package com.specifikarma.optimumklus.UI.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.specifikarma.optimumklus.R;

public class CycleCard extends FadeHint{

    private ConstraintLayout distanceRight, distanceLeft, distanceCycle;
    private ViewGroup topLineRight, topLineLeft, bottomLineLeft, bottomLineRight;

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        distanceRight = view.findViewById(R.id.transition_distance_right);
        distanceLeft = view.findViewById(R.id.transition_distance_left);
        distanceCycle = view.findViewById(R.id.transition_distance_cycle);

        topLineRight = view.findViewById(R.id.top_line_right);
        topLineLeft = view.findViewById(R.id.top_line_left);
        bottomLineLeft = view.findViewById(R.id.bottom_line_right);
        bottomLineRight = view.findViewById(R.id.top_rail_left0);

        distanceRight.post(new Runnable() {
            @Override
            public void run() {

                topLineRight.setTranslationX(distanceRight.getWidth());
                topLineLeft.setTranslationX(-distanceLeft.getWidth());
                bottomLineLeft.setTranslationX(-distanceRight.getWidth());
                bottomLineRight.setTranslationX(distanceLeft.getWidth());

                runCycle();
            }
        });
    }

    private void runCycle() {
        float right_width = distanceCycle.getWidth();

        ObjectAnimator anim1 = ObjectAnimator.ofFloat(topLineRight, "translationX", topLineRight.getTranslationX() + right_width);
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setRepeatCount(ValueAnimator.INFINITE);
        anim1.setDuration(25000);
        anim1.start();

        ObjectAnimator anim2 = ObjectAnimator.ofFloat(topLineLeft, "translationX", topLineLeft.getTranslationX() + right_width);
        anim2.setInterpolator(new LinearInterpolator());
        anim2.setRepeatCount(ValueAnimator.INFINITE);
        anim2.setDuration(25000);
        anim2.start();

        ObjectAnimator anim3 = ObjectAnimator.ofFloat(bottomLineLeft, "translationX", bottomLineLeft.getTranslationX() - right_width);
        anim3.setInterpolator(new LinearInterpolator());
        anim3.setRepeatCount(ValueAnimator.INFINITE);
        anim3.setDuration(25000);
        anim3.start();

        ObjectAnimator anim4 = ObjectAnimator.ofFloat(bottomLineRight, "translationX", bottomLineRight.getTranslationX() - right_width);
        anim4.setInterpolator(new LinearInterpolator());
        anim4.setRepeatCount(ValueAnimator.INFINITE);
        anim4.setDuration(25000);
        anim4.start();
    }
}
