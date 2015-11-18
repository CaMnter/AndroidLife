package com.camnter.newlife.activity.design;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.camnter.newlife.R;

/**
 * Description：NavigationViewActivity
 * Created by：CaMnter
 * Time：2015-10-12 22:18
 */
public class NavigationViewActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavigationView.OnNavigationItemSelectedListener navigationViewListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
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

    private void settingNavigationView(){
        // 添加item的监听事件
        this.navigationView.setNavigationItemSelectedListener(this.navigationViewListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_natigation_view_layout);
        this.navigationView = (NavigationView) this.findViewById(R.id.navigation);
        this.drawerLayout = (DrawerLayout) this.findViewById(R.id.drawerLayout);

        this.settingNavigationView();

    }
}
