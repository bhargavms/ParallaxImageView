package com.bhargavms.parallaximageview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * A view with a parallax effect.
 */
public class ParallaxImageView extends View implements Layer.OnBitmapLoadUpdateListener {
    private List<Layer> mLayers;
    private ParallaxImageLoadCallbacks mImageLoadCallback;
    private boolean mTouchEnabled = false;
    private int mImageLoadedCounter = 0;
    private float mTouchStartX;
    private float mTouchStartY;

    public ParallaxImageView(Context context) {
        super(context);
    }

    public ParallaxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setDrawables(int[] drawableResIds) {
        int size = drawableResIds.length;
        mLayers = new ArrayList<>(size);
        Resources res = getResources();
        for (int i = 0; i < size; i++) {
            mLayers.add(i, new Layer(drawableResIds[i], i, res, this));
        }
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Layer layer : mLayers) {
            layer.draw(canvas);
        }
    }

    public void setImageLoadCallback(ParallaxImageLoadCallbacks callback) {
        mImageLoadCallback = callback;
    }

    private Layer getLargestLayer() {
        int largestArea = 0;
        Layer largestLayer = null;
        for (Layer layer : mLayers) {
            int height = layer.getIntrinsicHeight();
            int width = layer.getIntrinsicWidth();
            int area = height * width;
            if (area > largestArea) {
                largestArea = area;
                largestLayer = layer;
            }
        }
        return largestLayer;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mTouchEnabled)
            return false;
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // touch started
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // moving
                float dx = mTouchStartX - event.getX();
                float dy = mTouchStartY - event.getY();
                for (Layer layer : mLayers) {
                    layer.pan(dx, dy);
                    invalidateDrawable(layer.getDrawable());
                }
                break;
            case MotionEvent.ACTION_UP:
                resetLayers();
        }
        return true;
    }

    private void resetLayers() {
        for (Layer layer : mLayers) {
            layer.resetPan();
            invalidateDrawable(layer.getDrawable());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        Layer largestLayer = getLargestLayer();
        int desiredHeight = 100;
        if (largestLayer != null)
            desiredHeight = largestLayer.getIntrinsicHeight();

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        int desiredWidth = 100;
        if (largestLayer != null)
            desiredWidth = largestLayer.getIntrinsicWidth();
        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }
        if (mLayers != null && !mLayers.isEmpty()) {
            mImageLoadCallback.onStartedLoading();
            for (Layer layer : mLayers) {
                layer.onMeasure(width, height);
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void onDoneLoading() {
        mImageLoadedCounter++;
        if (mImageLoadedCounter == mLayers.size()) {
            mTouchEnabled = true;
            mImageLoadCallback.onDoneLoading();
        }
    }
}
