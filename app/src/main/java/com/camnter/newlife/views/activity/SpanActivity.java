package com.camnter.newlife.views.activity;

import android.os.Bundle;
import com.camnter.easyrecyclerview.widget.EasyRecyclerView;
import com.camnter.easyrecyclerview.widget.decorator.EasyDividerItemDecoration;
import com.camnter.newlife.R;
import com.camnter.newlife.adapter.SpanRecyclerAdapter;
import com.camnter.newlife.bean.SpanData;
import com.camnter.newlife.core.BaseAppCompatActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * Description：SpanActivity
 * Created by：CaMnter
 * Time：2015-12-27 13:36
 */
public class SpanActivity extends BaseAppCompatActivity {

    private static final String CONTENT = "Save you from anything";

    private EasyRecyclerView spanRV;
    private SpanRecyclerAdapter adapter;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_span;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.spanRV = this.findView(R.id.span_rv);
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
        this.adapter = new SpanRecyclerAdapter(this);
        this.spanRV.setAdapter(adapter);
        this.spanRV.addItemDecoration(
                new EasyDividerItemDecoration(this, EasyDividerItemDecoration.VERTICAL_LIST,
                        R.drawable.bg_recycler_view_divider));

        List<SpanData> spans = new ArrayList<>();
        SpanData spanData = new SpanData();
        spanData.setContent(CONTENT);
        spanData.setType(1);
        spans.add(spanData);
        for (int i = 0; i < 23; i++) {
            SpanData s = new SpanData();
            s.setContent(CONTENT);
            s.setType(0);
            spans.add(s);
        }
        adapter.setList(spans);
        adapter.notifyDataSetChanged();
    }
}
