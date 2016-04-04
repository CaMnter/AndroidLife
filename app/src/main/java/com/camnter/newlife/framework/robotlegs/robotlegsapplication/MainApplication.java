package com.camnter.newlife.framework.robotlegs.robotlegsapplication;

import com.camnter.newlife.framework.robotlegs.robotlegscontext.MainContext;
import com.camnter.robotlegs4android.mvcs.Context;
import com.camnter.robotlegs4android.views.RobotlegsApplication;

/**
 * Description：MainApplication
 * Created by：CaMnter
 * Time：2015-10-19 13:49
 */
public class MainApplication extends RobotlegsApplication {

    public static MainApplication instance;


    public static MainApplication getInstance() {
        if (instance == null) instance = new MainApplication();
        return instance;
    }


    @Override protected Context getMvcContextInstance() {
        return new MainContext(this, true);
    }
}
