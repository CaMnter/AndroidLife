package com.camnter.mvvm;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description：MVVMViewAdapter
 * Created by：CaMnter
 */

public abstract class MVVMViewAdapter<T> extends RecyclerView.Adapter<MVVMViewHolder> {

    private final Context context;
    private final List<T> list;
    private final LayoutInflater inflater;

    private WeakReference<VHandler> vHandler;


    public MVVMViewAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setVHandler(@NonNull final VHandler vHandler) {
        this.vHandler = new WeakReference<>(vHandler);
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
    @Override public int getItemViewType(int position) {
        return this.getRecycleViewItemType(position);
    }


    /**
     * @param holder holder
     * @param position position
     * @param viewType viewType
     */
    protected void onBindRecycleViewHolder(MVVMViewHolder holder, int position, int viewType) {
        // Nothing to do
    }


    /******************
     * Magic extension
     ******************/

    @SuppressWarnings("unchecked")
    @Override public MVVMViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType < 0) return null;
        if (this.getItemLayouts() == null) return null;
        int[] layoutIds = this.getItemLayouts();
        if (layoutIds.length < 1) return null;

        int itemLayoutId;
        itemLayoutId = layoutIds.length == 1 ? layoutIds[0] : layoutIds[viewType];
        return new MVVMViewHolder(
            DataBindingUtil.inflate(this.inflater, itemLayoutId, parent, false));
    }


    @Override public void onBindViewHolder(MVVMViewHolder holder, int position) {
        try {
            final T itemValue = this.list.get(position);
            final ViewDataBinding binding = holder.getBinding();
            binding.setVariable(com.camnter.mvvm.BR.position, position);
            binding.setVariable(com.camnter.mvvm.BR.itemValue, itemValue);
            binding.setVariable(com.camnter.mvvm.BR.vHandler, this.vHandler.get());
            binding.executePendingBindings();

            this.onBindRecycleViewHolder(holder, position, this.getRecycleViewItemType(position));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override public int getItemCount() {
        return this.list.size();
    }


    /************
     * Listener *
     ************/

    public interface VHandler {
    }


    /**********************
     * Some smart methods *
     **********************/

    public int getListSize() {
        return this.list.size();
    }


    @SuppressWarnings("unchecked") public T getItem(int position) {
        return this.list.get(position);
    }


    public T getItemByPosition(int position) {
        return this.getItem(position);
    }


    @SuppressWarnings("unchecked") public void setList(List list) {
        this.list.clear();
        if (list == null) return;
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
    public List getList() {
        return this.list;
    }


    @SuppressWarnings("unchecked") public void addAll(Collection list) {
        this.list.addAll(list);
    }

}
