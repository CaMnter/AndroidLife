package com.camnter.newlife.widget.synchorizontalscrollview;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Description：SyncHorizontalListViewAdapter
 * Created by：CaMnter
 */

public abstract class SyncHorizontalListViewAdapter extends BaseListViewAdapter {

    public void refitSyncHorizontalListViewHeight(@NonNull final ListView listView) {
        int totalHeight = 0;
        final int count = this.getCount();
        for (int i = 0; i < count; i++) {
            View itemView = this.getView(i, null, listView);
            itemView.measure(0, 0);
            totalHeight += itemView.getMeasuredHeight();
        }
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.height = totalHeight + (listView.getDividerHeight() * (count - 1));
        listView.setLayoutParams(layoutParams);
    }

}
