package com.camnter.newlife.views.fragment.tablayoutfragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.widget.decorator.DividerItemDecoration;

import java.util.LinkedList;
import java.util.List;


/**
 * Description：TabLayoutFirstFragment
 * Created by：CaMnter
 * Time：2015-10-17 12:15
 */
public class TabLayoutFirstFragment extends Fragment {

    private View self;
    private static TabLayoutFirstFragment instance;
    private RecyclerView firstRV;

    private TabLayoutFirstFragment() {
    }

    public static TabLayoutFirstFragment getInstance() {
        if (instance == null) instance = new TabLayoutFirstFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.self == null) {
            this.self = inflater.inflate(R.layout.tablayout_first_fragment, null);
            this.firstRV = (RecyclerView) this.self.findViewById(R.id.first_rv);
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

            // 实例化LinearLayoutManager
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity());
            // 设置垂直布局
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            // 设置布局管理器
            this.firstRV.setLayoutManager(linearLayoutManager);

            this.firstRV.setItemAnimator(new DefaultItemAnimator());
            this.firstRV.addItemDecoration(new DividerItemDecoration(this.getActivity(), DividerItemDecoration.VERTICAL_LIST));

            // 使RecyclerView保持固定的大小，该信息被用于自身的优化
            this.firstRV.setHasFixedSize(true);
        }
        if (this.self.getParent() != null) {
            ViewGroup parent = (ViewGroup) this.self.getParent();
            parent.removeView(this.self);
        }

        return this.self;
    }


    public class FirstRecyclerViewAdapter extends EasyRecyclerViewAdapter {

        @Override
        public int[] getItemLayouts() {
            return new int[]{R.layout.item_first_recycler};
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
