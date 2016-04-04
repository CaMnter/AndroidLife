package com.camnter.newlife.framework.robotlegs.robotlegscontext;

import com.camnter.newlife.framework.robotlegs.robotlegs4android.controller.Login;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.event.LoginEvent;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.model.UserModel;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.activity.Robotlegs4AndroidActivity;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.activity.Robotlegs4AndroidActivityMediator;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsFirstFragment;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsFirstFragmentMediator;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsFourthFragment;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsFourthFragmentMediator;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsSecondFragment;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsSecondFragmentMediator;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsThirdFragment;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsThirdFragmentMediator;
import com.camnter.robotlegs4android.mvcs.Context;

/**
 * Description：MainContext
 * Created by：CaMnter
 * Time：2015-11-06 16:22
 */
public class MainContext extends Context {

    public MainContext(Object contextView, Boolean autoStartup) {
        super(contextView, autoStartup);
    }


    /**
     * set your mvc relation
     * 设置你的mvc关系
     * <p/>
     * Add the view map
     * Link the View and View the corresponding Mediator
     * 添加view映射
     * 将View 和 View 对应的 Mediator 联系起来
     * <p/>
     * Injection as an singleton, instantiate the singleton
     * 注入实例，实例化单例
     * <p/>
     * Add Event (Event) with the connection of the Command
     * 添加事件（Event）与Command的联系
     */
    @Override public void setMvcRelation() {

        /*
         * view映射
         * 将View 和 View 对应的 Mediator 联系起来
         * Add the view map
         * Link the View and View the corresponding Mediator
         */
        this.getMediatorMap()
            .mapView(Robotlegs4AndroidActivity.class, Robotlegs4AndroidActivityMediator.class, null,
                    true, true);
        this.getMediatorMap()
            .mapView(RobotlegsFirstFragment.class, RobotlegsFirstFragmentMediator.class, null, true,
                    true);
        this.getMediatorMap()
            .mapView(RobotlegsSecondFragment.class, RobotlegsSecondFragmentMediator.class, null,
                    true, true);
        this.getMediatorMap()
            .mapView(RobotlegsThirdFragment.class, RobotlegsThirdFragmentMediator.class, null, true,
                    true);
        this.getMediatorMap()
            .mapView(RobotlegsFourthFragment.class, RobotlegsFourthFragmentMediator.class, null,
                    true, true);

        /*
         * 注入实现 实例化单例
         * Injection as an singleton, instantiate the singleton
         */
        this.getInjector().mapSingleton(UserModel.class, "");

        /*
         * 添加事件与Command的联系
         * Add Event (Event) with the connection of the Command
         */
        this.getCommandMap().mapEvent(LoginEvent.USER_LOGIN, Login.class, null, false);
        this.getCommandMap()
            .mapEvent(LoginEvent.USER_LOGIN_SUCCESS_FROM_MODEL_TO_CONTROLLER, Login.class, null,
                    false);
    }
}