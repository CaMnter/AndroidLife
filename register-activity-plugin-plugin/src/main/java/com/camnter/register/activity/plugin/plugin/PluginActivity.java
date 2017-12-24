package com.camnter.register.activity.plugin.plugin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author CaMnter
 */

public class PluginActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setBackgroundColor(0xff000000);
        final TextView text = new TextView(this);
        text.setText("Plugin Activity");
        rootLayout.addView(text);
        this.setContentView(rootLayout);
    }

}
