package com.gmail.specifikarma.optimumklus.UI.card;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.gmail.specifikarma.optimumklus.R;
import com.gmail.specifikarma.optimumklus.UI.Preferences;
import com.gmail.specifikarma.optimumklus.UI.animation.HintAndCard;
import com.gmail.specifikarma.optimumklus.networking.RetrieveSettingsTask;
import com.gmail.specifikarma.optimumklus.networking.Settings;
import com.gmail.specifikarma.optimumklus.networking.Util;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.net.URL;

public class Card extends HintAndCard {

    private ViewPager2 services;
    private Settings settings;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        services = view.findViewById(R.id.services);

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!Preferences.load("IS_PLAYED", getContext())) {
                    requestJSON();
                } else {
                    loadPreferences();
                }

                CardAdapter adapter = new CardAdapter(getContext(), settings);
                services.setAdapter(adapter);

                runMoveAnim(services, 5000);
            }
        });
    }

    private void runMoveAnim(ViewPager2 services,int delay) {
        ValueAnimator anim1 = ValueAnimator.ofInt(0, 10);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                super.onAnimationStart(animation);
                if(services.getCurrentItem() == 5){
                    services.setCurrentItem(0, true);
                } else {
                    services.setCurrentItem(services.getCurrentItem()+1, true);
                }
                runMoveAnim(services, 5000);
            }
        });
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setStartDelay(delay);
        anim1.setDuration(500);
        anim1.start();
    }

    private void loadPreferences() {
        settings = Preferences.loadSettings(getContext());
    }

    private void requestJSON() {
        try {
//            Host valid by 6/2021
            AsyncTask<URL, Void, String> task = new RetrieveSettingsTask().execute(new URL("https://storage.googleapis.com/image_host/images/settings.json"));

            if (task.get() == null) {
                showRetry();
            } else {
                settings = Util.getGsonParser().fromJson(task.get(), Settings.class);

                Preferences.save("SETTINGS", task.get(), getContext());
            }
        } catch (Exception e) {
            showRetry();
        }
    }

    private void showRetry() {
        new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogTheme)
                .setCancelable(false)
                .setView(getLayoutInflater().inflate(R.layout.connectivity_dialog, null))
                .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Runnable() {
                            @Override
                            public void run() {
                                requestJSON();
                            }
                        }.run();
                    }
                }).create().show();
    }

    private void runCycle(View view) {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX() + 0);
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setRepeatCount(ValueAnimator.INFINITE);
        anim1.setDuration(25000);
        anim1.start();
    }
}
