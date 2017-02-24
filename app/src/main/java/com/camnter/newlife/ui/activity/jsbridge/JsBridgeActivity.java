package com.camnter.newlife.ui.activity.jsbridge;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;

/**
 * Description：JsBridgeActivity
 * Created by：CaMnter
 */

public class JsBridgeActivity extends BaseAppCompatActivity {

    @BindView(R.id.js_bridge_webview) WebView jsBridgeWebview;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_js_bridge;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);

        WebSettings settings = this.jsBridgeWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        this.jsBridgeWebview.setWebChromeClient(new JsBridgeWebChromeClient());
        this.jsBridgeWebview.loadUrl("file:///android_asset/index.html");

        JsBridge.register("bridge", BridgeImpl.class);
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
