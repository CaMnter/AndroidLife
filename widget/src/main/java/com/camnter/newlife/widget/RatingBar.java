package com.camnter.newlife.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Description：RatingBar
 * Created by：CaMnter
 */

public class RatingBar extends LinearLayout {

    private Drawable progressDrawable;
    private Drawable backgroundDrawable;

    private int spacing;
    private int number;

    private List<ImageView> imageList;


    public RatingBar(Context context) {
        super(context);
    }


    public RatingBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initAttributes(context, attrs);
    }


    public RatingBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initAttributes(context, attrs);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RatingBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initAttributes(context, attrs);
    }


    private void initAttributes(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_rating_bar, this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatingBar);
        this.spacing = typedArray.getDimensionPixelSize(R.styleable.RatingBar_spacing, 0);
        this.number = typedArray.getDimensionPixelSize(R.styleable.RatingBar_number, 0);
        this.progressDrawable = typedArray.getDrawable(R.styleable.RatingBar_progressDrawable);
        this.backgroundDrawable = typedArray.getDrawable(R.styleable.RatingBar_backgroundDrawable);
        typedArray.recycle();
    }


    /**
     * Finalize inflating a view from XML.  This is called as the last phase
     * of inflation, after all child views have been added.
     *
     * <p>Even if the subclass overrides onFinishInflate, they should always be
     * sure to call the super method, so that we get called.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.imageList = new ArrayList<>();
        this.imageList.add((ImageView) this.findViewById(R.id.view_rating_bar_first_image));
        this.imageList.add((ImageView) this.findViewById(R.id.view_rating_bar_second_image));
        this.imageList.add((ImageView) this.findViewById(R.id.view_rating_bar_third_image));
        this.imageList.add((ImageView) this.findViewById(R.id.view_rating_bar_fourth_image));
        this.imageList.add((ImageView) this.findViewById(R.id.view_rating_bar_fifth_image));

        if (this.backgroundDrawable == null) return;
        for (int i = 0; i < this.imageList.size(); i++) {
            final ImageView imageView = this.imageList.get(i);
            if (i != 0) {
                final MarginLayoutParams params = (MarginLayoutParams) imageView.getLayoutParams();
                params.setMargins(this.spacing, 0, 0, 0);
            }
            imageView.setImageDrawable(this.backgroundDrawable);
        }

        this.setNumber(this.number);
    }


    public void setNumber(int number) {
        if (this.progressDrawable == null || this.backgroundDrawable == null) return;
        final int size = this.imageList.size();
        // 超出上限
        number = number > size ? size : number;
        for (int i = 0; i < size; i++) {
            final ImageView imageView = this.imageList.get(i);
            imageView.setImageDrawable(
                number - 1 >= i ? this.progressDrawable : this.backgroundDrawable);
        }
    }

}

