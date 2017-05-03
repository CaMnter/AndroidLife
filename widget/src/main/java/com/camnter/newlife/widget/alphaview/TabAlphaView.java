package com.camnter.newlife.widget.alphaview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import com.camnter.newlife.widget.R;

/**
 * Description：TabAlphaView
 * Created by：CaMnter
 */

public class TabAlphaView extends View {

    private Bitmap normalBitmap;
    private Bitmap selectedBitmap;
    private String tabText;
    private int normalColor = 0xFF999999;
    private int selectedColor = 0xFF999999;
    private int tabTextSize = 12;
    private int iconPadding = 5;

    private float alpha;
    private Paint selectedBitmapPaint;
    private Paint textPaint;
    private Paint.FontMetricsInt fontMetricsInt;

    private Rect iconAvailableRect;
    private Rect iconDrawRect;
    private Rect textBoundRect;


    public TabAlphaView(Context context) {
        super(context);
    }


    public TabAlphaView(Context context,
                        @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public TabAlphaView(Context context,
                        @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) public TabAlphaView(Context context,
                                                                         @Nullable
                                                                         final AttributeSet attrs,
                                                                         int defStyleAttr,
                                                                         int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    private void initAttributes(@NonNull Context context,
                                @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TabAlphaView);
        if (typedArray == null) return;

        final BitmapDrawable normalDrawable = (BitmapDrawable) typedArray.getDrawable(
            R.styleable.TabAlphaView_normalDrawable);
        if (normalDrawable != null) this.normalBitmap = normalDrawable.getBitmap();
        final BitmapDrawable selectedDrawable = (BitmapDrawable) typedArray.getDrawable(
            R.styleable.TabAlphaView_selectedDrawable);
        if (selectedDrawable != null) this.selectedBitmap = selectedDrawable.getBitmap();

        this.tabText = typedArray.getString(R.styleable.TabAlphaView_tabText);
        this.tabTextSize = typedArray.getDimensionPixelSize(R.styleable.TabAlphaView_tabTextSize,
            this.tabTextSize);
        this.iconPadding = typedArray.getDimensionPixelSize(R.styleable.TabAlphaView_iconPadding,
            this.iconPadding);
        this.normalColor = typedArray.getColor(R.styleable.TabAlphaView_normalColor,
            this.normalColor);
        this.selectedColor = typedArray.getColor(R.styleable.TabAlphaView_selectedColor,
            this.selectedColor);

        typedArray.recycle();
        this.initRect();
        this.initPaints();
    }


    private void initRect() {
        this.iconAvailableRect = new Rect();
        this.iconDrawRect = new Rect();
        this.textBoundRect = new Rect();
    }


    private void initPaints() {
        this.selectedBitmapPaint = new Paint();
        this.textPaint = new Paint();
        this.textPaint.setTextSize(this.tabTextSize);
        this.textPaint.setAntiAlias(true);
        this.textPaint.setDither(true);

        if (this.tabText == null) return;
        this.textPaint.getTextBounds(this.tabText, 0, this.tabText.length(), this.textBoundRect);
        this.fontMetricsInt = this.textPaint.getFontMetricsInt();
    }


    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (this.tabText == null &&
            (this.normalBitmap == null || this.selectedBitmap == null)) {
            throw new IllegalArgumentException(
                "tabText == null || normalDrawable == null || selectedDrawable == null");
        }

        final int paddingLeft = this.getPaddingLeft();
        final int paddingTop = this.getPaddingTop();
        final int paddingRight = this.getPaddingRight();
        final int paddingBottom = this.getPaddingBottom();
        final int measuredWidth = this.getMeasuredWidth();
        final int measuredHeight = this.getMeasuredHeight();

        int availableWidth = measuredWidth - paddingLeft - paddingRight;
        int availableHeight = measuredHeight - paddingTop - paddingBottom;
        if (this.tabText != null && this.normalBitmap != null) {
            availableHeight -= (this.textBoundRect.height() + iconPadding);
            // 计算出图标可以绘制的画布大小
            this.iconAvailableRect.set(paddingLeft, paddingTop, paddingLeft + availableWidth,
                paddingTop + availableHeight);
            // 计算文字的绘图区域
            int textLeft = paddingLeft + (availableWidth - this.textBoundRect.width()) / 2;
            int textTop = this.iconAvailableRect.bottom + iconPadding;
            this.textBoundRect.set(textLeft, textTop, textLeft + this.textBoundRect.width(),
                textTop + this.textBoundRect.height());
        } else if (this.tabText == null) {
            // 计算出图标可以绘制的画布大小
            this.iconAvailableRect.set(paddingLeft, paddingTop, paddingLeft + availableWidth,
                paddingTop + availableHeight);
        } else if (this.normalBitmap == null) {
            // 计算文字的绘图区域
            int textLeft = paddingLeft + (availableWidth - this.textBoundRect.width()) / 2;
            int textTop = paddingTop + (availableHeight - this.textBoundRect.height()) / 2;
            this.textBoundRect.set(textLeft, textTop, textLeft + this.textBoundRect.width(),
                textTop + this.textBoundRect.height());
        }
    }


    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // setAlpha 必须放在 Paint 的属性最后设置
        final int alpha = (int) Math.ceil(this.alpha * 255);
        Rect drawReact = null;
        if (this.normalBitmap != null) {
            drawReact = this.availableToDrawRect(this.iconAvailableRect, this.normalBitmap);
            this.selectedBitmapPaint.reset();
            this.selectedBitmapPaint.setAntiAlias(true);
            this.selectedBitmapPaint.setFilterBitmap(true);
            this.selectedBitmapPaint.setAlpha(255 - alpha);
            canvas.drawBitmap(this.normalBitmap, null, drawReact, this.selectedBitmapPaint);
        }
        if (this.selectedBitmap != null) {
            if (drawReact == null) {
                drawReact = this.availableToDrawRect(this.iconAvailableRect, this.selectedBitmap);
            }
            this.selectedBitmapPaint.reset();
            this.selectedBitmapPaint.setAntiAlias(true);
            this.selectedBitmapPaint.setFilterBitmap(true);
            this.selectedBitmapPaint.setAlpha(alpha);
            canvas.drawBitmap(this.selectedBitmap, null, drawReact, this.selectedBitmapPaint);
        }
        if (this.tabText != null) {
            this.textPaint.setColor(this.normalColor);
            this.textPaint.setAlpha(255 - alpha);
            // y 轴 坐标代表的是 baseLine 值
            // textBoundRect.height() + fontMetricsInt.bottom  = 字体的高
            // 修正偏移量，将文字向上修正 fontMetricsInt.bottom / 2 = 实现垂直居中
            canvas.drawText(this.tabText, this.textBoundRect.left,
                this.textBoundRect.bottom - this.fontMetricsInt.bottom / 2, this.textPaint);
            this.textPaint.setColor(this.selectedColor);
            this.textPaint.setAlpha(alpha);
            canvas.drawText(this.tabText, this.textBoundRect.left,
                this.textBoundRect.bottom - this.fontMetricsInt.bottom / 2,
                this.textPaint);
        }
    }


    private Rect availableToDrawRect(@NonNull final Rect availableRect,
                                     @NonNull final Bitmap bitmap) {
        float dx = 0, dy = 0;
        final float widthRatio = availableRect.width() * 1.0f / bitmap.getWidth();
        final float heightRatio = availableRect.height() * 1.0f / bitmap.getHeight();
        if (widthRatio > heightRatio) {
            dx = (availableRect.width() - heightRatio * bitmap.getWidth()) / 2;
        } else {
            dy = (availableRect.height() - widthRatio * bitmap.getHeight()) / 2;
        }
        int left = (int) (availableRect.left + dx + 0.5f);
        int top = (int) (availableRect.top + dy + 0.5f);
        int right = (int) (availableRect.right - dx + 0.5f);
        int bottom = (int) (availableRect.bottom - dy + 0.5f);
        this.iconDrawRect.set(left, top, right, bottom);
        return this.iconDrawRect;
    }


    public void setIconAlpha(final float alpha) {
        if (alpha < 0 || alpha > 1) return;
        this.alpha = alpha;

        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.invalidate();
        } else {
            this.postInvalidate();
        }
    }

}
