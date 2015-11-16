package com.camnter.newlife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.camnter.newlife.R;

import java.util.LinkedList;

/**
 * Description：
 * Created by：CaMnter
 * Time：2015-10-27 17:17
 */
public class MainAdapter extends BaseAdapter {

    private Context context;
    private LinkedList<Class> classes;

    public MainAdapter(Context context, LinkedList<Class> classes) {
        this.context = context;
        this.classes = classes;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return this.classes.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return this.classes.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Class c = (Class) this.getItem(position);
        if (c == null) return null;
        MainViewHolder holder;
        if (convertView != null) {
            holder = (MainViewHolder) convertView.getTag();
        } else {
            holder = new MainViewHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.item_main, null);
            holder.textView = (TextView) convertView.findViewById(R.id.main_item_tv);
        }
        holder.textView.setText(c.getSimpleName());
        convertView.setTag(holder);
        return convertView;
    }
    static class MainViewHolder {
        TextView textView;
    }

}
