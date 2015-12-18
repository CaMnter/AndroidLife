package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.widget.CustomPopupWindow;

/**
 * Description：PopupWindowActivity
 * Created by：CaMnter
 * Time：2015-12-18 00:05
 */
public class PopupWindowActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView customTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_popupwindow);
        this.customTV = (TextView) this.findViewById(R.id.popupwindow_custom);
        this.customTV.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popupwindow_custom: {
                CustomPopupWindow.PopupWindowBuilder.getInstance(this)
                        .getPopupWindow()
                        .showAtLocation(this.customTV, 0, 0, 0);
                break;
            }
        }
    }
}
