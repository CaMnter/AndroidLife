package com.camnter.newlife.widget;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoAdjustSizeTextView extends TextView {

    // 最小字体
    private static final float DEFAULT_MIN_TEXT_SIZE = 8.0f;
    // 最大字体
    private static final float DEFAULT_MAX_TEXT_SIZE = 16.0f;

    private Paint textPaint;
    private float minTextSize = DEFAULT_MIN_TEXT_SIZE;
    private float maxTextSize = DEFAULT_MAX_TEXT_SIZE;


    public AutoAdjustSizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private void initialise() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        if (this.textPaint == null) {
            this.textPaint = new Paint();
            this.textPaint.set(this.getPaint());
        }
        this.maxTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.maxTextSize,
                displayMetrics);
        if (DEFAULT_MIN_TEXT_SIZE >= maxTextSize) {
            this.maxTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    this.maxTextSize, displayMetrics);
        }
        this.maxTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.maxTextSize,
                displayMetrics);
        this.minTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.minTextSize,
                displayMetrics);
    }


    /**
     * Re size the font so the specified text fits in the text box * assuming
     * the text box is the specified width.
     */
    private void fitText(String text, int textWidth) {
        if (textWidth > 0) {
            // 单行可见文字宽度
            int availableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
            float trySize = maxTextSize;
            // 先用最大字体写字
            textPaint.setTextSize(trySize);
            // 如果最大字体>最小字体 && 最大字体画出字的宽度>单行可见文字宽度
            while ((trySize > minTextSize) && (textPaint.measureText(text) > availableWidth)) {
                // 最大字体小一号
                trySize -= 1;
                // 保证大于最小字体
                if (trySize <= minTextSize) {
                    trySize = minTextSize;
                    break;
                }
                // 再次用新字体写字
                textPaint.setTextSize(trySize);
            }
            this.setTextSize(trySize);
        }
    }


    /**
     * 重写setText
     * 每次setText的时候
     *
     * @param text text
     * @param type type
     */
    @Override public void setText(CharSequence text, BufferType type) {
        this.initialise();
        String textString = text.toString();
        float trySize = maxTextSize;
        if (this.textPaint == null) {
            this.textPaint = new Paint();
            this.textPaint.set(this.getPaint());
        }
        this.textPaint.setTextSize(trySize);
        // 计算设置内容前 内容占据的宽度
        int textWidth = (int) this.textPaint.measureText(textString);
        // 拿到宽度和内容，进行调整
        this.fitText(textString, textWidth);
        super.setText(text, type);
    }


    @Override protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        this.fitText(text.toString(), this.getWidth());
    }


    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // 如果当前view的宽度 != 原来view的宽度
        if (w != oldw) this.fitText(this.getText().toString(), w);
    }
}