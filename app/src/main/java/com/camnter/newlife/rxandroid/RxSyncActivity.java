package com.camnter.newlife.rxandroid;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.utils.ThreadUtil;

import rx.Observable;
import rx.Subscriber;

/**
 * Description：RxSyncActivity
 * Created by：CaMnter
 * Time：2015-11-30 17:09
 */
public class RxSyncActivity extends AppCompatActivity {

    private static final String TAG = "RxSyncActivity";

    private TextView syncRxTV;
    private ImageView syncRxIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_rx_sync);
        this.syncRxIV = (ImageView) this.findViewById(R.id.rx_sync_iv);
        this.syncRxTV = (TextView) this.findViewById(R.id.rx_sync_tv);
        this.initData();
    }

    private void initData() {

        /**
         * create添加OnSubscribe对象（观察者）
         * 此时要实现OnSubscribe（观察者）的call方法，如果是异步的话，这里的call就应该写后台的I/O操作
         * 当你调用了subscribe方法时，就会自动调用OnSubscribe对象（观察者）的call方法去执行
         * （可以参考subscribe方法的源码）
         * 失败会走到onError方法
         * 成功的话，因为call方法有Subscriber对象，这是添加的订阅者，可以调用它的onNext或onCompleted
         */
        Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                Drawable drawable;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = RxSyncActivity.this.getTheme().getDrawable(R.mipmap.mm_1);
                } else {
                    drawable = RxSyncActivity.this.getResources().getDrawable(R.mipmap.mm_1);
                }
                RxSyncActivity.this.checkThread("create -> OnSubscribe.call()");
                /*
                 * 通知订阅者
                 */
                subscriber.onNext(drawable);
                subscriber.onCompleted();
            }
        }).subscribe(new Subscriber<Drawable>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Drawable drawable) {
                RxSyncActivity.this.checkThread("create -> Subscriber.onNext()");
                RxSyncActivity.this.syncRxIV.setImageDrawable(drawable);
            }
        });

        /**
         * 使用just 省略了create
         * 使用just，就表示不需要OnSubscribe对象了，默认发送了事件（也就是默认做了什么）。
         * 相当于实现了如下：
         * Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
         *  @Override
         *  public void call(Subscriber<? super String> subscriber) {
         *      subscriber.onNext("Save");
         *      subscriber.onNext("you");
         *      subscriber.onNext("anything");
         *      subscriber.onCompleted();
         *   }
         * });
         * 调用了3次onNext
         * 一次onCompleted
         */
        Observable.just("Save", "you", "from", "anything")
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        RxSyncActivity.this.checkThread("just -> Subscriber.onNext()");
                        String text = RxSyncActivity.this.syncRxTV.getText().toString();
                        text += s + " ";
                        RxSyncActivity.this.syncRxTV.setText(text);
                    }
                });

    }

    private void checkThread(String info) {
        Log.i(TAG, ThreadUtil.getThreadMsg(info));
    }

}
