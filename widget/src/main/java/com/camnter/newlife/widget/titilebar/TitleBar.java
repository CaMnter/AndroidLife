package com.camnter.newlife.widget.titilebar;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.camnter.newlife.widget.R;

/**
 * Description：TitleBar
 * Created by：CaMnter
 */

public class TitleBar extends RelativeLayout {

    private TextView leftText;
    private ImageView leftImage;
    private TextView rightText;
    private ImageView rightImage;
    private TextView titleText;


    public TitleBar(Context context) {
        super(context);
        this.initAttributes();
    }


    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initAttributes();
    }


    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initAttributes();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initAttributes();
    }


    private void initAttributes() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_title_bar, this, true);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (this.isInEditMode()) return;

        this.titleText = (TextView) this.findViewById(R.id.title_bar_title_text);
        this.rightImage = (ImageView) this.findViewById(R.id.title_bar_right_image);
        this.rightText = (TextView) this.findViewById(R.id.title_bar_right_text);
        this.leftImage = (ImageView) this.findViewById(R.id.title_bar_left_image);
        this.leftText = (TextView) this.findViewById(R.id.title_bar_left_text);

        this.setBackgroundResource(R.drawable.bg_title_bar);
    }


    public TextView getLeftText() {
        return this.leftText;
    }


    public ImageView getLeftImage() {
        return this.leftImage;
    }


    public TextView getRightText() {
        return this.rightText;
    }


    public ImageView getRightImage() {
        return this.rightImage;
    }


    public TextView getTitleText() {
        return this.titleText;
    }


    public void hideLeftText() {
        this.leftText.setVisibility(GONE);
    }


    public void hideLeftImage() {
        this.leftImage.setVisibility(GONE);
    }


    public void hideRightText() {
        this.rightText.setVisibility(GONE);
    }


    public void hideRightImage() {
        this.rightImage.setVisibility(GONE);
    }


    public void hideTitleText() {
        this.titleText.setVisibility(GONE);
    }


    public void showLeftText() {
        this.leftText.setVisibility(VISIBLE);
    }


    public void showLeftImage() {
        this.leftImage.setVisibility(VISIBLE);
    }


    public void showRightText() {
        this.rightText.setVisibility(VISIBLE);
    }


    public void showRightImage() {
        this.rightImage.setVisibility(VISIBLE);
    }


    public void showTitleText() {
        this.titleText.setVisibility(VISIBLE);
    }


    public void setTitleText(@StringRes final int stringRes) {
        this.titleText.setText(stringRes);
    }


    public void setTitleText(@NonNull final CharSequence text) {
        this.titleText.setText(text);
    }


    public void setRightImageResource(@DrawableRes final int drawableRes) {
        this.hideRightText();
        this.showRightImage();
        this.rightImage.setImageResource(drawableRes);
    }


    public void setLeftImageResource(@DrawableRes final int drawableRes) {
        this.hideLeftText();
        this.showLeftImage();
        this.leftImage.setImageResource(drawableRes);
    }


    public void enableLeftImageBackOnClick() {
        if (this.leftImage != null) {
            this.leftImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Context context = getContext();
                    if (context != null &&
                        context instanceof Activity) {
                        final Activity activity = (Activity) context;
                        activity.finish();
                    }
                }
            });
        }
    }

}
