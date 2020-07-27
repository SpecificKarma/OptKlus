package com.gmail.specifikarma.optimumklus.UI.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class DrawImageView extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = "DrawableImageView";

    private int color = Color.parseColor("#B94A148C");
    private float width = 12f;
    private List<Pen> mPenList = new ArrayList<Pen>();
    private Activity mHostActivity;
    private boolean mIsDrawingEnabled, mIsSizeChanging;
    private Circle mCircle;
    private int mScreenWidth;

    private class Circle {
        float x, y;
        Paint paint;

        Circle(int color, float x, float y) {
            this.x = x;
            this.y = y;
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(width);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    private class Pen {
        Path path;
        Paint paint;

        Pen(int color, float width) {
            path = new Path();
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(width);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    public DrawImageView(Context context) {
        super(context);
        init(context);
    }

    public DrawImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public DrawImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context context) {
        mPenList.add(new Pen(color, width));
        setDrawingCacheEnabled(true);
        if (context instanceof Activity) {
            mHostActivity = (Activity) context;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mHostActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            mScreenWidth = displayMetrics.widthPixels;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Pen pen : mPenList) {
            canvas.drawPath(pen.path, pen.paint);
        }

        if (mCircle != null) {
            float radius = width / (float) mScreenWidth;
            canvas.drawCircle(mCircle.x, mCircle.y, radius, mCircle.paint);
        }
    }

    public boolean touchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP: {
                removeCircle();
                mIsSizeChanging = false;
                break;
            }
        }

        if (mIsDrawingEnabled) {
            if (event.getPointerCount() > 1) {

                if (!mIsSizeChanging) {
                    mIsSizeChanging = true;
                }

                try {
                    float[] xPositions = new float[2];
                    float[] yPositions = new float[2];
                    float minX = 0;
                    float minY = 0;

                    for (int i = 0; i < event.getPointerCount(); i++) {
                        int pointerId = event.getPointerId(i);
                        if (pointerId == MotionEvent.INVALID_POINTER_ID) {
                            continue;
                        }

                        if (minX == 0 && minY == 0) {
                            minX = event.getX(i);
                            minY = event.getY(i);
                        } else {
                            float tempMinX = event.getX(i);
                            float tempMinY = event.getY(i);
                            if (tempMinX < minX) {
                                minX = tempMinX;
                            }
                            if (tempMinY < minY) {
                                minY = tempMinY;
                            }
                        }

                        xPositions[i] = event.getX(i);
                        yPositions[i] = event.getY(i);
                    }

                    float circleX = (Math.abs(xPositions[1] - xPositions[0]) / 2) + minX;
                    float circleY = (Math.abs(yPositions[1] - yPositions[0]) / 2) + minY;


                    mCircle = new Circle(color, circleX, circleY);

                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "touchEvent: IndexOutOfBoundsException: " + e.getMessage());
                }
            } else {
                if (!mIsSizeChanging) {

                    float eventX = event.getX();
                    float eventY = event.getY();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mPenList.add(new Pen(color, width));
                            mPenList.get(mPenList.size() - 1).path.moveTo(eventX, eventY);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            mPenList.get(mPenList.size() - 1).path.lineTo(eventX, eventY);
                            break;
                        case MotionEvent.ACTION_UP:
                            drawingStopped();
                            break;
                        default:
                            return false;
                    }
                }
            }
        }

        invalidate();
        return true;
    }

    private void drawingStopped() {
        removeCircle();
    }

    private void removeCircle() {
        mCircle = null;
    }

    public void removeLastPath() {
        if (mPenList.size() > 0) {
            mPenList.remove(mPenList.size() - 1);

            invalidate();
        }
    }

    public void removeAllPath() {
        if (mPenList.size() > 0) {
            mPenList.clear();

            invalidate();
        }
    }

    public void setDrawingIsEnabled(boolean bool) {
        mIsDrawingEnabled = bool;
    }
}








