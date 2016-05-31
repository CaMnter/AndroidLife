package com.camnter.newlife.ui.activity.agera;

import com.camnter.newlife.R;
import com.camnter.newlife.ui.activity.MainActivity;

/**
 * Description：AgeraActivity
 * Created by：CaMnter
 * Time：2016-05-30 15:24
 */
public class AgeraActivity extends MainActivity {
    @Override protected void setListData() {
        this.showTag = false;
        this.classes.add(AgeraSimpleActivity.class);
        this.classes.add(AgeraClickActivity.class);
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_agera;
    }
}
