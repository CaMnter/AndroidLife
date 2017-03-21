/*
 * Copyright (C) 2016 CaMnter yuanyu.camnter@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camnter.newlife.ui.activity.easylikearea;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.camnter.easylikearea.EasyLikeArea;
import com.camnter.easylikearea.widget.EasyLikeImageView;
import com.camnter.newlife.R;
import com.camnter.newlife.constant.Constant;
import com.camnter.newlife.utils.GlideUtils;

/**
 * Description：EasyLikeAreaTopicActivity
 * Created by：CaMnter
 * Time：2016-04-20 14:32
 */
public class EasyLikeAreaTopicActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int likeAddedColor = 0xff38B8C1;
    private static final int likeColor = 0xff97A4AF;
    public EasyLikeArea topicEla;
    public TextView omitTv;
    private DisplayMetrics mMetrics;
    private EasyLikeImageView addIv;
    private boolean added = false;
    private TextView likeTv;


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_topic);
        this.initViews();
        this.initListeners();
        this.initLikeArea();
    }


    private void initViews() {
        this.mMetrics = this.getResources().getDisplayMetrics();
        this.topicEla = (EasyLikeArea) this.findViewById(R.id.topic_ela);
        this.addIv = this.createEasyLikeImageView();
        this.addIv.setImageResource(R.drawable.ic_camnter);
    }


    private void initListeners() {
        this.likeTv = (TextView) this.findViewById(R.id.topic_like_tv);
        if (this.likeTv != null) this.likeTv.setOnClickListener(this);
        View chatTv = this.findViewById(R.id.topic_chat_tv);
        if (chatTv != null) chatTv.setOnClickListener(this);
        View shareTv = this.findViewById(R.id.topic_share_tv);
        if (shareTv != null) shareTv.setOnClickListener(this);
    }


    private EasyLikeImageView createEasyLikeImageView() {
        EasyLikeImageView iv = new EasyLikeImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(this.dp2px(36), this.dp2px(36)));
        return iv;
    }


    private void initLikeArea() {
        this.setOmitView(Constant.AVATARS.length);
        for (int idRes : Constant.AVATARS) {
            EasyLikeImageView iv = this.createEasyLikeImageView();
            GlideUtils.displayNative(iv, idRes);
            this.topicEla.addView(iv);
        }
    }


    public void setOmitView(int count) {
        View omitView = LayoutInflater.from(this).inflate(R.layout.view_omit_style_topic, null);
        this.omitTv = (TextView) omitView.findViewById(R.id.topic_omit_tv);
        this.omitTv.setText(this.getString(this.getOmitVieStringFormatId(), count));
        this.topicEla.setOmitView(omitView);
    }


    public int getOmitVieStringFormatId() {
        return R.string.view_omit_style_topic_content;
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.topic_like_tv:
                if (!added) {
                    this.topicEla.addView(this.addIv);
                    this.added = true;
                    this.likeTv.setTextColor(likeAddedColor);
                    this.omitTv.setText(this.getString(this.getOmitVieStringFormatId(),
                        Constant.AVATARS.length + 1));
                } else {
                    this.topicEla.removeView(this.addIv);
                    this.added = false;
                    this.likeTv.setTextColor(likeColor);
                    this.omitTv.setText(this.getString(this.getOmitVieStringFormatId(),
                        Constant.AVATARS.length));
                }
                break;
            case R.id.topic_share_tv:
                break;
            case R.id.topic_chat_tv:
                break;
        }
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
