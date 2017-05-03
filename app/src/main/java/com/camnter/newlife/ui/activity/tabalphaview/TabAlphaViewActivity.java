package com.camnter.newlife.ui.activity.tabalphaview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.newlife.ui.fragment.tabalphaindicatorfragment.TabAlphaFragment;
import com.camnter.newlife.widget.alphaview.TabAlphaIndicator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description：TabAlphaViewActivity
 * Created by：CaMnter
 */

public class TabAlphaViewActivity extends BaseAppCompatActivity {

    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_tab_alpha_view;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        final ViewPager viewPager = (ViewPager) this.findViewById(R.id.tab_alpha_view_pager);
        viewPager.setAdapter(new PagerAdapter(this.getSupportFragmentManager(), "微信", "发现", "我"));
        final TabAlphaIndicator tabAlphaIndicator = (TabAlphaIndicator) this.findViewById(
            R.id.tab_alpha_indicator);
        tabAlphaIndicator.setViewPager(viewPager);
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


    private class PagerAdapter extends FragmentPagerAdapter {

        private List<String> contentList = new ArrayList<>();
        private List<Fragment> fragments = new ArrayList<>();


        public PagerAdapter(FragmentManager fm, String... content) {
            super(fm);
            this.contentList.addAll(Arrays.asList(content));
            // noinspection Convert2streamapi
            for (String element : this.contentList) {
                this.fragments.add(TabAlphaFragment.newInstance(element));
            }
        }


        /**
         * Return the Fragment associated with a specified position.
         */
        @Override public Fragment getItem(int position) {
            return this.fragments.get(position);
        }


        /**
         * Return the number of views available.
         */
        @Override public int getCount() {
            return this.fragments.size();
        }

    }

}
