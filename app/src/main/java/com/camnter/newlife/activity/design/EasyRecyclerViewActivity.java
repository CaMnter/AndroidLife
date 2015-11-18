package com.camnter.newlife.activity.design;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.camnter.newlife.R;
import com.camnter.newlife.adapter.easyrecyclerview.MyRecyclerViewAdapter;
import com.camnter.newlife.bean.RecyclerViewData;
import com.camnter.newlife.widget.decorator.DividerItemDecoration;

import java.util.ArrayList;


/**
 * Description：EasyRecyclerViewActivity
 * Created by：CaMnter
 * Time：2015-10-21 22:19
 */
public class EasyRecyclerViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_recycleview);
        this.recyclerView = (RecyclerView) this.findViewById(R.id.recycler_view);
        this.myRecyclerViewAdapter = new MyRecyclerViewAdapter();
        this.recyclerView.setAdapter(this.myRecyclerViewAdapter);
        this.initRecyclerView();
        this.initData();
    }

    private void initRecyclerView() {
        this.recyclerView.setAdapter(this.myRecyclerViewAdapter);

        // 实例化LinearLayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // 设置垂直布局
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // 设置布局管理器
        this.recyclerView.setLayoutManager(linearLayoutManager);

        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        // 使RecyclerView保持固定的大小，该信息被用于自身的优化
        this.recyclerView.setHasFixedSize(true);
    }

    private void initData() {
        ArrayList<RecyclerViewData> allData = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            RecyclerViewData dataSingle = new  RecyclerViewData();
            RecyclerViewData dataMultiple = new RecyclerViewData();
            String mipmapName = "mm_" + i;
            int mipmapId = this.getMipmapId(this, mipmapName);
            dataSingle.imageResId = mipmapId;
            dataMultiple.content = "Save you from anything " + "26" + "-" + i;
            dataMultiple.imageResId = mipmapId;
            allData.add(dataSingle);
            allData.add(dataMultiple);
        }
        this.myRecyclerViewAdapter.setList(allData);
        this.myRecyclerViewAdapter.notifyDataSetChanged();
    }

    public int getMipmapId(Context context, String mipmapName) {
        return context.getResources().getIdentifier(mipmapName,
                "mipmap", context.getPackageName());
    }

}
