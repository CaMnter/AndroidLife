package com.camnter.newlife.views.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.animation.RectAttribute;
import com.camnter.newlife.widget.AnimatorShaderRoundImageView;

/**
 * Description：CaMnter
 * Created by：CaMnter
 * Time：2016-02-25 22:37
 */
public class AnimatorActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private TextView valueTv;
    private TextView rotationTv;
    private TextView alphaTv;
    private TextView translationTv;
    private TextView scaleTv;
    private TextView setTv;
    private TextView evaluatorTv;
    private TextView propertyTv;
    private AnimatorShaderRoundImageView evaluatorIv;

    private SurfaceView valueSv;
    private SurfaceHolder holder;
    private Canvas holderCanvas;
    private Paint surfacePaint = new Paint();

    private ValueAnimator valueAnimator;
    private ObjectAnimator alphaAnimator;
    private ObjectAnimator rotationAnimator;
    private ObjectAnimator translationAnimator;
    private ObjectAnimator scaleAnimator;

    private int[] surfaceLocation = new int[2];
    private float[] property;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_animator;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.valueTv = (TextView) this.findViewById(R.id.animator_value_tv);
        this.alphaTv = (TextView) this.findViewById(R.id.animator_alpha_tv);
        this.rotationTv = (TextView) this.findViewById(R.id.animator_rotation_tv);
        this.translationTv = (TextView) this.findViewById(R.id.animator_translation_tv);
        this.scaleTv = (TextView) this.findViewById(R.id.animator_scale_tv);
        this.setTv = (TextView) this.findViewById(R.id.animator_set_tv);
        this.valueSv = (SurfaceView) this.findViewById(R.id.animator_sv);
        this.evaluatorIv = (AnimatorShaderRoundImageView) this.findViewById(
                R.id.animator_evaluator_iv);
        this.evaluatorTv = (TextView) this.findViewById(R.id.animator_evaluator_tv);
        this.propertyTv = (TextView) this.findViewById(R.id.animator_property_tv);
        this.holder = this.valueSv.getHolder();
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.valueTv.setOnClickListener(this);
        this.alphaTv.setOnClickListener(this);
        this.rotationTv.setOnClickListener(this);
        this.translationTv.setOnClickListener(this);
        this.scaleTv.setOnClickListener(this);
        this.setTv.setOnClickListener(this);
        this.evaluatorTv.setOnClickListener(this);
        this.propertyTv.setOnClickListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        // x y
        this.surfaceLocation = new int[2];
        //获取在整个屏幕内的绝对坐标
        this.valueSv.getLocationOnScreen(this.surfaceLocation);
        this.reset();
        this.initAnimator();
    }


    private void initAnimator() {
        this.valueAnimator = ValueAnimator.ofFloat(0.0f, 100.0f);
        this.valueAnimator.setDuration(5000);
        this.valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                String value = ((float) animation.getAnimatedValue()) + "";
                property = measureText(surfacePaint, value);
                holderCanvas = holder.lockCanvas(null);
                holderCanvas.drawColor(Color.BLACK);
                surfacePaint.setColor(Color.WHITE);
                surfacePaint.setTextSize(30);
                holderCanvas.drawText(value,
                        surfaceLocation[0] + valueSv.getWidth() / 2 - property[0] / 2,
                        surfaceLocation[1] + valueSv.getHeight() / 2 + property[1] / 2,
                        surfacePaint);
                holder.unlockCanvasAndPost(holderCanvas);
            }
        });
        this.valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {
                valueTv.setEnabled(false);
            }


            @Override public void onAnimationEnd(Animator animation) {
                valueTv.setEnabled(true);
            }


            @Override public void onAnimationCancel(Animator animation) {
                valueTv.setEnabled(true);
            }


            @Override public void onAnimationRepeat(Animator animation) {

            }
        });

        this.alphaAnimator = ObjectAnimator.ofFloat(this.alphaTv, "alpha", 1f, 0f, 1f, 0f, 1f);
        this.alphaAnimator.setDuration(2000);
        this.alphaAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {
                alphaTv.setEnabled(false);
            }


            @Override public void onAnimationEnd(Animator animation) {
                alphaTv.setEnabled(true);
            }


            @Override public void onAnimationCancel(Animator animation) {
                alphaTv.setEnabled(true);
            }


            @Override public void onAnimationRepeat(Animator animation) {

            }
        });

        this.rotationAnimator = ObjectAnimator.ofFloat(this.rotationTv, "rotation", 0f, 180f, 0f,
                180f, 0f);
        this.rotationAnimator.setDuration(2000);
        this.rotationAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {
                rotationTv.setEnabled(false);
            }


            @Override public void onAnimationEnd(Animator animation) {
                rotationTv.setEnabled(true);
            }


            @Override public void onAnimationCancel(Animator animation) {
                rotationTv.setEnabled(true);
            }


            @Override public void onAnimationRepeat(Animator animation) {

            }
        });

        this.scaleAnimator = ObjectAnimator.ofFloat(this.scaleTv, "scaleY", 1f, 1.5f, 1f, 1.5f, 1f,
                1.5f, 1f, 1.5f, 1f);
        this.scaleAnimator.setDuration(3000);
        this.scaleAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {
                scaleTv.setEnabled(false);
            }


            @Override public void onAnimationEnd(Animator animation) {
                scaleTv.setEnabled(true);
            }


            @Override public void onAnimationCancel(Animator animation) {
                scaleTv.setEnabled(true);
            }


            @Override public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.animator_value_tv:
                this.reset();
                this.valueAnimator.start();
                break;
            case R.id.animator_alpha_tv:
                this.alphaAnimator.start();
                break;
            case R.id.animator_rotation_tv:
                this.rotationAnimator.start();
                break;
            case R.id.animator_translation_tv: {
                // x y
                int[] translationTvLocation = new int[2];
                //获取在整个屏幕内的绝对坐标
                this.translationTv.getLocationOnScreen(translationTvLocation);
                int totalHeight = this.translationTv.getHeight() + translationTvLocation[1];
                float tY = this.translationTv.getTranslationY();
                this.translationAnimator = ObjectAnimator.ofFloat(this.translationTv,
                        "translationY", tY, -totalHeight, tY, -totalHeight, tY);
                this.translationAnimator.setDuration(3000);
                this.translationAnimator.addListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animation) {
                        translationTv.setEnabled(false);
                    }


                    @Override public void onAnimationEnd(Animator animation) {
                        translationTv.setEnabled(true);
                    }


                    @Override public void onAnimationCancel(Animator animation) {
                        translationTv.setEnabled(true);
                    }


                    @Override public void onAnimationRepeat(Animator animation) {

                    }
                });
                translationAnimator.start();
                break;
            }
            case R.id.animator_scale_tv:
                this.scaleAnimator.start();
                break;
            case R.id.animator_set_tv: {
                // x y
                int[] setTvLocation = new int[2];
                //获取在整个屏幕内的绝对坐标
                this.setTv.getLocationOnScreen(setTvLocation);
                int totalHeight = this.setTv.getHeight() + setTvLocation[1];
                float tY = this.setTv.getTranslationY();
                ObjectAnimator translation = ObjectAnimator.ofFloat(this.setTv, "translationY", tY,
                        -totalHeight, tY, -totalHeight, tY);
                ObjectAnimator rotation = ObjectAnimator.ofFloat(this.setTv, "rotation", 0f, 180f,
                        0f, 180f, 0f);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(this.setTv, "alpha", 1f, 0f, 1f, 0f,
                        1f);
                ObjectAnimator scale = ObjectAnimator.ofFloat(this.setTv, "scaleY", 1f, 1.5f, 1f,
                        1.5f, 1f, 1.5f, 1f, 1.5f, 1f);
                AnimatorSet set = new AnimatorSet();
                set.play(alpha).with(rotation).after(translation).before(scale);
                set.setDuration(4000);
                set.start();
                break;
            }
            case R.id.animator_evaluator_tv:
                RectAttribute newRectAttribute = new RectAttribute(this.evaluatorIv.getWidth() / 2,
                        this.evaluatorIv.getHeight() / 2, this.evaluatorIv.getWidth() / 2,
                        this.evaluatorIv.getHeight() / 2, 0.0f);
                this.evaluatorIv.startAnimation(newRectAttribute, new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animation) {
                        evaluatorTv.setEnabled(false);
                    }


                    @Override public void onAnimationEnd(Animator animation) {
                        evaluatorTv.setEnabled(true);
                    }


                    @Override public void onAnimationCancel(Animator animation) {
                        evaluatorTv.setEnabled(true);
                    }


                    @Override public void onAnimationRepeat(Animator animation) {

                    }
                });
                break;
            case R.id.animator_property_tv: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    v.animate()
                     .alpha(0.0f)
                     .setDuration(3000)
                     .setListener(new Animator.AnimatorListener() {
                         @Override public void onAnimationStart(Animator animation) {
                             v.setEnabled(false);
                         }


                         @Override public void onAnimationEnd(Animator animation) {
                             v.setEnabled(true);
                         }


                         @Override public void onAnimationCancel(Animator animation) {

                         }


                         @Override public void onAnimationRepeat(Animator animation) {

                         }
                     });
                }
                break;
            }
        }
    }


    /**
     * 获取 将要 绘制 文字的 宽高
     *
     * @param paint paint
     * @param text text
     * @return float[]
     */
    private float[] measureText(Paint paint, String text) {
        float[] property = new float[2];
        property[0] = paint.measureText(text);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        property[1] = fontMetrics.descent - fontMetrics.ascent + fontMetrics.leading;
        return property;
    }


    private void reset() {
        this.holderCanvas = this.holder.lockCanvas(null);
        if (this.holderCanvas == null) return;
        String initText = "0.0";
        this.property = this.measureText(this.surfacePaint, initText);
        this.holderCanvas.drawColor(Color.BLACK);
        this.surfacePaint.setColor(Color.WHITE);
        this.holderCanvas.drawText(initText,
                this.surfaceLocation[0] + this.valueSv.getWidth() / 2 - this.property[0] / 2,
                this.surfaceLocation[1] + this.valueSv.getHeight() / 2 + this.property[1] / 2,
                this.surfacePaint);
        this.holder.unlockCanvasAndPost(this.holderCanvas);
    }
}
