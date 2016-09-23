package com.camnter.newlife.ui.activity.jsbridge;

import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Description：JsBridgeWebChromeClient
 * Created by：CaMnter
 */

public class JsBridgeWebChromeClient extends WebChromeClient {
    /**
     * Tell the client to display a prompt dialog to the user. If the client
     * returns true, WebView will assume that the client will handle the
     * prompt dialog and call the appropriate JsPromptResult method. If the
     * client returns false, a default value of false will be returned to to
     * javascript. The default behavior is to return false.
     *
     * @param view The WebView that initiated the callback.
     * @param url The url of the page requesting the dialog.
     * @param message Message to be displayed in the window.
     * @param defaultValue The default value displayed in the prompt dialog.
     * @param result A JsPromptResult used to send the user's reponse to
     * javascript.
     * @return boolean Whether the client will handle the prompt dialog.
     */
    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        super.onJsPrompt(view, url, message, defaultValue, result);
        // Js 层通过在此, 调用到 native 层的 callJava 方法
        result.confirm(JsBridge.callJava(view, message));
        return true;
    }
}
