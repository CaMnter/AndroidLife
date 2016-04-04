package com.camnter.newlife.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.BounceInterpolator;
import com.camnter.newlife.utils.animation.RectAttribute;
import com.camnter.newlife.utils.animation.RectAttributeEvaluator;

/**
 * Description：AnimatorShaderRoundImageView
 * Created by：CaMnter
 * Time：2016-03-04 14:45
 */
public class AnimatorShaderRoundImageView extends ShaderRoundImageView {

    private RectAttribute currentCoordinates;


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


    @Override protected void onDraw(Canvas canvas) {
        if (currentCoordinates != null) {
            this.mRoundRect.left = this.currentCoordinates.left;
            this.mRoundRect.top = this.currentCoordinates.top;
            this.mRoundRect.right = this.currentCoordinates.right;
            this.mRoundRect.bottom = this.currentCoordinates.bottom;
            this.mBorderRadius = this.currentCoordinates.radius;
        }
        super.onDraw(canvas);
    }


    public void startAnimation(RectAttribute newCoordinates, Animator.AnimatorListener listener) {
        RectAttribute oldCoordinates = new RectAttribute(this.mRoundRect.left, this.mRoundRect.top,
                this.mRoundRect.right, this.mRoundRect.bottom, this.mBorderRadius);
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new RectAttributeEvaluator(),
                oldCoordinates, newCoordinates, oldCoordinates);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                currentCoordinates = (RectAttribute) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.addListener(listener);
        valueAnimator.setInterpolator(new BounceInterpolator());
        valueAnimator.setDuration(2666);
        valueAnimator.start();
    }
}
