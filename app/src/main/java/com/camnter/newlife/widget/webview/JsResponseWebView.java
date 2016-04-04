package com.camnter.newlife.widget.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Description：JsResponseWebView
 * Created by：CaMnter
 * Time：2016-01-12 11:36
 */
public class JsResponseWebView extends WebView {

    private static final String INTERFACE_NAME = "jsResponseInterface";
    private static final String ON_CLICK_PICTURE = "onClickPicture";
    private static final String ON_CLICK_TAG = "onClickTag";

    private String javascript;


    public JsResponseWebView(Context context) {
        super(context);
        this.initWebView();
    }


    public JsResponseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initWebView();
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public JsResponseWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr);
        this.initWebView();
    }


    public JsResponseWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initWebView();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public JsResponseWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initWebView();
    }


    @SuppressLint("SetJavaScriptEnabled") private void initWebView() {
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        this.setWebViewClient(new JsResponseViewClient());
        this.javascript = "javascript:(function(){" +
                "   // 图片   " +
                "   var images = document.getElementsByTagName('img');" +
                "   for(var i=0;i<images.length;i++){" +
                "       images[i].onclick=function(){" +
                "           window." + INTERFACE_NAME + "." + ON_CLICK_PICTURE +
                "(images[i].src);" +
                "       }" +
                "   }" +
                "   // 标签   " +
                "   var tags = document.getElementsByClassName('video');" +
                "   for(var i=0;i<tags.length;i++){" +
                "       tags[i].onclick=function(){" +
                "           window." + INTERFACE_NAME + "." + ON_CLICK_TAG + "(tags[i].id,i);" +
                "       }" +
                "   }" +
                "})()";
    }


    /**
     * inject your java script
     */
    private void injectJavaScript() {
        if (TextUtils.isEmpty(this.javascript)) return;
        this.loadUrl(this.javascript);
    }


    public void setJavascript(String javascript) {
        this.javascript = javascript;
    }


    @SuppressLint("AddJavascriptInterface")
    public void setJsResponseInterface(JsResponseInterface jsResponseInterface) {
        if (jsResponseInterface == null) return;
        this.addJavascriptInterface(jsResponseInterface, INTERFACE_NAME);
    }


    private class JsResponseViewClient extends WebViewClient {
        @Override public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            JsResponseWebView.this.injectJavaScript();
        }
    }

    /**
     * 继承此类实现自己逻辑
     * 并且调用setJsResponseInterface
     */
    public interface JsResponseInterface {
        /**
         * 标签点击
         *
         * @param name name
         */
        @JavascriptInterface void onClickTag(String name);

        /**
         * 图片点击
         *
         * @param url url
         * @param position position
         */
        @JavascriptInterface void onClickPicture(String url, int position);
    }
}
