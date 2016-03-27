package me.drakeet.newlife;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.camnter.newlife.R;
import rx.Subscription;
import rx.functions.Action1;

public class RxBusConsumerActivity extends AppCompatActivity {

    Subscription mSubscription;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_bus_consumer);
        mSubscription = RxBus.getInstance().toObserverable().subscribe(new Action1<Object>() {
            @Override public void call(Object event) {

                if (event instanceof RxBusActivity.TapEvent) {
                    Toast.makeText(RxBusConsumerActivity.this,
                            ((RxBusActivity.TapEvent) event).tag + " received", Toast.LENGTH_SHORT)
                         .show();
                }
            }
        });
    }


    public void onSend(View view) {
        RxBus.getInstance().send(new RxBusActivity.TapEvent("Real time tab event"));
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        mSubscription.unsubscribe();
    }
}
