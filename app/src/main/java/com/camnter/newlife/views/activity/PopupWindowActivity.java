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

    private TextView leftTV;
    private TextView centerTV;
    private TextView rightTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_popupwindow);
        this.leftTV = (TextView) this.findViewById(R.id.popupwindow_left_tv);
        this.centerTV = (TextView) this.findViewById(R.id.popupwindow_center_tv);
        this.rightTV = (TextView) this.findViewById(R.id.popupwindow_right_tv);
        this.leftTV.setOnClickListener(this);
        this.centerTV.setOnClickListener(this);
        this.rightTV.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popupwindow_left_tv: {
                CustomPopupWindow.PopupWindowBuilder.getInstance(this)
                        .getPopupWindow()
                        .showAtDropDownLeft(this.leftTV);
                break;
            }
            case R.id.popupwindow_center_tv: {
                CustomPopupWindow.PopupWindowBuilder.getInstance(this)
                        .getPopupWindow()
                        .showAtDropDownCenter(this.centerTV);
                break;
            }
            case R.id.popupwindow_right_tv:{
                    CustomPopupWindow.PopupWindowBuilder.getInstance(this)
                            .getPopupWindow()
                            .showAtDropDownRight(this.rightTV);
                break;
            }
        }
    }
}
