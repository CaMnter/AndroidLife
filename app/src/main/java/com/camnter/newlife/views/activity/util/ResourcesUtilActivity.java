package com.camnter.newlife.views.activity.util;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.ResourcesUtils;

/**
 * Description：ResourcesUtilActivity
 * Created by：CaMnter
 * Time：2015-11-26 12:27
 */
public class ResourcesUtilActivity extends BaseAppCompatActivity {

    private TextView resourcesTV;
    private ImageView resourcesIV;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return ResourcesUtils.getLayoutId(this, "activity_resources");
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.resourcesTV = (TextView) this.findViewById(R.id.resources_tv);
        this.resourcesIV = (ImageView) this.findViewById(R.id.resources_iv);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        this.resourcesTV.setText(ResourcesUtils.getStringId(this, "app_label"));
        this.resourcesTV.setTextColor(
                ResourcesUtils.getColor(this, ResourcesUtils.getColorId(this, "colorAccent")));

        this.resourcesIV.setImageResource(ResourcesUtils.getMipmapId(this, "mm_1"));
    }
}
