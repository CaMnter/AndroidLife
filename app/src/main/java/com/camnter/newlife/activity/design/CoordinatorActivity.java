package com.camnter.newlife.activity.design;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.widget.decorator.DividerItemDecoration;

import java.util.LinkedList;
import java.util.List;


/**
 * Description：CoordinatorActivity
 * Created by：CaMnter
 * Time：2015-10-24 11:55
 */
public class CoordinatorActivity extends Activity {

    private RecyclerView coordinatorRV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_layout);
        this.coordinatorRV = (RecyclerView) this.findViewById(R.id.coordinator_rv);
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
        // 实例化LinearLayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // 设置垂直布局
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // 设置布局管理器
        this.coordinatorRV.setLayoutManager(linearLayoutManager);
        this.coordinatorRV.setItemAnimator(new DefaultItemAnimator());
        this.coordinatorRV.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        // 使RecyclerView保持固定的大小，该信息被用于自身的优化
        this.coordinatorRV.setHasFixedSize(true);
    }

    public class CoordinatorRecyclerViewAdapter extends EasyRecyclerViewAdapter {

        @Override
        public int[] getItemLayouts() {
            return new int[]{R.layout.item_coordinator_recycler};
        }

        @Override
        public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int i) {
            int resId = (int) this.getList().get(i);
            ImageView firstIV = easyRecyclerViewHolder.findViewById(R.id.first_recycler_iv);
            firstIV.setImageResource(resId);
        }

        @Override
        public int getRecycleViewItemType(int i) {
            return 0;
        }
    }

}
