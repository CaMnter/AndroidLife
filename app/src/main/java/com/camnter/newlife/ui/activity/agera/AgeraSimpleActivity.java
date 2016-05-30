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
import com.google.android.agera.Repositories;
import com.google.android.agera.Repository;
import com.google.android.agera.Supplier;
import com.google.android.agera.Updatable;
import java.util.UUID;

/**
 * Description：AgeraSimpleActivity
 * Created by：CaMnter
 * Time：2016-05-30 16:21
 */
public class AgeraSimpleActivity extends BaseAppCompatActivity {

    @Bind(R.id.agera_observable_text_one) TextView observableOneText;
    @Bind(R.id.agera_observable_text_two) TextView observableTwoText;

    private Observable observable = new Observable() {
        @Override public void addUpdatable(@NonNull Updatable updatable) {
            updatable.update();
        }


        @Override public void removeUpdatable(@NonNull Updatable updatable) {

        }
    };

    private Updatable updatableOne = new Updatable() {
        @Override public void update() {
            observableOneText.setText("Jud: " + UUID.randomUUID().toString());
        }
    };

    private Supplier<String> supplier = new Supplier<String>() {
        @NonNull @Override public String get() {
            return "Jud: " + UUID.randomUUID().toString();
        }
    };

    private Repository<String> repository = Repositories
            .repositoryWithInitialValue("Tes")
            .observe()
            .onUpdatesPerLoop()
            .thenGetFrom(this.supplier)
            .compile();

    private Updatable updatableTwo = new Updatable() {
        @Override public void update() {
            observableTwoText.setText("Jud: " + UUID.randomUUID().toString());
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
        this.repository.addUpdatable(this.updatableTwo);
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


    public void call1(View view) {
        observable.addUpdatable(this.updatableOne);
    }
}
