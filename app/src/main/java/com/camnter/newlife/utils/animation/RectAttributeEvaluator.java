package com.camnter.newlife.utils.animation;

import android.animation.TypeEvaluator;

/**
 * Description：RectAttributeEvaluator
 * Created by：CaMnter
 * Time：2016-03-04 15:07
 */
public class RectAttributeEvaluator implements TypeEvaluator<RectAttribute> {
    public RectAttribute result;


    public RectAttributeEvaluator() {
        this.result = new RectAttribute(0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }


    @Override
    public RectAttribute evaluate(float fraction, RectAttribute startValue, RectAttribute endValue) {
        this.result.left = startValue.left + fraction * (endValue.left - startValue.left);
        this.result.top = startValue.top + fraction * (endValue.top - startValue.top);
        this.result.right = startValue.right + fraction * (endValue.right - startValue.right);
        this.result.bottom = startValue.bottom + fraction * (endValue.bottom - startValue.bottom);
        this.result.radius = startValue.radius + fraction * (endValue.radius - startValue.radius);
        return this.result;
    }
}