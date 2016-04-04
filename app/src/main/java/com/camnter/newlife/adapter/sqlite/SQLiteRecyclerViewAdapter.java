package com.camnter.newlife.adapter.sqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.SQLiteData;
import com.camnter.newlife.component.sqlite.MySQLiteHelper;
import java.util.List;

/**
 * Description：SQLiteRecyclerViewAdapter
 * Created by：CaMnter
 * Time：2015-11-04 11:47
 */
public class SQLiteRecyclerViewAdapter extends EasyRecyclerViewAdapter
        implements View.OnClickListener {

    private static final int ITEM_SQL_LITE_OPERATION = 0;
    private static final int ITEM_SQL_LITE_DATA = 1;

    private Context context;


    public SQLiteRecyclerViewAdapter(Context context) {
        this.context = context;
    }


    @Override public int[] getItemLayouts() {
        return new int[] { R.layout.item_sql_lite_operation, R.layout.item_sql_lite_data };
    }


    @SuppressLint("SetTextI18n") @Override
    public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int position) {
        int itemType = this.getRecycleViewItemType(position);
        switch (itemType) {
            case ITEM_SQL_LITE_OPERATION:
                easyRecyclerViewHolder.findViewById(R.id.data_base_add_bt).setOnClickListener(this);
                easyRecyclerViewHolder.findViewById(R.id.data_base_del_bt).setOnClickListener(this);
                easyRecyclerViewHolder.findViewById(R.id.data_base_mod_bt).setOnClickListener(this);
                easyRecyclerViewHolder.findViewById(R.id.data_base_query_bt)
                                      .setOnClickListener(this);
                break;
            case ITEM_SQL_LITE_DATA:
                SQLiteData data = (SQLiteData) this.getList().get(position);
                TextView idTV = easyRecyclerViewHolder.findViewById(R.id.data_base_id_tv);
                TextView contentTV = easyRecyclerViewHolder.findViewById(R.id.data_base_content_tv);
                idTV.setText(data.id + "");
                contentTV.setText(data.content + "");
                break;
        }
    }


    @Override public int getRecycleViewItemType(int i) {
        if (i == 0) {
            return ITEM_SQL_LITE_OPERATION;
        } else {
            return ITEM_SQL_LITE_DATA;
        }
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.data_base_add_bt:
                MySQLiteHelper.getInstance(this.context).insert("Save you from anything");
                this.refresh();
                break;
            case R.id.data_base_del_bt: {
                MySQLiteHelper.getInstance(this.context).deleteAll();
                this.refresh();
                break;
            }
            case R.id.data_base_mod_bt: {
                MySQLiteHelper.getInstance(this.context).updateFirst();
                this.refresh();
                break;
            }
            case R.id.data_base_query_bt: {
                this.refresh();
                break;
            }
        }
    }


    private void refresh() {
        List<SQLiteData> allData = MySQLiteHelper.getInstance(this.context).queryAll();
        allData.add(0, new SQLiteData());
        this.setList(allData);
        this.notifyDataSetChanged();
    }
}
