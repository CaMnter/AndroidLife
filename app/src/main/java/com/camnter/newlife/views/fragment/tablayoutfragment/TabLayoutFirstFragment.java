package com.camnter.newlife.views.fragment.tablayoutfragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.easyrecyclerview.widget.EasyRecyclerView;
import com.camnter.easyrecyclerview.widget.decorator.EasyDividerItemDecoration;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseFragment;
import java.util.LinkedList;
import java.util.List;

/**
 * Description：TabLayoutFirstFragment
 * Created by：CaMnter
 * Time：2015-10-17 12:15
 */
public class TabLayoutFirstFragment extends BaseFragment {

    private static TabLayoutFirstFragment instance;
    private EasyRecyclerView firstRV;


    @SuppressLint("ValidFragment") private TabLayoutFirstFragment() {
    }


    public static TabLayoutFirstFragment getInstance() {
        if (instance == null) instance = new TabLayoutFirstFragment();
        return instance;
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.tablayout_first_fragment;
    }


    /**
     * Initialize the view in the layout
     *
     * @param self self
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(View self, Bundle savedInstanceState) {
        this.firstRV = this.findView(R.id.first_rv);
        this.firstRV.addItemDecoration(new EasyDividerItemDecoration(this.getContext(),
                EasyDividerItemDecoration.VERTICAL_LIST));
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
        FirstRecyclerViewAdapter adapter = new FirstRecyclerViewAdapter();
        List<Integer> resIds = new LinkedList<>();
        resIds.add(R.mipmap.mm_1);
        resIds.add(R.mipmap.mm_2);
        resIds.add(R.mipmap.mm_3);
        resIds.add(R.mipmap.mm_4);
        resIds.add(R.mipmap.mm_5);
        resIds.add(R.mipmap.mm_6);
        this.firstRV.setAdapter(adapter);
        adapter.setList(resIds);
    }


    public class FirstRecyclerViewAdapter extends EasyRecyclerViewAdapter {

        @Override public int[] getItemLayouts() {
            return new int[] { R.layout.item_first_recycler };
        }


        @Override
        public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int i) {
            int resId = (int) this.getList().get(i);
            ImageView firstIV = easyRecyclerViewHolder.findViewById(R.id.first_recycler_iv);
            firstIV.setImageResource(resId);
        }


        @Override public int getRecycleViewItemType(int i) {
            return 0;
        }
    }
}
