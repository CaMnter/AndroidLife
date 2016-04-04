package com.camnter.newlife.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import com.camnter.newlife.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Description：XfermodeRoundImageView
 * Created by：CaMnter
 * Time：2016-03-02 16:30
 */
public class XfermodeRoundImageView extends ImageView {

    public static final int ROUND = 2601;
    public static final int CIRCLE = 2602;

    @IntDef({ ROUND, CIRCLE }) @Retention(RetentionPolicy.SOURCE) public @interface ImageType {}

    private static final int DEFAULT_BORDER_RADIUS = 8;

    @ImageType private int imageType;

    private int mBorderRadius;
    private Paint mBitmapPaint;
    private Bitmap mMaskBitmap;
    private Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    private WeakReference<Bitmap> mWeakReference;


    public XfermodeRoundImageView(Context context) {
        super(context);
        this.init(context, null);
    }


    public XfermodeRoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }


    public XfermodeRoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public XfermodeRoundImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        this.mBitmapPaint = new Paint();
        this.mBitmapPaint.setAntiAlias(true);

        if (attrs == null) return;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.XfermodeRoundImageView);
        this.imageType =
                typedArray.getInt(R.styleable.XfermodeRoundImageView_xfermodeImageType, CIRCLE) ==
                        CIRCLE ? CIRCLE : ROUND;
        this.mBorderRadius = typedArray.getDimensionPixelSize(
                R.styleable.XfermodeRoundImageView_xfermodeBorderRadius,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BORDER_RADIUS,
                        this.getResources().getDisplayMetrics()));
        typedArray.recycle();
    }


    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.imageType == CIRCLE) {
            int side = Math.min(this.getMeasuredWidth(), this.getMeasuredHeight());
            this.setMeasuredDimension(side, side);
        }
    }


    @Override protected void onDraw(Canvas canvas) {
        Bitmap bitmap = this.mWeakReference == null ? null : this.mWeakReference.get();
        if (bitmap == null || bitmap.isRecycled()) {
            Drawable drawable = this.getDrawable();
            if (drawable == null) {
                super.onDraw(canvas);
                return;
            }
            bitmap = this.drawableToBitmap(drawable, this.imageType);
            if (this.mMaskBitmap == null || this.mMaskBitmap.isRecycled()) {
                this.mMaskBitmap = this.drawBitmapSafely(this.getWidth(), this.getHeight(),
                        Bitmap.Config.ARGB_8888, 1);
            }
            this.mWeakReference = new WeakReference<>(bitmap);
        }
        if (bitmap != null) {
            int sc = canvas.saveLayer(0, 0, this.getWidth(), this.getHeight(), null,
                    Canvas.MATRIX_SAVE_FLAG |
                            Canvas.CLIP_SAVE_FLAG |
                            Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                            Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                            Canvas.CLIP_TO_LAYER_SAVE_FLAG);
            this.mBitmapPaint.reset();
            this.mBitmapPaint.setFilterBitmap(false);
            canvas.drawBitmap(this.mMaskBitmap, 0.0f, 0.0f, this.mBitmapPaint);
            this.mBitmapPaint.setXfermode(this.mXfermode);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, this.mBitmapPaint);
            this.mBitmapPaint.setXfermode(null);
            canvas.restoreToCount(sc);
        } else {
            this.mBitmapPaint.reset();
            super.onDraw(canvas);
        }
    }


    @Override public void invalidate() {
        this.mWeakReference = null;
        if (this.mMaskBitmap != null) {
            this.mMaskBitmap.recycle();
            this.mMaskBitmap = null;
        }
        super.invalidate();
    }


    private Bitmap drawableToBitmap(Drawable drawable, @ImageType int imageType) {
        if (drawable instanceof BitmapDrawable) ((BitmapDrawable) drawable).getBitmap();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        float scale = 1.0f;
        Bitmap bitmap = this.createBitmapSafely(width, height, Bitmap.Config.ARGB_8888, 1);
        Canvas canvas = new Canvas(bitmap);
        switch (imageType) {
            case ROUND: {
                scale = Math.max(this.getWidth() * 1.0f / width, getHeight() * 1.0f / height);
                break;
            }
            case CIRCLE: {
                scale = this.getWidth() * 1.0f / Math.min(width, height);
                break;
            }
        }
        drawable.setBounds(0, 0, (int) (scale * width), (int) (scale * height));
        drawable.draw(canvas);
        return bitmap;
    }


    public Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }


    public Bitmap drawBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            switch (this.imageType) {
                case ROUND:
                    canvas.drawRoundRect(new RectF(0, 0, this.getWidth(), getHeight()),
                            this.mBorderRadius, this.mBorderRadius, paint);
                    break;
                case CIRCLE:
                    canvas.drawCircle(this.getWidth() / 2, this.getWidth() / 2, this.getWidth() / 2,
                            paint);
                    break;
            }
            return bitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (retryCount > 0) {
                System.gc();
                return drawBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }
}
