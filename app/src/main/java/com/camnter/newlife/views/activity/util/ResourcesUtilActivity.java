package com.camnter.newlife.views.activity.util;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.utils.ResourcesUtil;

/**
 * Description：ResourcesUtilActivity
 * Created by：CaMnter
 * Time：2015-11-26 12:27
 */
public class ResourcesUtilActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(ResourcesUtil.getLayoutId(this, "activity_resources"));

        TextView resourcesTV = (TextView) this.findViewById(R.id.resources_tv);
        ImageView resourcesIV = (ImageView) this.findViewById(R.id.resources_iv);

        resourcesTV.setText(ResourcesUtil.getStringId(this, "app_label"));
        resourcesTV.setTextColor(this.getResources().getColor(ResourcesUtil.getColorId(this, "colorAccent")));

        resourcesIV.setImageResource(ResourcesUtil.getMipmapId(this, "mm_1"));
    }
}
