package com.camnter.newlife.adapter.easyrecyclerview;

import android.widget.ImageView;
import android.widget.TextView;
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.RecyclerViewData;

/**
 * Description：MyRecyclerViewAdapter
 * Created by：CaMnter
 * Time：2015-10-21 17:00
 */
public class MyRecyclerViewAdapter extends EasyRecyclerViewAdapter {

    private static final int MULTIPLE_ITEM_TYPE = 0;
    private static final int SINGLE_ITEM_TYPE = 1;


    /**
     * 请返回RecycleView加载的布局Id数组
     *
     * @return 布局Id数组
     */
    @Override public int[] getItemLayouts() {
        return new int[] { R.layout.item_recyclerview_multiple, R.layout.item_recyclerview_single };
    }


    /**
     * 对接了onBindViewHolder
     * onBindViewHolder里的逻辑写在这
     *
     * @param viewHolder viewHolder
     * @param position position
     */
    @Override public void onBindRecycleViewHolder(EasyRecyclerViewHolder viewHolder, int position) {
        int itemType = this.getRecycleViewItemType(position);
        RecyclerViewData data = this.getItem(position);
        switch (itemType) {
            case MULTIPLE_ITEM_TYPE: {
                TextView multipleTV = viewHolder.findViewById(R.id.recycler_view_mul_tv);
                ImageView multipleIV = viewHolder.findViewById(R.id.recycler_view_mul_iv);
                multipleTV.setText(data.content);
                multipleIV.setImageResource(data.imageResId);
                break;
            }
            case SINGLE_ITEM_TYPE: {
                ImageView singleIV = viewHolder.findViewById(R.id.recycler_view_single_iv);
                singleIV.setImageResource(data.imageResId);
                break;
            }
        }
    }


    /**
     * 如果是多布局的话，请写判断逻辑
     * 单布局可以不写
     *
     * @param position Item position
     * @return 布局Id数组中的index
     */
    @Override public int getRecycleViewItemType(int position) {
        if (position % 2 == 0) {
            return SINGLE_ITEM_TYPE;
        } else {
            return MULTIPLE_ITEM_TYPE;
        }
    }
}
