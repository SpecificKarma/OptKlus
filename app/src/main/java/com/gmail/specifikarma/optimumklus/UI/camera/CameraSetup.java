package com.gmail.specifikarma.optimumklus.UI.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.gmail.specifikarma.optimumklus.R;
import com.gmail.specifikarma.optimumklus.UI.Preferences;
import com.gmail.specifikarma.optimumklus.UI.form.Form;
import com.gmail.specifikarma.optimumklus.networking.Email;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;

public class CameraSetup extends Fragment implements View.OnClickListener {

    private boolean isFlash, hasImage;
    private CameraSelector cameraSelector;
    private DrawImageView paint_me;
    private Executor executor = Executors.newSingleThreadExecutor();
    public File file;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private Form form = new Form();
    private ImageCapture imageCapture;
    private ImageView recentImage;
    private int number = 1;
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private Preview preview;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private TextView count;
    private Button sent;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(getContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
            }
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();

                    preview = new Preview.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                            .build();

                    imageCapture = new ImageCapture.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                            .setTargetRotation(toSurfaceRotationDegrees(getActivity().getWindowManager().getDefaultDisplay().getRotation()))
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                            .build();

                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    ImageCapture.Builder builder = new ImageCapture.Builder();

                    //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
                    HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

                    if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
                        hdrImageCaptureExtender.enableExtension(cameraSelector);
                    }

                    Camera camera = cameraProvider.bindToLifecycle(
                            (getViewLifecycleOwner()),
                            cameraSelector,
                            preview,
                            imageCapture);

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }

                    // Connect the preview use case to the previewView
                    preview.setSurfaceProvider(previewView.createSurfaceProvider());

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                    Log.e("CAMERA", e.toString());
                }
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.camera, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            cameraProvider.unbindAll();
            getActivity().setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        form = Preferences.loadForm(getContext());
        count = view.findViewById(R.id.camera_image_count);
        previewView = view.findViewById(R.id.camera_preview);
        recentImage = view.findViewById(R.id.recent_image);
        paint_me = view.findViewById(R.id.paint_me);
        sent = view.findViewById(R.id.sent_message);

        view.post(new Runnable() {
            @Override
            public void run() {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        });

        for (int id : new int[]{R.id.capture, R.id.save, R.id.undo, R.id.sent_message}) {
            ((Button) view.findViewById(id)).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.capture:
                setImageForDraw(view.getRootView());
                view.getRootView().findViewById(R.id.paint_me_fade).setVisibility(View.VISIBLE);

                runFadeAnim(view.getRootView(), new int[]{R.id.sent_message_fade}, 0, 300, false);
                runFadeAnim(view.getRootView(), new int[]{R.id.capture_fade}, 0, 300, false);
                runFadeAnim(view.getRootView(), new int[]{R.id.mode_draw_fade}, 0, 300, true);
                break;

            case R.id.save:
                runFadeAnim(view.getRootView(), new int[]{R.id.sent_message_fade}, 0, 300, true);
                runFadeAnim(view.getRootView(), new int[]{R.id.capture_fade}, 0, 300, true);
                runFadeAnim(view.getRootView(), new int[]{R.id.undo_fade}, 0, 300, false);
                runFadeAnim(view.getRootView(), new int[]{R.id.mode_draw_fade}, 0, 300, false);

                savePaintedToDisk(view.getRootView(), new File(file.getPath()));

                Glide.with(getContext())
                        .load(paint_me.getDrawingCache())
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade(200))
                        .into(recentImage);

                count.setText("+" + number++);

                zoomImageFromThumb(recentImage);
                runFadeAnim(view.getRootView(), new int[]{R.id.paint_me_fade}, 0, 100, false);
                runFadeAnim(view.getRootView(), new int[]{R.id.undo_fade}, 0, 300, false);
                break;

            case R.id.undo:
                undoAction();
                break;

            case R.id.sent_message:
                if (Preferences.load("HAS_IMAGE", getContext())) {
                    String message = "<body style=\"background-color:#FFFFFF\" ><br><p align=\"left\" style=\"font-family: Consolas,sans-serif; font-size: 16px;\">" +
                            "Hello my name is: <b> " + form.getName().toUpperCase() + "</b>.</p><p align=\"left\" style=\"font-family: Consolas,sans-serif; font-size: 16px;\">" +
                            "I need help with <b>" + form.getServices().toString().replace("[", "").replace("]", "") + "</b>.</p><p align=\"left\" style=\"font-family: Consolas,sans-serif; font-size: 16px;\">" +
                            "I have sent you some photos for estimation.</p><p align=\"left\" style=\"font-family: Consolas,sans-serif; font-size: 16px;\">" +
                            "Please take a look and call me back with a price on <b>" + form.getPhone() + "</b>. Thank you.</p><br><hr style=\"margin: -1em\"><br><p align=\"left\" style=\"font-family: Consolas,sans-serif; font-size: 16px;\">Generated in Optimus Klus app</p></body>";

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Email.sendEmail("optklus.clients@gmail.com", "APP ORDER - " + getRandomNumberString(), message, form.getFiles());
                            Navigation.findNavController(view)
                                    .navigate(R.id.action_camera_preview_to_main);

                            new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogTheme)
                                    .setView(getLayoutInflater().inflate(R.layout.camera_dialog, null))
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).create().show();
                        }
                    });
                }
                break;
        }
    }

    public static String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(9999);

        return String.format("%04d", number);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setImageForDraw(@NonNull View view) {
        createFolderIfNotExist();

        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/OptimumKlus/",
                new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()) + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file)
                .build();

        imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        view.findViewById(R.id.blink).setBackgroundResource(R.drawable.camera_blink);

                        Matrix matrix = new Matrix();
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            matrix.postRotate(0);
                        } else {
                            matrix.postRotate(90);
                        }

                        Bitmap bm = BitmapFactory.decodeFile(file.getPath());
                        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

                        paint_me.setImageBitmap(rotatedBitmap);
                        paint_me.setVisibility(View.VISIBLE);
                        paint_me.setDrawingIsEnabled(true);
                        paint_me.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                if (!isFlash) {
                                    runFadeAnim(view.getRootView(), new int[]{R.id.mode_draw_fade}, 0, 300, false);
                                    runFadeAnim(view.getRootView(), new int[]{R.id.undo_fade}, 0, 300, true);
                                    isFlash = !isFlash;
                                }

                                return paint_me.touchEvent(motionEvent);
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException error) {
                error.printStackTrace();
                Log.i(TAG, "onError: ");
            }
        });
    }

    private void undoAction() {
        paint_me.removeLastPath();
    }

    private void savePaintedToDisk(View view, File file) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                paint_me.invalidate();

                Bitmap bitmap = Bitmap.createBitmap(paint_me.getDrawingCache());
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);

                paint_me.removeAllPath();
                try {
                    FileOutputStream fo = new FileOutputStream(file);
                    form.addFiles(file);
                    Preferences.save("HAS_IMAGE", true, getContext());
                    fo.write(bytes.toByteArray());
                    fo.flush();
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createFolderIfNotExist() {
        File pictureFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/OptimumKlus/");
        if (!pictureFolder.exists()) {
            if (!pictureFolder.mkdir()) {
                Log.e(TAG, "Failed to create directory: " + pictureFolder);
            }
        }
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

    private int toSurfaceRotationDegrees(int rotation) {
        switch (rotation) {
            case 0:
                return Surface.ROTATION_0;
            case 90:
                return Surface.ROTATION_90;
            case 180:
                return Surface.ROTATION_180;
            case 270:
                return Surface.ROTATION_270;
        }
        return Surface.ROTATION_90;
    }

    private void zoomImageFromThumb(final View thumbView) {

        final ImageView expandedImageView = thumbView.getRootView().findViewById(
                R.id.paint_me);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        thumbView.getRootView().findViewById(R.id.camera_main)
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

        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        final float startScaleFinal = startScale;

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
        set.setDuration(300);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                thumbView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);

                AnimatorSet set1 = new AnimatorSet();
                set1
                        .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                                startBounds.left, finalBounds.left))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                                startBounds.top, finalBounds.top))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                                startScale, 1f))
                        .with(ObjectAnimator.ofFloat(expandedImageView,
                                View.SCALE_Y, startScale, 1f));
                set1.setDuration(10);
                set1.setInterpolator(new DecelerateInterpolator());
                set1.start();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                thumbView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
            }
        });
        set.start();
    }
}


