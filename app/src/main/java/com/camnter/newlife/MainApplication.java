package com.camnter.newlife;

import com.camnter.newlife.utils.hotfix.HotPatchApplication;

/**
 * Description：MainApplication
 * Created by：CaMnter
 * Time：2015-10-19 13:49
 */
public class MainApplication extends HotPatchApplication {

    public static MainApplication instance;


    public static MainApplication getInstance() {
        return instance;
    }


    @Override public void onCreate() {
        instance = this;
        super.onCreate();
    }
}
