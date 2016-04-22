package com.camnter.newlife.views.activity.lrucache;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import com.camnter.newlife.core.BaseActivity;

/**
 * Description：LruCacheActivity
 * Created by：CaMnter
 * Time：2016-04-21 20:48
 */
public class LruCacheActivity extends BaseActivity {

    int cacheSize = 4 * 1024 * 1024; // 4MiB
    LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return 0;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {

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

    }
}
