package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.widget.CustomPopupWindow;

/**
 * Description：PopupWindowActivity
 * Created by：CaMnter
 * Time：2015-12-18 00:05
 */
public class PopupWindowActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private TextView leftTV;
    private TextView centerTV;
    private TextView rightTV;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_popupwindow;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.leftTV = (TextView) this.findViewById(R.id.popupwindow_left_tv);
        this.centerTV = (TextView) this.findViewById(R.id.popupwindow_center_tv);
        this.rightTV = (TextView) this.findViewById(R.id.popupwindow_right_tv);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.leftTV.setOnClickListener(this);
        this.centerTV.setOnClickListener(this);
        this.rightTV.setOnClickListener(this);
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
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popupwindow_left_tv: {
                CustomPopupWindow p = new CustomPopupWindow(this);
                p.showAtDropDownLeft(this.leftTV);
                break;
            }
            case R.id.popupwindow_center_tv: {
                CustomPopupWindow p = new CustomPopupWindow(this);
                p.showAtDropDownCenter(this.centerTV);
                break;
            }
            case R.id.popupwindow_right_tv: {
                CustomPopupWindow p = new CustomPopupWindow(this);
                p.showAtDropDownRight(this.rightTV);
                break;
            }
        }
    }
}
