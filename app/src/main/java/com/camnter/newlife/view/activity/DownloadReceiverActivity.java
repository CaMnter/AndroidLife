package com.camnter.newlife.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.broadcastreceiver.DownloadReceiver;

/**
 * Description：
 * Created by：CaMnter
 * Time：2015-11-22 22:54
 */
public class DownloadReceiverActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String OBJECT_IMAGE_URL = "http://img.blog.csdn.net/20150913233900119";

    private TextView downloadTV;
    private Button downloadBT;
    private ImageView downloadIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_download_broadcast_receiver);
        this.initViews();
        this.initListeners();
    }

    private void initViews() {
        this.downloadTV = (TextView) this.findViewById(R.id.down_broadcast_image_tv);
        this.downloadTV.setText(OBJECT_IMAGE_URL);
        this.downloadBT = (Button) this.findViewById(R.id.down_broadcast_start_bt);
        this.downloadIV = (ImageView) this.findViewById(R.id.down_broadcast_image_iv);
    }

    private void initListeners() {
        this.downloadBT.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.down_broadcast_start_bt:
                v.setEnabled(false);
                Intent intent = new Intent(DownloadReceiver.INTENT_ACTION);
                intent.putExtra(DownloadReceiver.INTENT_DATA_IMAGE_URL, OBJECT_IMAGE_URL);
                intent.putExtra(DownloadReceiver.INTENT_TYPE, DownloadReceiver.TYPE_DOWNLOAD_START);
                this.sendBroadcast(intent);
                break;
        }
    }
}
