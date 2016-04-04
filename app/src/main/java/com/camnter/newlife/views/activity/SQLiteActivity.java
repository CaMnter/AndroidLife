package com.camnter.newlife.views.activity;

import android.os.Bundle;
import com.camnter.easyrecyclerview.widget.EasyRecyclerView;
import com.camnter.easyrecyclerview.widget.decorator.EasyDividerItemDecoration;
import com.camnter.newlife.R;
import com.camnter.newlife.adapter.sqlite.SQLiteRecyclerViewAdapter;
import com.camnter.newlife.bean.SQLiteData;
import com.camnter.newlife.core.BaseAppCompatActivity;
import java.util.ArrayList;

public class SQLiteActivity extends BaseAppCompatActivity {

    private EasyRecyclerView recyclerView;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_sqlite;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.recyclerView = this.findView(R.id.data_base_rv);
        this.recyclerView.addItemDecoration(
                new EasyDividerItemDecoration(this, EasyDividerItemDecoration.VERTICAL_LIST));
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
        SQLiteRecyclerViewAdapter sqLiteRecyclerViewAdapter = new SQLiteRecyclerViewAdapter(this);
        this.recyclerView.setAdapter(sqLiteRecyclerViewAdapter);
        ArrayList<SQLiteData> allData = new ArrayList<>();
        allData.add(new SQLiteData());
        sqLiteRecyclerViewAdapter.setList(allData);
        sqLiteRecyclerViewAdapter.notifyDataSetChanged();
    }
}
