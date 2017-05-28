package com.camnter.newlife.ui.activity.rxjava;

import android.os.Bundle;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.RxChildData;
import com.camnter.newlife.bean.RxData;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;

/**
 * Description：RxMapActivity
 * Created by：CaMnter
 * Time：2015-12-01 17:28
 */
public class RxJavaMapActivity extends BaseAppCompatActivity {

    public static final int KEY = 206;
    public static final String VALUE = "Save you from anything";
    private static final String TAG = RxJavaMapActivity.class.getSimpleName();

    private TextView rxMapOneTV;
    private TextView rxMapTwoTV;
    private TextView rxFlatMapThrTV;

    private CompositeDisposable disposable = new CompositeDisposable();


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_rx_map;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.rxMapOneTV = (TextView) this.findViewById(R.id.rx_map_one_tv);
        this.rxMapTwoTV = (TextView) this.findViewById(R.id.rx_map_two_tv);
        this.rxFlatMapThrTV = (TextView) this.findViewById(R.id.rx_map_thr_tv);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    @Override protected void initData() {

        /**
         * map一对一的类型转换
         * 通过map改变订阅者接受的参数
         * 传入的是Integer，改后变为String
         * 订阅者接收到的也是String
         */
        this.disposable.add(
            Flowable
                .just(KEY)
                .map(new Function<Integer, String>() {
                    @Override public String apply(@NonNull Integer integer) throws Exception {
                        switch (integer) {
                            case KEY:
                                return VALUE;
                            default:
                                return VALUE;
                        }
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override public void accept(@NonNull String s) throws Exception {
                        RxJavaMapActivity.this.rxMapOneTV.setText(s);
                    }
                })
        );

        RxData data1 = new RxData();
        data1.setId(106L);
        RxData data2 = new RxData();
        data2.setId(206L);
        RxData data3 = new RxData();
        data3.setId(266L);
        RxData[] data = { data1, data2, data3 };

        /**
         * map一对一的类型转换
         * 通过map改变订阅者接受的参数
         * 传入的是RxData，改后变为Long
         * 订阅者接收到的也是Long
         */
        this.disposable.add(
            Flowable
                .fromArray(data)
                .map(new Function<RxData, Long>() {
                    @Override public Long apply(@NonNull RxData rxData) throws Exception {
                        return rxData.getId();
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override public void accept(@NonNull Long aLong) throws Exception {
                        String text = RxJavaMapActivity.this.rxMapTwoTV.getText().toString();
                        text += aLong + " ";
                        RxJavaMapActivity.this.rxMapTwoTV.setText(text);
                    }
                })
        );

        RxData parentData = new RxData();
        RxChildData childData1 = new RxChildData();
        childData1.setChildContent("childData1");
        RxChildData childData2 = new RxChildData();
        childData2.setChildContent("childData2");
        RxChildData childData3 = new RxChildData();
        childData3.setChildContent("childData3");
        RxChildData[] childData = { childData1, childData2, childData3 };
        parentData.setChildData(childData);
        /**
         * flatMap一对多的类型转换
         * flatMap() 和 map() 有一个相同点：它也是把传入的参数转化之后返回另一个对象。
         * 和 map() 不同的是， flatMap() 中返回的是个 Observable 对象，
         * 并且这个 Observable 对象并不是被直接发送到了 Subscriber 的回调方法中。
         * flatMap() 的原理是这样的：
         * 1. 使用传入的事件对象创建一个 Observable 对象；
         * 2. 并不发送这个 Observable, 而是将它激活，于是它开始发送事件；
         * 3. 每一个创建出来的 Observable 发送的事件，都被汇入同一个 Observable，而
         * 这个 Observable 负责将这些事件统一交给 Subscriber 的回调方法。这三个步骤，把
         * 事件拆成了两级，通过一组新创建的 Observable 将初始的对象『铺平』之后通过统一路径
         * 分发了下去。而这个『铺平』就是 flatMap() 所谓的 flat
         */
        this.disposable.add(
            Flowable
                .fromArray(parentData)
                .flatMap(new Function<RxData, Publisher<RxChildData>>() {
                    @Override public Publisher<RxChildData> apply(@NonNull RxData rxData)
                        throws Exception {
                        return Flowable.fromArray(rxData.getChildData());
                    }
                })
                .subscribe(new Consumer<RxChildData>() {
                    @Override public void accept(@NonNull RxChildData rxChildData)
                        throws Exception {
                        String text
                            = RxJavaMapActivity.this.rxFlatMapThrTV.getText()
                            .toString();
                        text += rxChildData.getChildContent() + " ";
                        RxJavaMapActivity.this.rxFlatMapThrTV.setText(text);
                    }
                })
        );
    }


    @Override protected void onDestroy() {
        this.disposable.clear();
        super.onDestroy();
    }

}
