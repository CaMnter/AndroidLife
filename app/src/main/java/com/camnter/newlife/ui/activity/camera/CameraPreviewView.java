package com.camnter.newlife.ui.activity.camera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.camnter.newlife.R;
import com.camnter.utils.BitmapUtils;
import com.camnter.utils.DeviceUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import static com.camnter.newlife.ui.activity.camera.IdCardCameraActivity.PROMPT_FRONT;

/**
 * Description：CameraPreviewView
 * Created by：CaMnter
 */

public class CameraPreviewView extends SurfaceView implements SurfaceHolder.Callback {

    // 1080.0f / 792.0f
    private static final float SCREEN_RECT_WIDTH_RATIO = 1.3636364f;
    // 1488.0f / 1224.0f
    private static final float SCREEN_RECT_HEIGHT_RATIO = 1.2156863f;

    // 1080.0f / 100.8f
    private static final float SCREEN_WIDTH_RECT_MARGIN_LEFT_RATIO = 10.714286f;
    // 1080.0f / 64.9f
    private static final float SCREEN_WIDTH_RECT_MARGIN_RIGHT_RATIO = 16.640985f;

    // 1224.0f / 634.97f
    private static final float RECT_HEIGHT_FRONT_IMAGE_MARGIN_TOP_RATIO = 1.9276502f;
    // 1224.0f / 165.6f
    private static final float RECT_HEIGHT_REVERSE_IMAGE_MARGIN_TOP_RATIO = 7.391304f;
    // 1080.0f / 421.92f
    private static final float RECT_HEIGHT_REVERSE_IMAGE_MARGIN_LEFT_RATIO = 2.559727f;

    private static final String PROMPT_CONTENT_FRONT = "将身份证正面 对准边框和头像";
    private static final String PROMPT_CONTENT_REVERSE = "将身份证背面 对准边框和国徽";
    private static final float PROMPT_SIZE = 13.4f;

    private static final int DEFAULT_PROMPT_COLOR = 0xffFFFFFF;
    private static final int DEFAULT_CORNER_COLOR = 0xffFFFFFF;

    public static final int DRAW_MODE_BY_SELF = 0x261;
    public static final int DRAW_MODE_BY_DRAWABLE = 0x262;


    @IntDef({ DRAW_MODE_BY_SELF, DRAW_MODE_BY_DRAWABLE })
    @Retention(RetentionPolicy.SOURCE)
    private @interface DrawMode {
    }


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
    private String promptTipContent = "";

    @DrawMode
    private int drawMode;
    private DrawProxy drawProxy;

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


    public void setPromptViewType(final int promptViewType) {
        this.promptViewType = promptViewType;
        this.initPromptPaint();
    }


    public void setDrawMode(@DrawMode final int drawMode) {
        this.drawMode = drawMode;
        if (this.drawProxy == null) {
            this.drawProxy = new DrawProxy(this.getContext(), this.drawMode, this.promptViewType);
            return;
        }
        this.drawProxy.setDrawMode(this.drawMode);
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

        this.promptWidth = Layout.getDesiredWidth(this.promptTipContent, promptPaint);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.screenWidth = this.getWidth();
        this.screenHeight = this.getHeight();
        this.rectWidthHeight = this.drawProxy.getRectWidthHeightProxy();
        this.start();
    }


    private void handlePreviewListener(@NonNull final PreviewListener previewListener,
                                       final int[] rectWidthHeight) {
        switch (this.promptViewType) {
            case PROMPT_FRONT:
                final int frontImageViewWidth = this.getResources()
                    .getDimensionPixelOffset(R.dimen.id_card_front_image_width);
                final int frontImageMarginTop = this.rect.top +
                    (int) (rectWidthHeight[1] / RECT_HEIGHT_FRONT_IMAGE_MARGIN_TOP_RATIO);
                final int frontImageMarginLeft = this.rect.left +
                    (int) (rectWidthHeight[0] / 2.0f - frontImageViewWidth / 2.0f);
                previewListener.notificationFrontImageView(frontImageMarginTop,
                    frontImageMarginLeft);
                break;
            case IdCardCameraActivity.PROMPT_REVERSE:
                final int reverseImageMarginTop = this.rect.top +
                    (int) (rectWidthHeight[1] / RECT_HEIGHT_REVERSE_IMAGE_MARGIN_TOP_RATIO);
                final int reverseImageMarginLeft = this.rect.left +
                    (int) (rectWidthHeight[1] / RECT_HEIGHT_REVERSE_IMAGE_MARGIN_LEFT_RATIO);
                previewListener.notificationReverseImageView(reverseImageMarginTop,
                    reverseImageMarginLeft);
                break;
        }
    }


