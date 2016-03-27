package me.drakeet.newlife;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.camnter.newlife.R;

public class RxBusActivity extends AppCompatActivity {

    public static class TapEvent {
        public String tag;


        public TapEvent(String tag) {
            this.tag = tag;
        }
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_bus);
    }


    public void onSend(View view) {
        RxBus.getInstance().send(new TapEvent("Lazy tab event"));
    }


    public void onStartConsumer(View view) {
        startActivity(new Intent(this, RxBusConsumerActivity.class));
    }
}
