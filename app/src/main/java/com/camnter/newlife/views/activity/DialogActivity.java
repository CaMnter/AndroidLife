package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.ToastUtils;
import com.camnter.newlife.widget.CustomDialog;
import com.camnter.newlife.widget.MenuDialog;

/**
 * Description：DialogActivity
 * Created by：CaMnter
 * Time：2015-12-13 15:36
 */
public class DialogActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private MenuDialog menuDialog;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_dialog;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.menuDialog = new MenuDialog(this);
        this.menuDialog.setCaseListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ToastUtils.show(DialogActivity.this, "Case", Toast.LENGTH_SHORT);
                DialogActivity.this.menuDialog.dismiss();
            }
        });
        this.menuDialog.setHelpListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ToastUtils.show(DialogActivity.this, "Help", Toast.LENGTH_SHORT);
                DialogActivity.this.menuDialog.dismiss();
            }
        });
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.findViewById(R.id.dialog_custom).setOnClickListener(this);
        this.findViewById(R.id.dialog_menu).setOnClickListener(this);
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
            case R.id.dialog_custom:
                CustomDialog d = new CustomDialog(this);
                d.setDuration(2000L);
                d.setContent("CustomDialog");
                d.setCanceledOnTouchOutside(false);
                d.setCallback(new CustomDialog.DialogCallback() {
                    @Override public void onDismiss() {
                        ToastUtils.showCenter(DialogActivity.this, "CustomDialog dismiss");
                    }
                });
                d.show();
                break;
            case R.id.dialog_menu:
                this.menuDialog.show();
                break;
        }
    }
}
