package com.camnter.newlife.widget.span;

import android.text.TextPaint;
import android.text.style.URLSpan;

public class URLSpanNoUnderline extends URLSpan {
    private int color;


    public URLSpanNoUnderline(String url, int color) {
        super(url);
        this.color = color;
    }


    @Override public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(color);
    }
}