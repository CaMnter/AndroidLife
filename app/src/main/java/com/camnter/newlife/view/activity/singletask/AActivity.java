package com.camnter.newlife.view.activity.singletask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.camnter.newlife.R;


/**
 * Description：AActivity
 * Created by：CaMnter
 * Time：2015-09-23 15:28
 */
public class AActivity extends AppCompatActivity implements View.OnClickListener {
    private Button startBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_a);
        this.startBT = (Button) this.findViewById(R.id.start_bt);
        this.startBT.setOnClickListener(this);
    }

    /**
     * 动态改变ListView的高度
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        if(listView == null) return;

        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return;
        }

        int totalHeight = 0;

        // 开始计算ListView里所有Item加起来的总高度
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // 高度 ＝ 所有分割线高度 + Item总高度
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_bt:{
                this.startActivity(new Intent(this, VActivity.class));
                break;
            }
        }
    }
}
