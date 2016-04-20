package com.camnter.newlife.views.activity.easylikearea;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.camnter.easylikearea.EasyLikeArea;
import com.camnter.easylikearea.widget.EasyLikeImageView;
import com.camnter.newlife.R;
import com.camnter.newlife.constant.Constant;
import com.camnter.newlife.utils.GlideUtils;

/**
 * Description：EasyLikeAreaStyleActivity
 * Created by：CaMnter
 * Time：2016-04-20 14:40
 */
public class EasyLikeAreaStyleActivity extends AppCompatActivity {

    private EasyLikeArea circleEla;
    private EasyLikeArea roundEla;
    private EasyLikeArea normalEla;

    private DisplayMetrics mMetrics;


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_style);
        this.initViews();
        this.initData();
    }


    private void initViews() {
        this.circleEla = (EasyLikeArea) this.findViewById(R.id.circle_ela);
        this.roundEla = (EasyLikeArea) this.findViewById(R.id.round_ela);
        this.normalEla = (EasyLikeArea) this.findViewById(R.id.normal_ela);
    }


    private void initData() {
        this.mMetrics = this.getResources().getDisplayMetrics();
        this.initLikeArea();
    }


    private void initLikeArea() {
        this.circleEla.setOmitView(this.getOmitView(Constant.STYLE_AVATARS.length));
        this.roundEla.setOmitView(this.getOmitView(Constant.STYLE_AVATARS.length));
        this.normalEla.setOmitView(this.getOmitView(Constant.STYLE_AVATARS.length));
        for (int idRes : Constant.STYLE_AVATARS) {
            EasyLikeImageView circleIv = this.createEasyLikeImageView(EasyLikeImageView.CIRCLE);
            EasyLikeImageView roundIv = this.createEasyLikeImageView(EasyLikeImageView.ROUND);
            ImageView normalIv = new ImageView(this);
            normalIv.setLayoutParams(new ViewGroup.LayoutParams(this.dp2px(36), this.dp2px(36)));
            GlideUtils.displayNative(circleIv, idRes);
            GlideUtils.displayNative(roundIv, idRes);
            GlideUtils.displayNative(normalIv, idRes);
            this.circleEla.addView(circleIv);
            this.roundEla.addView(roundIv);
            this.normalEla.addView(normalIv);
        }
    }


    private EasyLikeImageView createEasyLikeImageView(@EasyLikeImageView.ImageType int imageType) {
        EasyLikeImageView iv = new EasyLikeImageView(this);
        iv.setImageType(imageType);
        iv.setLayoutParams(new ViewGroup.LayoutParams(this.dp2px(36), this.dp2px(36)));
        return iv;
    }


    public View getOmitView(int count) {
        View omitView = LayoutInflater.from(this).inflate(R.layout.view_omit_style_topic, null);
        TextView omitTv = (TextView) omitView.findViewById(R.id.topic_omit_tv);
        omitTv.setText(this.getString(this.getOmitVieStringFormatId(), count));
        return omitView;
    }


    public int getOmitVieStringFormatId() {
        return R.string.view_omit_style_topic_content;
    }


    /**
     * Dp to px
     *
     * @param dp dp
     * @return px
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.mMetrics);
    }
}
