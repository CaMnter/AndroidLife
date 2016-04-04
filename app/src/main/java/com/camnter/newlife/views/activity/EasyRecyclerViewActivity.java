package com.camnter.newlife.views.activity;

import android.content.Context;
import android.os.Bundle;
import com.camnter.easyrecyclerview.widget.EasyRecyclerView;
import com.camnter.easyrecyclerview.widget.decorator.EasyDividerItemDecoration;
import com.camnter.newlife.R;
import com.camnter.newlife.adapter.easyrecyclerview.MyRecyclerViewAdapter;
import com.camnter.newlife.bean.RecyclerViewData;
import com.camnter.newlife.core.BaseAppCompatActivity;
import java.util.ArrayList;

/**
 * Description：EasyRecyclerViewActivity
 * Created by：CaMnter
 * Time：2015-10-21 22:19
 */
public class EasyRecyclerViewActivity extends BaseAppCompatActivity {

    private EasyRecyclerView easyRV;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_recycleview;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.easyRV = (EasyRecyclerView) this.findViewById(R.id.recycler_view);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    @Override protected void initData() {
        MyRecyclerViewAdapter myRecyclerViewAdapter = new MyRecyclerViewAdapter();
        this.easyRV.setAdapter(myRecyclerViewAdapter);

        // set divider
        this.easyRV.addItemDecoration(
                new EasyDividerItemDecoration(this, EasyDividerItemDecoration.VERTICAL_LIST,
                        R.drawable.bg_recycler_view_divider));
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
        myRecyclerViewAdapter.setList(allData);
        myRecyclerViewAdapter.notifyDataSetChanged();
    }


    public int getMipmapId(Context context, String mipmapName) {
        return context.getResources().getIdentifier(mipmapName, "mipmap", context.getPackageName());
    }
}
