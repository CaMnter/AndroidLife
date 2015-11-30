package com.camnter.newlife.rxandroid;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.camnter.newlife.R;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;

/**
 * Description：RxSyncActivity
 * Created by：CaMnter
 * Time：2015-11-30 17:09
 */
public class RxSyncActivity extends AppCompatActivity {

    private ImageView syncRxIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_rx_sync);
        this.syncRxIV = (ImageView) this.findViewById(R.id.sync_rx_iv);
        this.initData();
    }

    private void initData() {
        Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                Drawable drawable;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = RxSyncActivity.this.getTheme().getDrawable(R.mipmap.mm_1);
                } else {
                    drawable = RxSyncActivity.this.getResources().getDrawable(R.mipmap.mm_1);
                }

                subscriber.onNext(drawable);
                subscriber.onCompleted();
            }
        }).subscribe(new Observer<Drawable>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Drawable drawable) {
                RxSyncActivity.this.syncRxIV.setImageDrawable(drawable);
            }
        });
    }

}
