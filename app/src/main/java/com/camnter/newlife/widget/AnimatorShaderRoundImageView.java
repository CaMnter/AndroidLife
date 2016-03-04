package com.camnter.newlife.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.BounceInterpolator;

import com.camnter.newlife.utils.animation.RectCoordinates;
import com.camnter.newlife.utils.animation.RectCoordinatesEvaluator;

/**
 * Description：AnimatorShaderRoundImageView
 * Created by：CaMnter
 * Time：2016-03-04 14:45
 */
public class AnimatorShaderRoundImageView extends ShaderRoundImageView {


    private RectCoordinates currentCoordinates;

    public AnimatorShaderRoundImageView(Context context) {
        super(context);
    }

    public AnimatorShaderRoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatorShaderRoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnimatorShaderRoundImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentCoordinates != null) {
            this.mRoundRect = new RectF(this.currentCoordinates.left, this.currentCoordinates.top, this.getWidth(), this.getHeight());
        }
        super.onDraw(canvas);
    }

    public void startAnimation(RectCoordinates newCoordinates,Animator.AnimatorListener listener) {
        RectCoordinates oldCoordinates = new RectCoordinates(
                this.mRoundRect.left,
                this.mRoundRect.top
        );
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new RectCoordinatesEvaluator(), oldCoordinates, newCoordinates, oldCoordinates);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentCoordinates = (RectCoordinates) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.addListener(listener);
        valueAnimator.setInterpolator(new BounceInterpolator());
        valueAnimator.setDuration(2666);
        valueAnimator.start();
    }

}
