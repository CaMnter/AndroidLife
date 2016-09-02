package com.camnter.newlife.ui.activity.agera;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.google.android.agera.BaseObservable;
import com.google.android.agera.Updatable;
import java.util.UUID;

/**
 * Description：AgeraClickActivity
 * Created by：CaMnter
 * Time：2016-05-31 16:49
 */
public class AgeraClickActivity extends BaseAppCompatActivity {

    @BindView(R.id.agera_click_button) Button clickButton;
    @BindView(R.id.agera_click_text) TextView clickText;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_agera_click;
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
        ClickAgeraObservable clickObservable = new ClickAgeraObservable();
        clickObservable.addUpdatable(new ClickUpdate());
        this.clickButton.setOnClickListener(clickObservable);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }


    private class ClickUpdate implements Updatable {
        /**
         * Called when an event has occurred.
         */
        @SuppressLint("SetTextI18n") @Override public void update() {
            clickText.setText("ClickObservable: " + UUID.randomUUID().toString());
        }
    }


    private class ClickAgeraObservable extends BaseObservable implements View.OnClickListener {

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override public void onClick(View v) {
            dispatchUpdate();
        }
    }
}
