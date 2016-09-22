package com.camnter.newlife.ui.activity.jsbridge;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：JsBridgeActivity
 * Created by：CaMnter
 */

public class JsBridgeActivity extends BaseAppCompatActivity implements View.OnClickListener {

    @BindView(R.id.js_bridge_webview) WebView jsBridgeWebview;
    @BindView(R.id.js_bridge_button) Button jsBridgeButton;


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
    @SuppressLint("SetJavaScriptEnabled") @Override
    protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        this.setContentView(R.layout.activity_js_bridge);

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
        this.jsBridgeButton.setOnClickListener(this);
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
        switch (v.getId()){
            case R.id.js_bridge_button:
                this.jsBridgeWebview.loadUrl("file:///android_asset/index.html");
                break;
        }
    }
}
