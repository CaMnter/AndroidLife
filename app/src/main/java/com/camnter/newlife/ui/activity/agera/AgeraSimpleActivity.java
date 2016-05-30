package com.camnter.newlife.ui.activity.agera;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.google.android.agera.Observable;
import com.google.android.agera.Updatable;
import java.util.UUID;

/**
 * Description：AgeraSimpleActivity
 * Created by：CaMnter
 * Time：2016-05-30 16:21
 */
public class AgeraSimpleActivity extends BaseAppCompatActivity implements Updatable {

    @Bind(R.id.agera_observable_text) TextView observableText;

    private Observable observable = new Observable() {
        @Override public void addUpdatable(@NonNull Updatable updatable) {
            updatable.update();
        }


        @Override public void removeUpdatable(@NonNull Updatable updatable) {

        }
    };


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_agera_simple;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.setTitle("AgeraSimpleActivity");
        ButterKnife.bind(this);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }


    public void call(View view) {
        observable.addUpdatable(this);
    }


    /**
     * Called when an event has occurred.
     */
    @Override public void update() {
        this.observableText.setText("Jud: " + UUID.randomUUID().toString());
    }
}
