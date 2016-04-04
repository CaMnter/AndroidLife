package com.camnter.newlife.framework.robotlegs.robotlegs4android.view.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.Toast;
import com.camnter.newlife.R;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.event.LoginEvent;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.adapter.Robotlegs4AndroidAdapter;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsFirstFragment;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsFourthFragment;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsSecondFragment;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment.RobotlegsThirdFragment;
import com.camnter.robotlegs4android.base.Event;
import com.camnter.robotlegs4android.base.Listener;
import com.camnter.robotlegs4android.core.IListener;
import com.camnter.robotlegs4android.core.IMediator;
import com.camnter.robotlegs4android.mvcs.Mediator;

/**
 * Description：MainActivityMediator
 * Created by：CaMnter
 * Time：2015-11-07 23:45
 */
public class Robotlegs4AndroidActivityMediator extends Mediator {

    private static final String TAG = "MainActivityMediator";

    private Robotlegs4AndroidActivity activity;

    private TabLayout tabLayout;
    private ViewPager viewPager;


    /**
     * {@inheritDoc}
     * {@linkplain IMediator #onRegister}
     */
    @Override public void onRegister() {
        this.activity = (Robotlegs4AndroidActivity) this.getViewComponent();
        this.initViews();
        this.initData();
        this.initListeners();
    }


    private void initViews() {
        this.tabLayout = (TabLayout) this.activity.findViewById(R.id.tab_layout_tl);
        this.viewPager = (ViewPager) this.activity.findViewById(R.id.view_pager_vp);
        this.viewPager.setOffscreenPageLimit(4);
    }


    private void initData() {
        String[] tabTitles = { "ONE", "TWO", "THR", "FOU" };
        Fragment[] fragments = { RobotlegsFirstFragment.getInstance(),
                RobotlegsSecondFragment.getInstance(), RobotlegsThirdFragment.getInstance(),
                RobotlegsFourthFragment.getInstance() };
        Robotlegs4AndroidAdapter adapter = new Robotlegs4AndroidAdapter(
                this.activity.getSupportFragmentManager(), fragments, tabTitles);
        this.viewPager.setAdapter(adapter);
        this.tabLayout.setupWithViewPager(this.viewPager);
    }


    private void initListeners() {
        /*
         * listening your custom event（such as listening to an USER_LOGIN_SUCCESS type of LoginEvent）
         * listening from Controller layer to View layer in here
         * 监听你的自定义事件（例如监听一个USER_LOGIN_SUCCESS_FROM_CONTROLLER_TO_VIEW类型的LoginEvent）
         * 在这里监听从Controller层到View层
         */
        this.getEventMap()
            .mapListener(this.getEventDispatcher(),
                    LoginEvent.USER_LOGIN_SUCCESS_FROM_MODEL_TO_VIEW, new ControllerListeners(),
                    null, false, 0, false);
    }


    private class ControllerListeners extends Listener {
        /**
         * {@inheritDoc}
         * <p/>
         * {@linkplain IListener #onHandle}
         */
        @Override public void onHandle(Event event) {
            if (event instanceof LoginEvent) {
                Toast.makeText(Robotlegs4AndroidActivityMediator.this.activity,
                        "MainActivityMediator:Please see the TWO、THR and FOU", Toast.LENGTH_LONG)
                     .show();
            }
        }
    }
}
