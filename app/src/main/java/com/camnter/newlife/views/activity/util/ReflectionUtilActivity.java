package com.camnter.newlife.views.activity.util;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.ReflectionUtils;

/**
 * Description：ReflectionUtilActivity
 * Created by：CaMnter
 * Time：2015-11-20 12:17
 */
public class ReflectionUtilActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private ImageView imageView;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_reflection_util;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.imageView = (ImageView) this.findViewById(R.id.reflection_iv);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.findViewById(R.id.reflection_bt).setOnClickListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reflection_bt:
                /*
                 * 获取资源名为mm_1的mipmap类型文件
                 */
                this.imageView.setImageResource(ReflectionUtils.getResourceId(this, "mm_1",
                        ReflectionUtils.ResourcesType.mipmap));
                break;
        }
    }
}
