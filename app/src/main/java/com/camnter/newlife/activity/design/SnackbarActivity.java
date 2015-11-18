package com.camnter.newlife.activity.design;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.camnter.newlife.R;


/**
 * Description：SnackbarActivity
 * Created by：CaMnter
 * Time：2015-10-15 22:39
 */
public class SnackbarActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton showFB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_snackbar);
        this.showFB = (FloatingActionButton) this.findViewById(R.id.show_fb);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.show_fb);
        fab.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_fb: {
                this.popSnackbar(v,"Save you from anything");
            }
        }
    }

    public void popSnackbar(View view, CharSequence text) {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).setAction("CaMnter", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SnackbarActivity.this,"Hello World",Toast.LENGTH_LONG).show();
            }
        }).show();
    }

}
