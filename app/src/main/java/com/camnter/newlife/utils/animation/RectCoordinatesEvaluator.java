package com.camnter.newlife.utils.animation;

import android.animation.TypeEvaluator;

/**
 * Description：RectCoordinatesEvaluator
 * Created by：CaMnter
 * Time：2016-03-04 15:07
 */
public class RectCoordinatesEvaluator implements TypeEvaluator<RectCoordinates> {
    @Override
    public RectCoordinates evaluate(float fraction, RectCoordinates startValue, RectCoordinates endValue) {
        return new RectCoordinates(startValue.left + fraction * (endValue.left - startValue.left), startValue.top + fraction * (endValue.top - startValue.top));
    }
}