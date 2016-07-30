package com.camnter.newlife.ui.activity.classloader;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.DeviceUtils;

/**
 * Description：ClassLoaderActivity
 * Created by：CaMnter
 */

public class ClassLoaderActivity extends BaseAppCompatActivity {

    @Bind(R.id.class_loader_root_layout) LinearLayout classLoaderRootLayout;

    private int i = 0;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_class_loader;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
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
        ClassLoader classLoader = this.getClassLoader();
        if (classLoader != null) {
            TextView t1 = this.createdView();
            t1.setText("[onCreate] classLoader " + ++i + " : " + classLoader.toString());
            this.classLoaderRootLayout.addView(t1);
            while (classLoader.getParent() != null) {
                classLoader = classLoader.getParent();
                TextView t2 = this.createdView();
                t2.setText("[onCreate] classLoader " + ++i + " : " + classLoader.toString());
                this.classLoaderRootLayout.addView(t2);
            }
        }
    }


    private TextView createdView() {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = DeviceUtils.dp2px(this, 16.0f);
        textView.setLayoutParams(params);
        return textView;
    }

}
