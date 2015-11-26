package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.camnter.newlife.R;
import com.camnter.newlife.adapter.sqlite.SQLiteRecyclerViewAdapter;
import com.camnter.newlife.bean.SQLiteData;
import com.camnter.newlife.widget.decorator.DividerItemDecoration;

import java.util.ArrayList;


public class SQLiteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SQLiteRecyclerViewAdapter sqLiteRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlite);

        this.recyclerView = (RecyclerView) this.findViewById(R.id.data_base_rv);
        this.sqLiteRecyclerViewAdapter = new SQLiteRecyclerViewAdapter(this);
        this.recyclerView.setAdapter(this.sqLiteRecyclerViewAdapter);
        this.initRecyclerView();
    }

    private void initRecyclerView() {
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

        ArrayList<SQLiteData> allData = new ArrayList<>();
        allData.add(new SQLiteData());
        this.sqLiteRecyclerViewAdapter.setList(allData);
        this.sqLiteRecyclerViewAdapter.notifyDataSetChanged();
    }

}
