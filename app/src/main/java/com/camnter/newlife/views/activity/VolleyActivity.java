package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.volley.GsonRequest;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * Description：VolleyActivity
 * Created by：CaMnter
 * Time：2016-05-25 11:34
 */
public class VolleyActivity extends BaseAppCompatActivity {

    @Bind(R.id.volley_get_content_text) TextView mGetContentText;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_volley;
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

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(new GsonRequest<GankData>("http://gank.io/api/data/Android/1/1", GankData.class) {
            /**
             * Called when a response is received.
             */
            @Override public void onResponse(GankData response) {
                mGetContentText.setText(response.toString());
            }


            /**
             * Callback method that an error has been occurred with the
             * provided error code and optional user-readable message.
             */
            @Override public void onErrorResponse(VolleyError error) {
                showToast(error.getMessage());
                Log.d("GsonRequest", error.getMessage());
            }
        });
    }


    public class GankData {
        private static final String TAG = "GankData";

        public boolean error;
        public ArrayList<GankResultData> results;


        @Override public String toString() {
            StringBuilder builder = new StringBuilder(TAG).append("\n\n");
            for (GankResultData result : results) {
                builder.append(result.toString());
                builder.append("\n\n");
            }
            return builder.toString();
        }
    }

    public class GankResultData {

        private static final String TAG = "GankResultData";

        @SerializedName("_id") public String id;
        public String createdAt;
        public String desc;
        public String publishedAt;
        public String source;
        public String type;
        public String url;
        public boolean used;
        public String who;


        @Override public String toString() {
            return TAG + "id: " +
                    this.id +
                    "\n" +
                    "createdAt: " +
                    this.createdAt +
                    "\n" +
                    "desc: " +
                    this.desc +
                    "\n" +
                    "publishedAt: " +
                    this.publishedAt +
                    "\n" +
                    "source: " +
                    this.source +
                    "\n" +
                    "type: " +
                    this.type +
                    "\n" +
                    "url: " +
                    this.url +
                    "\n" +
                    "used: " +
                    this.used +
                    "\n" +
                    "who: " +
                    this.who;
        }
    }
}
