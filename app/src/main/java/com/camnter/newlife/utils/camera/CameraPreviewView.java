package com.camnter.newlife.utils.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.camnter.newlife.R;
import com.camnter.newlife.utils.DeviceUtils;

/**
 * Description：CameraPreviewView
 * Created by：CaMnter
 */

public class CameraPreviewView extends SurfaceView implements SurfaceHolder.Callback {

    // 1080.0f / 835.2f
    private static final float SCREEN_RECT_WIDTH_RATIO = 1.2931035f;
    // 1488.0f / 1224.0f
    private static final float SCREEN_RECT_HEIGHT_RATIO = 1.2156863f;

    // 1080.0f / 72.0f
    private static final float SCREEN_WIDTH_RECT_MARGIN_LEFT_RATIO = 15.0f;

    // 1224.0f / 634.97f
    private static final float RECT_HEIGHT_FRONT_IMAGE_MARGIN_TOP_RATIO = 1.9276502f;
    // 1224.0f / 165.6f
    private static final float RECT_HEIGHT_REVERSE_IMAGE_MARGIN_TOP_RATIO = 7.391304f;
    // 1080.0f / 421.92f
    private static final float RECT_HEIGHT_REVERSE_IMAGE_MARGIN_LEFT_RATIO = 2.559727f;

    private static final String PROMPT_CONTENT = "将身份证正面放入框中，并对准头像";
    private static final float PROMPT_SIZE = 13.4f;

    private static final int DEFAULT_PROMPT_COLOR = 0xffFFFFFF;
    private static final int DEFAULT_CORNER_COLOR = 0xffFFFFFF;

    // corner dp
    private static final float DEFAULT_CORNER_STROKE = 2.4f;
    private static final float DEFAULT_CORNER_LENGTH = 15.4f;
    private float screenWidth;
    private float screenHeight;
    private Paint rectPaint;
    private Paint cornerPaint;
    private TextPaint promptPaint;
    private DisplayMetrics metrics;
    // corner px
    private float cornerStroke;
    private float cornerLength;

    private float promptWidth;

    private int[] rectWidthHeight;

    private Rect rect;

    private volatile boolean runningState = false;
    private SurfaceThread surfaceThread;
    private SurfaceHolder surfaceHolder;

    @IdCardCameraActivity.PromptViewType
    private int promptViewType;

    private PreviewListener previewListener;


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


    public void setPromptViewType(@IdCardCameraActivity.PromptViewType final int promptViewType) {
        this.promptViewType = promptViewType;
    }


    private void init() {
        this.metrics = this.getResources().getDisplayMetrics();

        this.cornerStroke = this.dp2px(DEFAULT_CORNER_STROKE);
        this.cornerLength = this.dp2px(DEFAULT_CORNER_LENGTH);

        this.initSurfaceHolder();
        this.initRectPaint();
        this.initCornerPaint();
        this.initPromptPaint();
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


    private void initPromptPaint() {
        this.promptPaint = new TextPaint();
        final float promptSize = DeviceUtils.dp2px(this.getContext(), PROMPT_SIZE);

        this.promptPaint.setAntiAlias(true);
        this.promptPaint.setColor(DEFAULT_PROMPT_COLOR);
        this.promptPaint.setTextSize(promptSize);
        this.promptPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        this.promptWidth = Layout.getDesiredWidth(PROMPT_CONTENT, promptPaint);
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
        final int rectMarginLeft = (int) (this.screenWidth / SCREEN_WIDTH_RECT_MARGIN_LEFT_RATIO);
        final int rectMarginTop = (int) (this.screenHeight / 2 -
            ((float) this.rectWidthHeight[1]) / 2);
        this.rect.left = rectMarginLeft;
        this.rect.top = rectMarginTop;
        this.rect.right = rectMarginLeft + this.rectWidthHeight[0];
        this.rect.bottom = rectMarginTop + this.rectWidthHeight[1];

        if (this.previewListener != null) {
            this.handlePreviewListener(this.previewListener);
        }
    }


    private void handlePreviewListener(@NonNull final PreviewListener previewListener) {
        switch (this.promptViewType) {
            case IdCardCameraActivity.PROMPT_FRONT:
                final int frontImageViewWidth = this.getResources()
                    .getDimensionPixelOffset(R.dimen.id_card_front_image_width);
                final int frontImageMarginTop = this.rect.top +
                    (int) (this.rectWidthHeight[1] / RECT_HEIGHT_FRONT_IMAGE_MARGIN_TOP_RATIO);
                final int frontImageMarginLeft = this.rect.left +
                    (int) (this.rectWidthHeight[0] / 2.0f - frontImageViewWidth / 2.0f);
                previewListener.notificationFrontImageView(frontImageMarginTop,
                    frontImageMarginLeft);
                break;
            case IdCardCameraActivity.PROMPT_REVERSE:
                final int reverseImageMarginTop = this.rect.top +
                    (int) (this.rectWidthHeight[1] / RECT_HEIGHT_REVERSE_IMAGE_MARGIN_TOP_RATIO);
                final int reverseImageMarginLeft = this.rect.left +
                    (int) (this.rectWidthHeight[1] / RECT_HEIGHT_REVERSE_IMAGE_MARGIN_LEFT_RATIO);
                previewListener.notificationReverseImageView(reverseImageMarginTop,
                    reverseImageMarginLeft);
                break;
        }
    }


    public void setPreviewListener(PreviewListener previewListener) {
        this.previewListener = previewListener;
    }


    private int[] getRectWidthHeight() {
        return this.screenWidth > 0 ? new int[] {
            (int) (this.screenWidth / SCREEN_RECT_WIDTH_RATIO),
            (int) (this.screenHeight / SCREEN_RECT_HEIGHT_RATIO) } : null;
    }


    private void start() {
        if (this.runningState) return;
        this.surfaceThread = new SurfaceThread();
        this.surfaceThread.start();
        this.runningState = true;
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

                /********
                 * 框框 *
                 ********/
                canvas.save();

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

                canvas.restore();

                /********
                 * 文字 *
                 ********/

                canvas.save();

                // X = rect.marginLeft + rectWidth + rect.marginLeft
                // Y = screenHeight / 2 - 文字宽度 / 2
                drawTextRotate(canvas, PROMPT_CONTENT, rect.left * 2 + rectWidthHeight[0],
                    screenHeight / 2.0f - promptWidth / 2.0f, promptPaint, 90);

                canvas.restore();

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


        private void drawTextRotate(@NonNull final Canvas canvas,
                                    @NonNull final String text,
                                    final float x,
                                    final float y,
                                    @NonNull final Paint paint,
                                    final float angle) {
            if (angle != 0) {
                canvas.rotate(angle, x, y);
            }
            canvas.drawText(text, x, y, paint);
            if (angle != 0) {
                canvas.rotate(-angle, x, y);
            }
        }

    }


    public interface PreviewListener {
        void notificationFrontImageView(final int frontImageMarginTop,
                                        final int frontImageMarginLeft);

        void notificationReverseImageView(final int reverseImageMarginTop,
                                          final int reverseImageMarginLeft);
    }

}
