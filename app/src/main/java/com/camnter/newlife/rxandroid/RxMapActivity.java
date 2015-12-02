package com.camnter.newlife.rxandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.bean.RxChildData;
import com.camnter.newlife.bean.RxData;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
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
    private TextView rxFlatMapThrTV;

    private Subscription rxOneSubscription;
    private Subscription rxTwoSubscription;
    private Subscription rxThrSubscription;

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
        this.rxFlatMapThrTV = (TextView) this.findViewById(R.id.rx_map_thr_tv);
    }

    private void initData() {

        /**
         * map一对一的类型转换
         * 通过map改变订阅者接受的参数
         * 传入的是Integer，改后变为String
         * 订阅者接收到的也是String
         */
        this.rxOneSubscription = Observable.just(KEY).map(new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                switch (integer) {
                    case KEY:
                        return VALUE;
                    default:
                        return VALUE;
                }
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
         * map一对一的类型转换
         * 通过map改变订阅者接受的参数
         * 传入的是RxData，改后变为Long
         * 订阅者接收到的也是Long
         */
        this.rxTwoSubscription = Observable.from(data).map(new Func1<RxData, Long>() {
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

        RxData parentData = new RxData();
        RxChildData childData1 = new RxChildData();
        childData1.setChildContent("childData1");
        RxChildData childData2 = new RxChildData();
        childData2.setChildContent("childData2");
        RxChildData childData3 = new RxChildData();
        childData3.setChildContent("childData3");
        RxChildData[] childData = {childData1, childData2, childData3};
        parentData.setChildDatas(childData);

        this.rxThrSubscription = Observable.from(new RxData[]{parentData}).flatMap(new Func1<RxData, Observable<RxChildData>>() {
            @Override
            public Observable<RxChildData> call(RxData rxData) {
                return Observable.from(rxData.getChildDatas());
            }
        }).subscribe(new Subscriber<RxChildData>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(RxChildData rxChildData) {
                String text = RxMapActivity.this.rxFlatMapThrTV.getText().toString();
                text += rxChildData.getChildContent() + " ";
                RxMapActivity.this.rxFlatMapThrTV.setText(text);
            }
        });

    }

    @Override
    protected void onDestroy() {
        this.rxOneSubscription.unsubscribe();
        this.rxTwoSubscription.unsubscribe();
        this.rxThrSubscription.unsubscribe();
        super.onDestroy();
    }
}
