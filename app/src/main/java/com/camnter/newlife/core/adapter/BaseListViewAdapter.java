package com.camnter.newlife.core.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description：BaseListViewAdapter
 * Created by：CaMnter
 */

public abstract class BaseListViewAdapter extends BaseAdapter {

    private ArrayList dataList;

    private static final String EMPTY_LENGTH_STRING = "";


    public BaseListViewAdapter() {
        this.dataList = new ArrayList();
    }


    private boolean safetyCheckData() {
        return this.dataList != null;
    }


    @Override
    public int getCount() {
        return this.safetyCheckData() ? this.dataList.size() : 0;
    }


    @Override
    public Object getItem(int position) {
        return this.safetyCheckData() ? this.dataList.get(position) : null;
    }


    @Override
    public long getItemId(int position) {
        return this.safetyCheckData() ? position : -1;
    }


    @SuppressWarnings("unchecked")
    public <T> T getItemByPosition(int position) {
        return (T) this.getItem(position);
    }


    @SuppressWarnings("unchecked")
    public void setList(List list) {
        this.dataList.clear();
        if (list == null) return;
        this.dataList.addAll(list);
    }


    public void clear() {
        this.dataList.clear();
    }


    public void remove(Object o) {
        this.dataList.remove(o);
    }


    public List getList() {
        return this.dataList;
    }


    @SuppressWarnings("unchecked")
    public void addAll(Collection list) {
        this.dataList.addAll(list);
    }


    protected void safetySetText(@NonNull final TextView textView,
                                 @Nullable final String text,
                                 @NonNull final String defaultString) {
        textView.setText(TextUtils.isEmpty(text) ? defaultString : text);
    }


    protected void safetySetText(@NonNull final TextView textView,
                                 @Nullable final String text) {
        this.safetySetText(textView, text, EMPTY_LENGTH_STRING);
    }

}
