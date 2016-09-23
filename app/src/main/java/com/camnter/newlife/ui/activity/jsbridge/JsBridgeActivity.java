package com.camnter.newlife.ui.activity.jsbridge;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：JsBridgeActivity
 * Created by：CaMnter
 */

public class JsBridgeActivity extends BaseAppCompatActivity {

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
        // settings.setJavaScriptEnabled(true);
        // this.jsBridgeWebview.setWebChromeClient(new JsBridgeWebChromeClient());
        // this.jsBridgeWebview.loadUrl("file:///android_asset/index.html");

        this.jsBridgeWebview.getSettings().setSupportZoom(false);
        //      this.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.jsBridgeWebview.getSettings().setJavaScriptEnabled(true);
        jsBridgeWebview.setWebViewClient(new WebViewClient() {
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel(); 默认的处理方式，WebView变成空白页
                //                        //接受证书
                handler.proceed();
                //handleMessage(Message msg); 其他处理
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (jsBridgeWebview.getVisibility() != View.VISIBLE) {
                    jsBridgeWebview.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }



            @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        this.jsBridgeWebview.loadUrl("https://www.baidu.com/");

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


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    public void onButtonClick(View v) {
        this.jsBridgeWebview.loadUrl("https://www.baidu.com/");
    }
}
