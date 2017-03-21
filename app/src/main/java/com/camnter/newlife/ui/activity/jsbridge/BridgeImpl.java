package com.camnter.newlife.ui.activity.jsbridge;

import android.webkit.WebView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description：BridgeImpl
 * Created by：CaMnter
 */

public class BridgeImpl implements IBridge {

    public static void showToast(WebView webView, JSONObject param, final JsCallback jsCallback) {
        String msg = param.optString("msg");
        Toast.makeText(webView.getContext(), msg, Toast.LENGTH_SHORT).show();
        if (jsCallback != null) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("key", "value");
                jsCallback.apply(getJSONObject(0, "ok", jsonObject));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public static void childThread(WebView webView, JSONObject param, final JsCallback jsCallback) {
        new Thread(() -> {
            try {
                Thread.sleep(2666);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("key", "value");
                jsCallback.apply(getJSONObject(0, "ok", jsonObject));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private static JSONObject getJSONObject(int code, String msg, JSONObject result) {
        JSONObject object = new JSONObject();
        try {
            object.put("code", code);
            object.put("msg", msg);
            object.putOpt("result", result);
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
