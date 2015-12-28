package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.camnter.easyrecyclerview.widget.decorator.EasyDividerItemDecoration;
import com.camnter.newlife.R;
import com.camnter.newlife.adapter.SpanRecyclerAdapter;
import com.camnter.newlife.bean.SpanData;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：SpanActivity
 * Created by：CaMnter
 * Time：2015-12-27 13:36
 */
public class SpanActivity extends AppCompatActivity {

    private static final String CONTENT = "Save you from anything";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_span);
        RecyclerView spanRV = (RecyclerView) this.findViewById(R.id.span_rv);
        SpanRecyclerAdapter adapter = new SpanRecyclerAdapter(this);
        spanRV.setAdapter(adapter);
        spanRV.addItemDecoration(
                new EasyDividerItemDecoration(
                        this,
                        EasyDividerItemDecoration.VERTICAL_LIST,
                        R.drawable.bg_recycler_view_divider
                )
        );

        List<SpanData> spans = new ArrayList<>();
        SpanData spanData = new SpanData();
        spanData.setContent(CONTENT);
        spanData.setType(1);
        spans.add(spanData);
        for (int i = 0; i < 13; i++) {
            SpanData s = new SpanData();
            s.setContent(CONTENT);
            s.setType(0);
            spans.add(s);
        }
        adapter.setList(spans);
        adapter.notifyDataSetChanged();
    }


}
