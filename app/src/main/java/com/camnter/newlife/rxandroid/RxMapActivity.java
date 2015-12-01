package com.camnter.newlife.rxandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.bean.RxData;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Description：RxMapActivity
 * Created by：CaMnter
 * Time：2015-12-01 17:28
 */
public class RxMapActivity extends AppCompatActivity {

    public static final int KEY = 206;
    public static final String VALUE = "Save you from anything";


    private TextView rxMapOneTV;
    private TextView rxMapTwoTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_rx_map);
        this.initViews();
        this.initData();
    }

    private void initViews() {
        this.rxMapOneTV = (TextView) this.findViewById(R.id.rx_map_one_tv);
        this.rxMapTwoTV = (TextView) this.findViewById(R.id.rx_map_two_tv);
    }

    private void initData() {
        /**
         * 通过map改变订阅者接受的参数
         * 传入的是Integer，改后变为String
         * 订阅者接收到的也是String
         */
        Observable.just(KEY).map(new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                return VALUE;
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                RxMapActivity.this.rxMapOneTV.setText(s);
            }
        });

        RxData data1 = new RxData();
        data1.setId(106L);
        RxData data2 = new RxData();
        data2.setId(206L);
        RxData data3 = new RxData();
        data3.setId(266L);
        RxData[] data = {data1, data2, data3};

        /**
         * 通过map改变订阅者接受的参数
         * 传入的是RxData，改后变为Long
         * 订阅者接收到的也是Long
         */
        Observable.from(data).map(new Func1<RxData, Long>() {
            @Override
            public Long call(RxData rxData) {
                return rxData.getId();
            }
        }).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long aLong) {
                String text = RxMapActivity.this.rxMapTwoTV.getText().toString();
                text += aLong + " ";
                RxMapActivity.this.rxMapTwoTV.setText(text);
            }
        });

    }

}
