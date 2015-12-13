package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.camnter.newlife.R;
import com.camnter.newlife.widget.CustomDialog;
import com.camnter.newlife.widget.MenuDialog;

/**
 * Description：DialogActivity
 * Created by：CaMnter
 * Time：2015-12-13 15:36
 */
public class DialogActivity extends AppCompatActivity implements View.OnClickListener {

    private MenuDialog menuDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_dialog);
        this.menuDialog = MenuDialog.DialogBuilder.getInstance(this)
                .setCaseListenser(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(DialogActivity.this, "case", Toast.LENGTH_SHORT).show();
                        DialogActivity.this.menuDialog.dismiss();
                    }
                })
                .setHelpListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(DialogActivity.this, "Help", Toast.LENGTH_SHORT).show();
                        DialogActivity.this.menuDialog.dismiss();
                    }
                })
                .getDialog();
        this.initListeners();
    }

    private void initListeners() {
        this.findViewById(R.id.dialog_custom).setOnClickListener(this);
        this.findViewById(R.id.dialog_menu).setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_custom:
                CustomDialog.DialogBuilder.getInstance(this)
                        .setDuration(2000L)
                        .setContent("CustomDialog")
                        .setCanceledOnTouchOutside(false)
                        .setCallback(new CustomDialog.DialogCallback() {
                            @Override
                            public void onDismiss() {
                                Toast.makeText(DialogActivity.this, "CustomDialog dismiss", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .getDialog()
                        .show();
                break;
            case R.id.dialog_menu:
                this.menuDialog.show();
                break;
        }
    }
}
