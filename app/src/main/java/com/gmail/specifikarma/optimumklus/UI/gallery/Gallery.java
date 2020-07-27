package com.gmail.specifikarma.optimumklus.UI.gallery;

import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.gmail.specifikarma.optimumklus.R;
import com.gmail.specifikarma.optimumklus.UI.card.Card;

public class Gallery extends Card {

    private AnimatorSet currentAnimator;
    private Button gallery;

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraSetup(view);

        gallery = view.findViewById(R.id.gallery);

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(view);
            }
        });
    }

    private void cameraSetup(@NonNull View view) {
        float scale = getResources().getDisplayMetrics().density;
        view.setCameraDistance(6000 * scale);
    }

    void navigate(View view) {
        Navigation.findNavController(view)
                .navigate(R.id.action_main_to_gallery);
    }
}