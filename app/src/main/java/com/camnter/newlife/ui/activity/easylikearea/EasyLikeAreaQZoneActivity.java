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

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.camnter.newlife.R;

/**
 * Description：EasyLikeAreaQZoneActivity
 * Created by：CaMnter
 * Time：2016-04-20 14:47
 */
public class EasyLikeAreaQZoneActivity extends EasyLikeAreaTopicActivity {

    public void setOmitView(int count) {
        View omitView = LayoutInflater.from(this).inflate(R.layout.view_omit_style_qzone, null);
        this.omitTv = (TextView) omitView.findViewById(R.id.qzone_omit_tv);
        this.omitTv.setText(this.getString(this.getOmitVieStringFormatId(), count));
        this.topicEla.setOmitView(omitView);
    }


    @Override public int getOmitVieStringFormatId() {
        return R.string.view_omit_style_qzone_content;
    }

}
