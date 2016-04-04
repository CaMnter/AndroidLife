package com.camnter.newlife.views.activity.design;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：NavigationViewActivity
 * Created by：CaMnter
 * Time：2015-10-12 22:18
 */
public class NavigationViewActivity extends BaseAppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavigationView.OnNavigationItemSelectedListener navigationViewListener
            = new NavigationView.OnNavigationItemSelectedListener() {
        @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
            /**
             *  NavigationView选项事件处理
             *
             *  id 对应 NavigationView中定义的menu itemid
             */
            switch (menuItem.getItemId()) {
                case R.id.navigation_sub_setting:
                    break;
                case R.id.navigation_subheader:
                    break;
                case R.id.navigation_sub_plan:
                    break;
                case R.id.navigation_sub_share:
                    break;
                case R.id.navigation_sub_time:
                    break;
                case R.id.navigation_sub_clear:
                    break;
            }

            // 收起
            drawerLayout.closeDrawer(navigationView);
            return false;
        }
    };


    private void settingNavigationView() {
        // 添加item的监听事件
        this.navigationView.setNavigationItemSelectedListener(this.navigationViewListener);
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_natigation_view_layout;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.navigationView = this.findView(R.id.navigation);
        this.drawerLayout = this.findView(R.id.drawerLayout);
        this.settingNavigationView();
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }
}