    public void setPreviewListener(PreviewListener previewListener) {
        this.previewListener = previewListener;
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


    public interface PreviewListener {
        void notificationFrontImageView(final int frontImageMarginTop,
                                        final int frontImageMarginLeft);

        void notificationReverseImageView(final int reverseImageMarginTop,
                                          final int reverseImageMarginLeft);
    }


    private class SurfaceThread extends Thread {

        @Override
        public void run() {
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();

                /***************
                 * 抽离透明区域 *
                 ***************/
                canvas.save();
                canvas.drawARGB(100, 0, 0, 0);
                canvas.drawRect(rect, rectPaint);
                canvas.restore();

                /*************
                 * 身份证图片 *
                 *************/
                if (drawMode == DRAW_MODE_BY_DRAWABLE) {
                    canvas.save();
                    drawProxy.drawIdCardContourProxy(canvas);
                    canvas.save();
                    canvas.restore();
                }

                /********
                 * 边框 *
                 ********/
                canvas.save();
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
                drawTextRotate(canvas,
                    promptTipContent,
                    rect.left + (int) (screenWidth / SCREEN_WIDTH_RECT_MARGIN_RIGHT_RATIO) +
                        rectWidthHeight[0],
                    screenHeight / 2.0f - promptWidth / 2.0f,
                    promptPaint, 90);
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


    private final class DrawProxy {

        @DrawMode
        private int drawMode;
        @IdCardCameraActivity.PromptViewType
        private final int promptViewType;

        @DrawableRes
        private static final int ID_CARD_FRONT_CONTOUR = R.drawable.bg_id_card_front_contour;
        @DrawableRes
        private static final int ID_CARD_REVERSE_CONTOUR = R.drawable.bg_id_card_reverse_contour;

        private static final int DEFAULT_RETRY_COUNT = 3;

        private WeakReference<Context> contextReference;
        private Bitmap expectBitmap;


        public DrawProxy(@NonNull final Context context,
                         @DrawMode final int drawMode,
                         @IdCardCameraActivity.PromptViewType final int promptViewType) {
            this.drawMode = drawMode;
            this.promptViewType = promptViewType;
            this.contextReference = new WeakReference<>(context);
        }


        private void setDrawMode(@DrawableRes final int drawMode) {
            this.drawMode = drawMode;
        }


        @Nullable
        private int[] getRectWidthHeightProxy() {
            final int[] rectWidthHeight = new int[2];
            if (screenWidth <= 0) return rectWidthHeight;
            // width
            rectWidthHeight[0] = (int) (screenWidth / SCREEN_RECT_WIDTH_RATIO);
            rect = new Rect();
            final int rectMarginLeft = (int) (screenWidth / SCREEN_WIDTH_RECT_MARGIN_LEFT_RATIO);
            switch (this.drawMode) {
                case DRAW_MODE_BY_SELF: {
                    // height
                    rectWidthHeight[1] = (int) (screenHeight / SCREEN_RECT_HEIGHT_RATIO);
                    final int rectMarginTop = (int) (screenHeight / 2 -
                        ((float) rectWidthHeight[1]) / 2);
                    rect.left = rectMarginLeft;
                    rect.top = rectMarginTop;
                    rect.right = rectMarginLeft + rectWidthHeight[0];
                    rect.bottom = rectMarginTop + rectWidthHeight[1];
                    if (previewListener != null) {
                        handlePreviewListener(previewListener, rectWidthHeight);
                    }
                    break;
                }
                case DRAW_MODE_BY_DRAWABLE: {
                    this.initExpectBitmap(rectWidthHeight[0]);
                    if (this.expectBitmap == null) return rectWidthHeight;
                    final int expectBitmapWidth = this.expectBitmap.getWidth();
                    final int expectBitmapHeight = this.expectBitmap.getHeight();
                    // height
                    rectWidthHeight[1] = expectBitmapHeight;
                    final int rectMarginTop = (int) (screenHeight / 2 -
                        ((float) expectBitmapHeight) / 2);
                    rect.left = rectMarginLeft;
                    rect.top = rectMarginTop;
                    rect.right = rectMarginLeft + expectBitmapWidth;
                    rect.bottom = rectMarginTop + expectBitmapHeight;
                    break;
                }
            }
            return rectWidthHeight;
        }


        private void drawIdCardContourProxy(@NonNull final Canvas canvas) {
            canvas.drawBitmap(this.expectBitmap, rect.left, rect.top, null);
        }


        private void initExpectBitmap(final int newWidth) {
            @DrawableRes
            final int idCardContour = this.promptViewType == PROMPT_FRONT ?
                                      ID_CARD_FRONT_CONTOUR :
                                      ID_CARD_REVERSE_CONTOUR;
            try {
                final Context context = this.contextReference.get();
                if (context == null) return;
                final Bitmap originalBitmap = this.decodeResourceSafely(
                    context.getResources(),
                    idCardContour,
                    DEFAULT_RETRY_COUNT
                );
                if (originalBitmap == null) return;
                this.expectBitmap = this.getBitmapCompressedByWidthSafely(
                    originalBitmap,
                    newWidth,
                    DEFAULT_RETRY_COUNT
                );
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }


        @Nullable
        private Bitmap decodeResourceSafely(@NonNull final Resources res,
                                            final int drawableRes,
                                            final int retryCount) {
            try {
                return BitmapFactory.decodeResource(res, drawableRes);
            } catch (OutOfMemoryError outOfMemoryError) {
                outOfMemoryError.printStackTrace();
                System.gc();
                if (retryCount <= 0) return null;
                return this.decodeResourceSafely(res, drawableRes, retryCount - 1);
            }
        }


        @Nullable
        private Bitmap getBitmapCompressedByWidthSafely(@NonNull final Bitmap bitmap,
                                                        final double newWidth,
                                                        final int retryCount) {
            try {
                return BitmapUtils.getBitmapCompressedByWidth(bitmap, newWidth);
            } catch (OutOfMemoryError outOfMemoryError) {
                outOfMemoryError.printStackTrace();
                System.gc();
                if (retryCount <= 0) return null;
                return this.getBitmapCompressedByWidthSafely(bitmap, newWidth, retryCount - 1);
            }
        }

    }

}
