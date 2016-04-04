package com.camnter.newlife.framework.rxandroid;

import android.os.Bundle;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.RxChildData;
import com.camnter.newlife.bean.RxData;
import com.camnter.newlife.core.BaseAppCompatActivity;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

/**
 * Description：RxMapActivity
 * Created by：CaMnter
 * Time：2015-12-01 17:28
 */
public class RxMapActivity extends BaseAppCompatActivity {

    public static final int KEY = 206;
    public static final String VALUE = "Save you from anything";

    private TextView rxMapOneTV;
    private TextView rxMapTwoTV;
    private TextView rxFlatMapThrTV;
    private TextView rxLiftFouTV;

    private Subscription rxOneSubscription;
    private Subscription rxTwoSubscription;
    private Subscription rxThrSubscription;
    private Subscription rxFouSubscription;


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
        this.rxLiftFouTV = (TextView) this.findViewById(R.id.rx_map_fou_tv);
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
        this.rxOneSubscription = Observable.just(KEY).map(new Func1<Integer, String>() {
            @Override public String call(Integer integer) {
                switch (integer) {
                    case KEY:
                        return VALUE;
                    default:
                        return VALUE;
                }
            }
        }).subscribe(new Subscriber<String>() {
            @Override public void onCompleted() {

            }


            @Override public void onError(Throwable e) {

            }


            @Override public void onNext(String s) {
                RxMapActivity.this.rxMapOneTV.setText(s);
            }
        });

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
        this.rxTwoSubscription = Observable.from(data).map(new Func1<RxData, Long>() {
            @Override public Long call(RxData rxData) {
                return rxData.getId();
            }
        }).subscribe(new Subscriber<Long>() {
            @Override public void onCompleted() {

            }


            @Override public void onError(Throwable e) {

            }


            @Override public void onNext(Long aLong) {
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
        RxChildData[] childData = { childData1, childData2, childData3 };
        parentData.setChildDatas(childData);

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
        this.rxThrSubscription = Observable.from(new RxData[] { parentData })
                                           .flatMap(new Func1<RxData, Observable<RxChildData>>() {
                                               @Override
                                               public Observable<RxChildData> call(RxData rxData) {
                                                   return Observable.from(rxData.getChildDatas());
                                               }
                                           })
                                           .subscribe(new Subscriber<RxChildData>() {
                                               @Override public void onCompleted() {

                                               }


                                               @Override public void onError(Throwable e) {

                                               }


                                               @Override
                                               public void onNext(RxChildData rxChildData) {
                                                   String text
                                                           = RxMapActivity.this.rxFlatMapThrTV.getText()
                                                                                              .toString();
                                                   text += rxChildData.getChildContent() + " ";
                                                   RxMapActivity.this.rxFlatMapThrTV.setText(text);
                                               }
                                           });

        /**
         * 当含有 lift() 时：
         * 1.lift() 创建了一个 Observable 后，加上之前的原始 Observable，已经有两个 Observable 了；
         * 2.而同样地，新 Observable 里的新 OnSubscribe 加上之前的原始 Observable 中的原始 OnSubscribe，也
         * 就有了两个 OnSubscribe；
         * 3.当用户调用经过 lift() 后的 Observable 的 subscribe() 的时候，使用的是 lift() 所返回的新
         * 的 Observable ，于是它所触发的 onSubscribe.call(subscriber)，也是用的新 Observable 中的
         * 新 OnSubscribe，即在 lift() 中生成的那个 OnSubscribe；
         * 4.而这个新 OnSubscribe 的 call() 方法中的 onSubscribe ，就是指的原始 Observable 中的原始
         * OnSubscribe ，在这个 call() 方法里，新 OnSubscribe 利用 operator.call(subscriber) 生成
         * 了一个新的 Subscriber（Operator 就是在这里，通过自己的 call() 方法将新 Subscriber 和原始
         * Subscriber 进行关联，并插入自己的『变换』代码以实现变换），然后利用这个新 Subscriber 向原始
         * Observable 进行订阅。
         * 这样就实现了 lift() 过程，有点像一种代理机制，通过事件拦截和处理实现事件序列的变换。
         */
        this.rxFouSubscription = Observable.from(new Integer[] { 6, 7 })
                                           .lift(new Observable.Operator<String, Integer>() {
                                               @Override
                                               public Subscriber<? super Integer> call(final Subscriber<? super String> subscriber) {
                                                   return new Subscriber<Integer>() {
                                                       @Override public void onCompleted() {

                                                       }


                                                       @Override public void onError(Throwable e) {

                                                       }


                                                       @Override
                                                       public void onNext(Integer integer) {
                                                           subscriber.onNext(integer + "");
                                                       }
                                                   };
                                               }
                                           })
                                           .subscribe(new Subscriber<String>() {
                                               @Override public void onCompleted() {

                                               }


                                               @Override public void onError(Throwable e) {

                                               }


                                               @Override public void onNext(String s) {
                                                   String text
                                                           = RxMapActivity.this.rxLiftFouTV.getText()
                                                                                           .toString();
                                                   text += s + " ";
                                                   RxMapActivity.this.rxLiftFouTV.setText(text);
                                               }
                                           });
    }


    @Override protected void onDestroy() {
        this.rxOneSubscription.unsubscribe();
        this.rxTwoSubscription.unsubscribe();
        this.rxThrSubscription.unsubscribe();
        this.rxFouSubscription.unsubscribe();
        super.onDestroy();
    }
}
