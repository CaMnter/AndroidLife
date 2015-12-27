package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.camnter.newlife.R;
import com.camnter.newlife.adapter.SpanRecyclerAdapter;
import com.camnter.newlife.bean.SpanData;
import com.camnter.newlife.widget.decorator.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：SpanActivity
 * Created by：CaMnter
 * Time：2015-12-27 13:36
 */
public class SpanActivity extends AppCompatActivity {

    private static final String CONTENT = "Save you from anything";

    private RecyclerView spanRV;
    private SpanRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_span);
        this.spanRV = (RecyclerView) this.findViewById(R.id.span_rv);
        this.adapter = new SpanRecyclerAdapter(this);
        this.initRecyclerView();

        List<SpanData> spans = new ArrayList<>();
        SpanData spanData = new SpanData();
        spanData.setContent(CONTENT);
        spanData.setType(1);
        spans.add(spanData);
        for (int i = 0; i < 4; i++) {
            SpanData s = new SpanData();
            s.setContent(CONTENT);
            s.setType(0);
            spans.add(s);
        }
        adapter.setList(spans);
        adapter.notifyDataSetChanged();
    }

    private void initRecyclerView() {
        this.spanRV.setAdapter(this.adapter);

        // 实例化LinearLayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // 设置垂直布局
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // 设置布局管理器
        this.spanRV.setLayoutManager(linearLayoutManager);

        this.spanRV.setItemAnimator(new DefaultItemAnimator());
        this.spanRV.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        // 使RecyclerView保持固定的大小，该信息被用于自身的优化
        this.spanRV.setHasFixedSize(true);
    }

}
