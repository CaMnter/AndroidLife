package com.camnter.mvvm;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description：BindingAdapter
 * @author CaMnter
 */

public abstract class BindingAdapter<T> extends RecyclerView.Adapter<BindingHolder> {

    private final Context context;
    private final List<T> list;
    private final LayoutInflater inflater;


    public BindingAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    /**
     * Please return RecyclerView loading layout Id array
     * 请返回 RecyclerView 加载的布局 Id 数组
     *
     * @return 布局 Id 数组
     */
    public abstract int[] getItemLayouts();


    /**
     * Please write judgment logic when more layout
     * and not write when single layout
     * 如果是多布局的话，请写判断逻辑
     * 单布局可以不写
     *
     * @param position Item position
     * @return 布局 Id 数组中的 index
     */
    protected int getRecycleViewItemType(int position) {
        return 0;
    }


    /**
     * get the itemType by position
     * 根据 position 获取 itemType
     *
     * @param position Item position
     * @return 默认 ItemType 等于 0
     */
    @Override
    public int getItemViewType(int position) {
        return this.getRecycleViewItemType(position);
    }


    /**
     * @param holder holder
     * @param position position
     * @param viewType viewType
     */
    protected void onBindRecycleViewHolder(BindingHolder holder, int position, int viewType) {
        // Nothing to do
    }


    /******************
     * Magic extension
     ******************/

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType < 0) return null;
        if (this.getItemLayouts() == null) return null;
        int[] layoutIds = this.getItemLayouts();
        if (layoutIds.length < 1) return null;

        int itemLayoutId;
        itemLayoutId = layoutIds.length == 1 ? layoutIds[0] : layoutIds[viewType];
        return new BindingHolder<>(
            DataBindingUtil.inflate(this.inflater, itemLayoutId, parent, false));
    }


    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        final T itemValue = this.list.get(position);
        final ViewDataBinding binding = holder.getBinding();
        binding.setVariable(com.camnter.mvvm.BR.position, position);
        binding.setVariable(com.camnter.mvvm.BR.itemValue, itemValue);
        binding.executePendingBindings();

        this.onBindRecycleViewHolder(holder, position, this.getRecycleViewItemType(position));
    }


    @Override
    public int getItemCount() {
        return this.list.size();
    }


    /**********************
     * Some smart methods *
     **********************/

    public int getListSize() {
        return this.list.size();
    }


    public T getItem(int position) {
        return this.list.get(position);
    }


    public T getItemByPosition(int position) {
        return this.getItem(position);
    }


    public void setList(@NonNull final List<T> list) {
        this.list.clear();
        if (list.size() == 0) return;
        this.list.addAll(list);
        this.notifyDataSetChanged();
    }


    public void clear() {
        this.list.clear();
        this.notifyDataSetChanged();
    }


    public void remove(@NonNull final T t) {
        this.list.remove(t);
        this.notifyDataSetChanged();
    }


    @NonNull
    public List<T> getList() {
        return this.list;
    }


    public void addAll(@NonNull final Collection<T> list) {
        this.list.addAll(list);
    }

}
