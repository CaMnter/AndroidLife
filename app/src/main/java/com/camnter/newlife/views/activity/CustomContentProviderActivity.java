package com.camnter.newlife.views.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.ProviderData;
import com.camnter.newlife.bean.SQLiteData;
import com.camnter.newlife.component.contentprovider.MessageContentProvider;
import com.camnter.newlife.widget.decorator.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Description：CustomContentProviderActivity
 * Created by：CaMnter
 * Time：2015-11-13 14:27
 */
public class CustomContentProviderActivity extends AppCompatActivity {


    private RecyclerView providerRV;
    private ProviderRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_custom_content_provider);

        this.getContentResolver().registerContentObserver(MessageContentProvider.MESSAGE_URI, true, new MessageProviderObserver(new Handler()));

        this.providerRV = (RecyclerView) this.findViewById(R.id.provider_rv);
        this.adapter = new ProviderRecyclerViewAdapter(this.getContentResolver(), MessageContentProvider.MESSAGE_URI);
        this.providerRV.setAdapter(this.adapter);
        this.initRecyclerView();
    }


    private void initRecyclerView() {
        // 实例化LinearLayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // 设置垂直布局
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // 设置布局管理器
        this.providerRV.setLayoutManager(linearLayoutManager);

        this.providerRV.setItemAnimator(new DefaultItemAnimator());
        this.providerRV.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        // 使RecyclerView保持固定的大小，该信息被用于自身的优化
        this.providerRV.setHasFixedSize(true);

        ArrayList<SQLiteData> allData = new ArrayList<>();
        allData.add(new SQLiteData());
        this.adapter.setList(allData);
        this.adapter.notifyDataSetChanged();
    }

    private class MessageProviderObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MessageProviderObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
        }
    }


    /**
     * Provider RecyclerView Adapter
     */
    private class ProviderRecyclerViewAdapter extends EasyRecyclerViewAdapter implements View.OnClickListener {

        private static final int ITEM_PROVIDER_OPERATION = 0;
        private static final int ITEM_PROVIDER_DATA = 1;

        private ContentResolver resolver;
        private Uri uri;

        public ProviderRecyclerViewAdapter(ContentResolver resolver, Uri uri) {
            this.resolver = resolver;
            this.uri = uri;
        }

        @Override
        public int[] getItemLayouts() {
            return new int[]{R.layout.item_content_provider_operation, R.layout.item_content_provider_data};
        }

        @Override
        public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int position) {
            int itemType = this.getRecycleViewItemType(position);
            switch (itemType) {
                case ITEM_PROVIDER_OPERATION:
                    easyRecyclerViewHolder.findViewById(R.id.provider_add_bt).setOnClickListener(this);
                    easyRecyclerViewHolder.findViewById(R.id.provider_del_bt).setOnClickListener(this);
                    easyRecyclerViewHolder.findViewById(R.id.provider_mod_bt).setOnClickListener(this);
                    easyRecyclerViewHolder.findViewById(R.id.provider_query_bt).setOnClickListener(this);
                    break;
                case ITEM_PROVIDER_DATA:
                    ProviderData data = (ProviderData) this.getList().get(position);
                    TextView idTV = easyRecyclerViewHolder.findViewById(R.id.provider_id_tv);
                    TextView contentTV = easyRecyclerViewHolder.findViewById(R.id.provider_content_tv);
                    idTV.setText(data.id + "");
                    contentTV.setText(data.content + "");
                    break;
            }
        }

        @Override
        public int getRecycleViewItemType(int i) {
            if (i == 0) {
                return ITEM_PROVIDER_OPERATION;
            } else {
                return ITEM_PROVIDER_DATA;
            }
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.provider_add_bt: {
                    ContentValues values = new ContentValues();
                    values.put("content", "Save you from anything");
                    this.resolver.insert(this.uri, values);
                    this.refresh();
                    break;
                }
                case R.id.provider_del_bt: {
                    this.resolver.delete(this.uri, null, null);
                    this.refresh();
                    break;
                }
                case R.id.provider_mod_bt: {
                    List<ProviderData> allData = this.queryAll();
                    int firstId = allData.get(0).id;
                    ContentValues values = new ContentValues();
                    values.put("content", UUID.randomUUID().toString());
                    String path = this.uri.toString();
                    this.resolver.update(Uri.parse(path.substring(0, path.lastIndexOf("/")) +"/message/"+ firstId), values, null, null);
                    this.refresh();
                    break;
                }
                case R.id.provider_query_bt: {
                    this.refresh();
                    break;
                }
            }
        }

        private void refresh() {
            List<ProviderData> l = this.queryAll();
            l.add(0,new ProviderData());
            this.setList(l);
            this.notifyDataSetChanged();
        }

        private List<ProviderData> queryAll() {
            List<ProviderData> allData = new ArrayList<>();
            Cursor result = this.resolver.query(this.uri, null, null, null, null);
            for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                ProviderData data = new ProviderData();
                data.id = result.getInt(result.getColumnIndex("_id"));
                data.content = result.getString(result.getColumnIndex("content"));
                allData.add(data);
            }
            result.close();
            return allData;
        }

    }

}
