package com.camnter.newlife.views.activity.design;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：SnackbarActivity
 * Created by：CaMnter
 * Time：2015-10-15 22:39
 */
public class SnackbarActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private FloatingActionButton fab;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_snackbar;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.fab = findView(R.id.show_fb);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.fab.setOnClickListener(this);
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
            case R.id.show_fb: {
                this.popSnackbar(v, "Save you from anything");
            }
        }
    }


    public void popSnackbar(View view, CharSequence text) {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
                .setAction("CaMnter", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Toast.makeText(SnackbarActivity.this, "Hello World", Toast.LENGTH_LONG)
                             .show();
                    }
                })
                .show();
    }
}
