package com.camnter.newlife.widget.text;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class ClickableSpanNoUnderline extends ClickableSpan {

    private static final int NO_COLOR = -206;

    private int color;

    public ClickableSpanNoUnderline(int color) {
        super();
        this.color = color;
    }

    public ClickableSpanNoUnderline() {
        this(NO_COLOR);
    }

    /**
     * Makes the text underlined and in the link color.
     *
     * @param ds
     */
    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        if (this.color == NO_COLOR) {
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);
        } else {
            ds.setUnderlineText(false);
            ds.setColor(this.color);
        }
        ds.clearShadowLayer();
        ds.bgColor = Color.TRANSPARENT;
    }

    /**
     * Performs the click action associated with this span.
     *
     * @param widget
     */
    @Override
    public void onClick(View widget) {
    }
}