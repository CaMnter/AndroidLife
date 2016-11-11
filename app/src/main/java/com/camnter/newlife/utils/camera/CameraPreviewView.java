package com.camnter.newlife.utils.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Description：CameraPreviewView
 * Created by：CaMnter
 */

public class CameraPreviewView extends SurfaceView implements SurfaceHolder.Callback {

    public static final float RECT_WIDTH_HEIGHT_RATIO = 157.33f / 98.0f;
    private static final float SCREEN_RECT_WIDTH_RATIO = 194.0f / 140.33f;
    private static final float SCREEN_RECT_HEIGHT_RATIO = 124.0f / 98.0f;
    // picture 1280 x 960
    // SCREEN_RECT_HEIGHT_RATIO / 5.0f * 6.0f
    private static final float PICTURE_RECT_HEIGHT_RATIO = 1.5183674f;
    private static final int DEFAULT_CORNER_COLOR = 0xff2E336D;
    // corner dp
    private static final float DEFAULT_CORNER_STROKE = 3.0f;
    private static final float DEFAULT_CORNER_LENGTH = 16.6f;
    private float screenWidth;
    private float screenHeight;
    private Paint rectPaint;
    private Paint cornerPaint;
    private DisplayMetrics metrics;
    // corner px
    private float cornerStroke;
    private float cornerLength;

    private int[] rectWidthHeight;

    private Rect rect;

    private volatile boolean runningState = false;
    private SurfaceThread surfaceThread;
    private SurfaceHolder surfaceHolder;


    public CameraPreviewView(Context context) {
        this(context, null);
    }


    public CameraPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }


    private void init() {
        this.metrics = this.getResources().getDisplayMetrics();

        this.cornerStroke = this.dp2px(DEFAULT_CORNER_STROKE);
        this.cornerLength = this.dp2px(DEFAULT_CORNER_LENGTH);

        this.initSurfaceHolder();
        this.initRectPaint();
        this.initCornerPaint();
    }


    private void initSurfaceHolder() {
        this.surfaceHolder = this.getHolder();
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        this.setZOrderOnTop(true);
        this.setKeepScreenOn(true);
    }


    /**
     * 透过 CLEAR 将 SurfaceView 的 background 和 矩形范围颜色消除
     */
    private void initRectPaint() {
        this.rectPaint = new Paint();
        this.rectPaint.setAntiAlias(true);
        this.rectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }


    private void initCornerPaint() {
        this.cornerPaint = new Paint();
        this.cornerPaint.setColor(DEFAULT_CORNER_COLOR);
        this.cornerPaint.setStrokeWidth(this.cornerStroke);
        this.cornerPaint.setStrokeCap(Paint.Cap.ROUND);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.screenWidth = this.getWidth();
        this.screenHeight = this.getHeight();
        this.rectWidthHeight = this.getRectWidthHeight();
        this.initRect();
        this.start();
    }


    private void initRect() {
        this.rect = new Rect();
        if (this.rectWidthHeight == null) return;
        final int rectMarginLeft = (int) (this.screenWidth / 2 -
            ((float) this.rectWidthHeight[0]) / 2);
        final int rectMarginTop = (int) (this.screenHeight / 2 -
            ((float) this.rectWidthHeight[1]) / 2);
        this.rect.left = rectMarginLeft;
        this.rect.top = rectMarginTop;
        this.rect.right = rectMarginLeft + rectWidthHeight[0];
        this.rect.bottom = rectMarginTop + rectWidthHeight[1];
    }


    private int[] getRectWidthHeight() {
        return this.screenWidth > 0 ? new int[] {
            (int) (this.screenWidth / SCREEN_RECT_WIDTH_RATIO),
            (int) (this.screenWidth / SCREEN_RECT_WIDTH_RATIO / RECT_WIDTH_HEIGHT_RATIO) } : null;
    }


    private void start() {
        if (this.runningState) return;
        this.surfaceThread = new SurfaceThread();
        this.surfaceThread.start();
        this.runningState = true;
    }


    public Bitmap cropToPreviewBitmap(@NonNull final Bitmap originalBitmap) {
        final float originalWidth = originalBitmap.getWidth();
        final float originalHeight = originalBitmap.getHeight();
        final float expectWidth = originalWidth / SCREEN_RECT_WIDTH_RATIO;
        final float expectHeight = originalHeight / PICTURE_RECT_HEIGHT_RATIO;
        final float leftTopX = originalWidth / 2 - expectWidth / 2;
        final float leftTopY = originalHeight / 2 - expectHeight / 2;
        return Bitmap.createBitmap(originalBitmap, (int) leftTopX, (int) leftTopY,
            (int) expectWidth, (int) expectHeight, null, false);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.stop();
    }


    private void stop() {
        if (!this.runningState || this.surfaceThread == null) return;
        this.surfaceThread.interrupt();
        this.surfaceThread = null;
        this.runningState = false;
    }


    /**
     * Dp to px
     *
     * @param dp dp
     * @return px
     */
    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.metrics);
    }


    private class SurfaceThread extends Thread {

        @Override
        public void run() {
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                canvas.drawARGB(100, 0, 0, 0);
                canvas.drawRect(rect, rectPaint);

                float cornerHalfStroke = cornerStroke / 2;

                // leftTop >> h + v
                canvas.drawLine(rect.left - cornerHalfStroke, rect.top - cornerHalfStroke,
                    rect.left - cornerHalfStroke + cornerLength, rect.top - cornerHalfStroke,
                    cornerPaint);
                canvas.drawLine(rect.left - cornerHalfStroke, rect.top - cornerHalfStroke,
                    rect.left - cornerHalfStroke, rect.top - cornerHalfStroke + cornerLength,
                    cornerPaint);

                // leftBottom >> h + v
                canvas.drawLine(rect.left - cornerHalfStroke, rect.bottom + cornerHalfStroke,
                    rect.left - cornerHalfStroke + cornerLength, rect.bottom + cornerHalfStroke,
                    cornerPaint);
                canvas.drawLine(rect.left - cornerHalfStroke, rect.bottom + cornerHalfStroke,
                    rect.left - cornerHalfStroke, rect.bottom + cornerHalfStroke - cornerLength,
                    cornerPaint);

                // rightTop >> h + v
                canvas.drawLine(rect.right + cornerHalfStroke, rect.top - cornerHalfStroke,
                    rect.right + cornerHalfStroke - cornerLength, rect.top - cornerHalfStroke,
                    cornerPaint);
                canvas.drawLine(rect.right + cornerHalfStroke, rect.top - cornerHalfStroke,
                    rect.right + cornerHalfStroke, rect.top - cornerHalfStroke + cornerLength,
                    cornerPaint);

                // rightBottom >> h + v
                canvas.drawLine(rect.right + cornerHalfStroke, rect.bottom + cornerHalfStroke,
                    rect.right + cornerHalfStroke - cornerLength, rect.bottom + cornerHalfStroke,
                    cornerPaint);
                canvas.drawLine(rect.right + cornerHalfStroke, rect.bottom + cornerHalfStroke,
                    rect.right + cornerHalfStroke, rect.bottom + cornerHalfStroke - cornerLength,
                    cornerPaint);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
