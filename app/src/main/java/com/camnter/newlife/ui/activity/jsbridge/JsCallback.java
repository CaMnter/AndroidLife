package com.camnter.newlife.ui.activity.jsbridge;

import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import java.lang.ref.WeakReference;
import org.json.JSONObject;

/**
 * Description：JsCallback
 *
 * native 层执行完代码,回调到 Js 层
 *
 * Created by：CaMnter
 */

public class JsCallback {
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private static final String JS_CALLBACK_FORMAT = "javascript:JSBridge.onFinish('%s', %s);";
    private String mPort;
    private WeakReference<WebView> mWebViewRef;


    public JsCallback(WebView view, String port) {
        this.mWebViewRef = new WeakReference<>(view);
        this.mPort = port;
    }


    public void apply(JSONObject jsonObject) {
        final String execJs = String.format(JS_CALLBACK_FORMAT, this.mPort,
            String.valueOf(jsonObject));
        if (this.mWebViewRef != null && this.mWebViewRef.get() != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWebViewRef.get().loadUrl(execJs);
                }
            });
        }

    }
}