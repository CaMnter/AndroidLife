package com.camnter.newlife.activity.util;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.camnter.newlife.R;
import com.camnter.newlife.utils.ReflectionUtil;

/**
 * Description：ReflectionUtilActivity
 * Created by：CaMnter
 * Time：2015-11-20 12:17
 */
public class ReflectionUtilActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_reflection_util);

        this.imageView = (ImageView) this.findViewById(R.id.reflection_iv);
        this.findViewById(R.id.reflection_bt).setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reflection_bt:
                /*
                 * 获取资源名为mm_1的mipmap类型文件
                 */
                this.imageView.setImageResource(ReflectionUtil.getResourceId(this, "mm_1", ReflectionUtil.ResourcesType.mipmap));
                break;
        }
    }
}
