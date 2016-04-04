package com.camnter.newlife.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.camnter.newlife.R;

/**
 * Description：AIEditText
 * Created by：CaMnter
 * Time：2015-09-29 14:18
 */
public class AIEditText extends AppCompatEditText
        implements View.OnTouchListener, View.OnFocusChangeListener, TextWatcher {

    private Drawable mClearDrawable;
    private OnFocusChangeListener mOnFocusChangeListener;
    private OnTouchListener mOnTouchListener;


    public AIEditText(Context context) {
        super(context);
        this.init(context);
    }


    public AIEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }


    public AIEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }


    /**
     * Register a callback to be invoked when focus of this view changed.
     *
     * @param l The callback that will run.
     */
    @Override public void setOnFocusChangeListener(OnFocusChangeListener l) {
        this.mOnFocusChangeListener = l;
    }


    /**
     * Register a callback to be invoked when a touch event is sent to this view.
     *
     * @param l the touch listener to attach to this view
     */
    @Override public void setOnTouchListener(OnTouchListener l) {
        this.mOnTouchListener = l;
    }


    /**
     * @param context
     */
    private void init(Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.ic_clear_mtrl_alpha);
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, super.getCurrentHintTextColor());
        this.mClearDrawable = wrappedDrawable;
        this.mClearDrawable.setBounds(0, 0, this.mClearDrawable.getIntrinsicHeight(),
                this.mClearDrawable.getIntrinsicHeight());
        this.setClearDrawableVisible(false);
        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
        this.addTextChangedListener(this);
    }


    private void setClearDrawableVisible(boolean visible) {
        this.mClearDrawable.setVisible(visible, false);
        final Drawable[] compoundDrawables = this.getCompoundDrawables();
        this.setCompoundDrawables(compoundDrawables[0], compoundDrawables[1],
                visible ? this.mClearDrawable : null, compoundDrawables[3]);
    }


    /**
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     * the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override public boolean onTouch(View v, MotionEvent event) {
        final int x = (int) event.getX();
        /**
         * x > ableEditWidth (totalWidth - paddingRight - clearDrawableWidth )
         * x > ableEditWidth == clearDrawable_X
         * so , when clearDrawable is visible and onClick it.
         */
        if (this.mClearDrawable.isVisible() && x > this.getWidth() - this.getPaddingRight() -
                this.mClearDrawable.getIntrinsicWidth()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                this.setError(null);
                this.setText("");
            }
            return true;
        }
        return this.mOnTouchListener != null && this.mOnTouchListener.onTouch(v, event);
    }


    /**
     * Called when the focus state of a view has changed.
     *
     * @param v The view whose state has changed.
     * @param hasFocus The new focus state of v.
     */
    @Override public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            this.setClearDrawableVisible(this.getText().length() > 0);
        } else {
            this.setClearDrawableVisible(false);
        }
        if (this.mOnFocusChangeListener != null) {
            this.mOnFocusChangeListener.onFocusChange(v, hasFocus);
        }
    }


    /**
     * @param text The text the TextView is displaying
     * @param start The offset of the start of the range of the text that was
     * modified
     * @param lengthBefore The length of the former text that has been replaced
     * @param lengthAfter The length of the replacement modified text
     */
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (isFocused()) this.setClearDrawableVisible(text.length() > 0);
    }


    /**
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }


    /**
     * @param s
     */
    @Override public void afterTextChanged(Editable s) {
    }
}
