package com.bhargavms.parallaximageview;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * A layer of the parallaxview.
 */
class Layer {
    private static final float PAN_FACTOR = 0.2f;
    private int mDrawableRes;
    private int mPosition;
    private int mIntrinsicWidth;
    private int mIntrinsicHeight;
    private int mRequiredWidth;
    private int mRequiredHeight;
    private Resources mRes;
    private FastBitmapDrawable mDrawable;
    private Handler mainThreadHandler;
    private OnBitmapLoadUpdateListener mListener;
    private Matrix mDisplayMatrix;

    public Layer(int drawableRes, int position, Resources res,
                 @NonNull OnBitmapLoadUpdateListener listener) {
        mDrawableRes = drawableRes;
        mPosition = position;
        mRes = res;
        mainThreadHandler = new Handler(Looper.getMainLooper());
        mListener = listener;
        mDisplayMatrix = new Matrix();
        init();
    }

    public void draw(Canvas canvas) {
        if (mDrawable == null)
            return;
        int saveCount = canvas.getSaveCount();
        canvas.save();
        if (mDisplayMatrix != null && !mDisplayMatrix.isIdentity())
            canvas.concat(mDisplayMatrix);
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    void resetPan() {
        mDisplayMatrix.reset();
    }

    void pan(float dx, float dy) {
        if (dx > 0)
            dx = dx - (dx / mPosition);
        else
            dx = dx + (dx / mPosition);

        if (dy > 0)
            dy = dy - (dy / mPosition);
        else
            dy = dy + (dy / mPosition);

        mDisplayMatrix.postTranslate(dx, dy);
    }

    private void init() {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mRes, mDrawableRes, o);
        mIntrinsicHeight = o.outHeight;
        mIntrinsicWidth = o.outWidth;
    }

    public void onMeasure(int width, int height) {
        mRequiredWidth = Math.round(width + (width * PAN_FACTOR));
        mRequiredHeight = Math.round(height + (height * PAN_FACTOR));
        startLoadingBitmap();
    }

    private void startLoadingBitmap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadBitmap();
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onDoneLoading();
                    }
                });
            }
        }).start();
    }

    /**
     * Made to run in a non-main thread. A slow operation.
     */
    private void loadBitmap() {
        final BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = calculateInSampleSize();
        o.inJustDecodeBounds = false;
        // todo: Set this configurable if user wants to use a state based bitmap drawable to enable mirroring for RTL layouts.
        mDrawable = new FastBitmapDrawable(BitmapFactory.decodeResource(mRes, mDrawableRes, o));
    }

    private int calculateInSampleSize() {
        // Raw height and width of image
        final int height = mIntrinsicHeight;
        final int width = mIntrinsicWidth;
        int inSampleSize = 1;

        if (height > mRequiredHeight || width > mRequiredWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= mRequiredHeight
                    && (halfWidth / inSampleSize) >= mRequiredWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

//    public void setCenter(float fx, float fy) {
//        int x = Math.round(fx);
//        int y = Math.round(fy);
//        int height = mBounds.bottom - mBounds.top;
//        int width = mBounds.right - mBounds.left;
//        int top = y - (height / 2);
//        int bottom = y + (height / 2);
//        int left = x - (width / 2);
//        int right = x + (width / 2);
//        mBounds.set(left, top, right, bottom);
//    }

    public int getDrawableRes() {
        return mDrawableRes;
    }

    public void setDrawableRes(int mDrawableRes) {
        this.mDrawableRes = mDrawableRes;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

//    public Rect getBounds() {
//        return mBounds;
//    }
//
//    public void setBounds(int left, int top, int right, int bottom) {
//        mBounds.set(left, top, right, bottom);
//    }
//
//    public void setBounds(Rect r) {
//        mBounds.set(r);
//    }

    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    public int getRequiredWidth() {
        return mRequiredWidth;
    }

    public int getRequiredHeight() {
        return mRequiredHeight;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    interface OnBitmapLoadUpdateListener {
        void onDoneLoading();
    }
}
