package com.camnter.newlife.core.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description：BaseFragmentPagerAdapter
 * Created by：CaMnter
 */

public class BaseFragmentPagerAdapter<V extends Fragment> extends FragmentPagerAdapter {

    private final List<V> fragments;


    public BaseFragmentPagerAdapter(@NonNull final FragmentManager fragmentManager,
                                    @NonNull final ArrayList<V> fragments) {
        super(fragmentManager);
        this.fragments = fragments;
    }


    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }


    @SuppressWarnings("unchecked")
    public V getItemByPosition(int position) {
        return (V) this.getItem(position);
    }


    public void setList(List<V> list) {
        this.fragments.clear();
        if (list == null) return;
        this.fragments.addAll(list);
    }


    public void clear() {
        this.fragments.clear();
    }


    public void remove(V v) {
        this.fragments.remove(v);
    }


    public void addAll(Collection<V> list) {
        this.fragments.addAll(list);
    }


    @Override
    public int getCount() {
        return this.fragments.size();
    }

}
