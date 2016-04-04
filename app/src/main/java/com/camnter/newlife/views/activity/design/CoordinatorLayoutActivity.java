package com.camnter.newlife.views.activity.design;

import android.os.Bundle;
import android.widget.ImageView;
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.easyrecyclerview.widget.EasyRecyclerView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseActivity;
import java.util.LinkedList;
import java.util.List;

/**
 * Description：CoordinatorActivity
 * Created by：CaMnter
 * Time：2015-10-24 11:55
 */
public class CoordinatorLayoutActivity extends BaseActivity {

    private EasyRecyclerView coordinatorRV;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_coordinator_layout;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.coordinatorRV = this.findView(R.id.coordinator_rv);
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
        CoordinatorRecyclerViewAdapter adapter = new CoordinatorRecyclerViewAdapter();
        List<Integer> resIds = new LinkedList<>();
        resIds.add(R.mipmap.mm_1);
        resIds.add(R.mipmap.mm_2);
        resIds.add(R.mipmap.mm_3);
        resIds.add(R.mipmap.mm_4);
        resIds.add(R.mipmap.mm_5);
        resIds.add(R.mipmap.mm_6);
        this.coordinatorRV.setAdapter(adapter);
        adapter.setList(resIds);
    }


    public class CoordinatorRecyclerViewAdapter extends EasyRecyclerViewAdapter {

        @Override public int[] getItemLayouts() {
            return new int[] { R.layout.item_coordinator_recycler };
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
