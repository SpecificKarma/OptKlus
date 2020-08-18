package com.gmail.specifikarma.optimumklus.UI.form;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.navigation.Navigation;

import com.gmail.specifikarma.optimumklus.R;
import com.gmail.specifikarma.optimumklus.UI.Preferences;
import com.gmail.specifikarma.optimumklus.networking.Util;
import com.google.android.material.chip.Chip;

public class Estimation extends Fragment implements View.OnClickListener {
    private int REQUEST_CODE_PERMISSIONS = 1002;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.READ_PHONE_STATE"};
    private EditText name, phone;
    private TextView auto;
    private Button camera;
    private View phoneError, nameError, estimationError;
    private TelephonyManager tMgr;
    private ConstraintLayout main;
    private ConstraintSet constraintSet = new ConstraintSet();
    private boolean isChipped;
    private Form form = new Form();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        name = view.findViewById(R.id.getName);
        nameError = view.findViewById(R.id.nameLine);
        phone = view.findViewById(R.id.getPhone);
        phoneError = view.findViewById(R.id.phoneLine);
        auto = view.findViewById(R.id.auto);
        camera = view.findViewById(R.id.start_camera);
        estimationError = view.findViewById(R.id.chipLine);
        main = view.findViewById(R.id.main);

        for (int id : new int[]{R.id.chip0, R.id.chip1, R.id.chip2,
                R.id.chip3, R.id.chip4, R.id.chip5}) {
            ((Chip) view.findViewById(id)).setOnClickListener(this);
        }

        auto.setOnClickListener(this);
        camera.setOnClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frame_main, container, false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_camera:
                if (checkName() & checkPhone() & checkChips(view.getRootView())) {
                    String formToJson = Util.getGsonParser().toJson(form);
                    Preferences.save("FORM", formToJson, getContext());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_main_to_camera);
                }
                break;
            case R.id.auto:
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                break;
        }
    }

    private boolean checkName() {
        if (name.getText().length() < 3) {
            form.addName("");
            nameError.setBackgroundColor(Color.RED);
            return false;
        }
        form.addName(name.getText().toString());
        nameError.setBackgroundColor(Color.parseColor("#806E6E74"));
        return true;
    }

    private boolean checkPhone() {
        if (phone.getText().length() < 10) {
            phoneError.setBackgroundColor(Color.RED);
            return false;
        }
        form.addPhone(phone.getText().toString());
        phoneError.setBackgroundColor(Color.parseColor("#806E6E74"));
        return true;
    }

    private boolean checkChips(View view) {
        form.getServices().clear();
        for (int id : new int[]{R.id.chip0, R.id.chip1, R.id.chip2,
                R.id.chip3, R.id.chip4, R.id.chip5}) {
            if (((Chip) view.findViewById(id)).isChecked()) {
                isChipped = true;
                form.addServices(((Chip) view.findViewById(id)).getText().toString());
            }
        }
        if (!isChipped) {
            estimationError.setBackgroundColor(Color.RED);
            return false;
        }

        estimationError.setBackgroundColor(Color.parseColor("#806E6E74"));
        isChipped  = false;
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                if (!tMgr.getLine1Number().isEmpty()) {
                    phone.setText(tMgr.getLine1Number());
                }
            } else {
                Toast.makeText(getContext(), getString(R.string.please_type), Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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

    private void runFadeAnim(final View view, int id, int delay, int duration, boolean isVisible) {
        ValueAnimator anim1 = ValueAnimator.ofInt(0, 10);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                super.onAnimationStart(animation);

                TransitionSet set = new TransitionSet()
                        .addTransition(new Fade())
                        .setDuration(duration)
                        .setInterpolator(new FastOutLinearInInterpolator());
                TransitionManager.beginDelayedTransition((ViewGroup) view.findViewById(id), set);

                view.findViewById(id).setVisibility(!isVisible ? View.INVISIBLE : View.VISIBLE);
            }
        });
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setStartDelay(delay);
        anim1.setDuration(duration);
        anim1.start();
    }

}

