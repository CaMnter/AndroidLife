package com.camnter.newlife.utils.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;

/**
 * Description：GsonRequest
 * Created by：CaMnter
 * Time：2016-05-25 12:01
 */
public abstract class GsonRequest<T> extends Request<T>
        implements Response.Listener<T>, Response.ErrorListener {

    protected static final String PROTOCOL_CHARSET = "utf-8";

    private Gson mGson;
    private Response.Listener<T> mResponseListener;
    private Class<T> mClass;


    public GsonRequest(String url, Class<T> clazz) {
        this(Method.GET, url, clazz);
    }


    public GsonRequest(int method, String url, Class<T> clazz) {
        super(method, url, null);
        this.mGson = new Gson();
        this.mClass = clazz;
        this.mResponseListener = this;
    }


    @Override protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(this.mGson.fromJson(jsonString, this.mClass),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }


    @Override protected void deliverResponse(T response) {
        this.mResponseListener.onResponse(response);
    }


    /**
     * @return this request's {@link com.android.volley.Response.ErrorListener}.
     */
    public Response.ErrorListener getErrorListener() {
        return this;
    }


    /**
     * Delivers error message to the ErrorListener that the Request was
     * initialized with.
     *
     * @param error Error details
     */
    @Override public void deliverError(VolleyError error) {
        this.onErrorResponse(error);
    }


    /**
     * Called when a response is received.
     */
    public abstract void onResponse(T response);

    /**
     * Callback method that an error has been occurred with the
     * provided error code and optional user-readable message.
     */
    public abstract void onErrorResponse(VolleyError error);
}
