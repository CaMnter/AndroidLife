package com.camnter.newlife.views.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.camnter.easyrecyclerview.widget.EasyRecyclerView;
import com.camnter.easyrecyclerview.widget.decorator.EasyDividerItemDecoration;
import com.camnter.newlife.R;
import com.camnter.newlife.adapter.easyrecyclerview.MyRecyclerViewAdapter;
import com.camnter.newlife.bean.RecyclerViewData;

import java.util.ArrayList;


/**
 * Description：EasyRecyclerViewActivity
 * Created by：CaMnter
 * Time：2015-10-21 22:19
 */
public class EasyRecyclerViewActivity extends AppCompatActivity {

    private EasyRecyclerView easyRV;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_recycleview);
        this.easyRV = (EasyRecyclerView) this.findViewById(R.id.recycler_view);
        this.myRecyclerViewAdapter = new MyRecyclerViewAdapter();
        this.easyRV.setAdapter(this.myRecyclerViewAdapter);

        // set divider
        this.easyRV.addItemDecoration(
                new EasyDividerItemDecoration(
                        this,
                        EasyDividerItemDecoration.VERTICAL_LIST,
                        R.drawable.bg_recycler_view_divider
                )
        );
        this.initData();
    }

    private void initData() {
        ArrayList<RecyclerViewData> allData = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            RecyclerViewData dataSingle = new RecyclerViewData();
            RecyclerViewData dataMultiple = new RecyclerViewData();
            String mipmapName = "mm_" + i;
            int mipmapId = this.getMipmapId(this, mipmapName);
            dataSingle.imageResId = mipmapId;
            dataMultiple.content = "Save you from anything " + "26" + "-" + i + "6";
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
