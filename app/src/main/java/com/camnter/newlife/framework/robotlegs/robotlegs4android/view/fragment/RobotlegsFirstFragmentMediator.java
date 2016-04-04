package com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.event.LoginEvent;
import com.camnter.robotlegs4android.base.Event;
import com.camnter.robotlegs4android.base.Listener;
import com.camnter.robotlegs4android.core.IListener;
import com.camnter.robotlegs4android.core.IMediator;
import com.camnter.robotlegs4android.mvcs.Mediator;

/**
 * Description：TabLayoutFirstFragmentMediator
 * Created by：CaMnter
 * Time：2015-11-06 17:34
 */
public class RobotlegsFirstFragmentMediator extends Mediator implements View.OnClickListener {

    public RobotlegsFirstFragment fragment;
    public FragmentActivity activity;

    private Button firstBT;
    private TextView firstTV;
    private ImageView firstIV;
    private TextView controllerTV;


    /**
     * {@inheritDoc}
     * {@linkplain IMediator #onRegister}
     */
    @Override public void onRegister() {
        this.fragment = (RobotlegsFirstFragment) this.getViewComponent();

        this.activity = this.fragment.getActivity();
        this.initViews();
        this.initData();
        this.initListeners();
    }


    private void initViews() {
        this.firstBT = (Button) this.fragment.self.findViewById(R.id.first_bt);
        this.firstTV = (TextView) this.fragment.self.findViewById(R.id.first_tv);
        this.firstIV = (ImageView) this.fragment.self.findViewById(R.id.first_iv);
        this.controllerTV = (TextView) this.fragment.self.findViewById(R.id.first_controller_tv);
    }


    private void initData() {
        this.firstTV.setText("The ONE created by robotlegs4android frame");
    }


    private void initListeners() {
        this.firstBT.setOnClickListener(this);

        /*
         * listening your custom event（such as listening to an USER_LOGIN_SUCCESS type of LoginEvent）
         * listening from Model layer to View layer in here
         * 监听你的自定义事件（例如监听一个USER_LOGIN_SUCCESS_FROM_MODEL_TO_CONTROLLER_AND_VIEW类型的LoginEvent）
         * 在这里监听从Model层到View层
         */
        this.getEventMap()
            .mapListener(this.getEventDispatcher(),
                    LoginEvent.USER_LOGIN_SUCCESS_FROM_MODEL_TO_VIEW, new Listener() {
                        /**
                         * {@inheritDoc}
                         * <p/>
                         * {@linkplain IListener #onHandle}
                         *
                         * @param event
                         */
                        @Override public void onHandle(Event event) {
                            if (event instanceof LoginEvent) {
                                RobotlegsFirstFragmentMediator.this.firstIV.setVisibility(
                                        View.VISIBLE);
                            }
                        }
                    }, null, false, 0, true);

        /*
         * listening your custom event（such as listening to an USER_LOGIN_SUCCESS type of LoginEvent）
         * listening from Controller layer to View layer in here
         * 监听你的自定义事件（例如监听一个USER_LOGIN_SUCCESS_FROM_CONTROLLER_TO_VIEW类型的LoginEvent）
         * 在这里监听从Controller层到View层
         */
        this.getEventMap()
            .mapListener(this.getEventDispatcher(),
                    LoginEvent.USER_LOGIN_SUCCESS_FROM_CONTROLLER_TO_VIEW, new Listener() {
                        /**
                         * {@inheritDoc}
                         * <p/>
                         * {@linkplain IListener #onHandle}
                         *
                         * @param event
                         */
                        @Override public void onHandle(Event event) {
                            if (event instanceof LoginEvent) {
                                RobotlegsFirstFragmentMediator.this.controllerTV.setVisibility(
                                        View.VISIBLE);
                            }
                        }
                    }, null, false, 0, true);
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.first_bt: {
                /*
                 * you can send a your custom event from Model layer to Controller layer,and the
                 * frame will search the event configuration from your custom context
                 * 你可以发送一个你自定义的事件从Model层到Controller层，并且框架会去你自定义的context搜索
                 * 这个事件的配置
                 */
                LoginEvent loginEvent = new LoginEvent(LoginEvent.USER_LOGIN);
                loginEvent.name = "CaMnter";
                loginEvent.password = "Save you from anything";
                this.dispatch(loginEvent);
            }
        }
    }
}
