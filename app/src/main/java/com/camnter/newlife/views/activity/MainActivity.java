package com.camnter.newlife.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.mvp.MvpActivity;
import com.camnter.newlife.robotlegs4android.view.activity.Robotlegs4AndroidActivity;
import com.camnter.newlife.rxandroid.RxSyncActivity;
import com.camnter.newlife.views.activity.design.CoordinatorLayoutActivity;
import com.camnter.newlife.views.activity.design.EasyRecyclerViewActivity;
import com.camnter.newlife.views.activity.design.FloatingActionButtonActivity;
import com.camnter.newlife.views.activity.design.NavigationViewActivity;
import com.camnter.newlife.views.activity.design.SnackbarActivity;
import com.camnter.newlife.views.activity.design.TextInputLayoutActivity;
import com.camnter.newlife.views.activity.design.tablayout.CustomViewTabLayoutActivity;
import com.camnter.newlife.views.activity.design.tablayout.ImageSpanTabLayoutActivity;
import com.camnter.newlife.views.activity.design.tablayout.NormalTabLayoutActivity;
import com.camnter.newlife.views.activity.design.tablayout.SetIconTabLayoutActivity;
import com.camnter.newlife.views.activity.singletask.LaunchModeActivity;
import com.camnter.newlife.views.activity.util.DateUtilActivity;
import com.camnter.newlife.views.activity.util.DeviceUtilActivity;
import com.camnter.newlife.views.activity.util.ReflectionUtilActivity;
import com.camnter.newlife.views.activity.util.ResourcesUtilActivity;
import com.camnter.newlife.widget.decorator.DividerItemDecoration;

import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {


    private RecyclerView menuRV;
    public LinkedList<Class> classes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.menuRV = (RecyclerView) this.findViewById(R.id.menu_rv);


        this.classes = new LinkedList<>();

        classes.add(ImageScaleTypesActivity.class);
        classes.add(AsyncTaskActivity.class);
        classes.add(TextInputLayoutActivity.class);
        classes.add(RefreshUIActivity.class);
        classes.add(LaunchModeActivity.class);
        classes.add(NavigationViewActivity.class);
        classes.add(DeviceUtilActivity.class);
        classes.add(FloatingActionButtonActivity.class);
        classes.add(SnackbarActivity.class);
        classes.add(DateUtilActivity.class);
        classes.add(EasySlidingTabsActivity.class);
        classes.add(AutoAdjustSizeEditTextActivity.class);
        classes.add(AutoAdjustSizeTextViewActivity.class);
        classes.add(DownloadImageToGalleryActivity.class);
        classes.add(EasyRecyclerViewActivity.class);
        classes.add(NormalTabLayoutActivity.class);
        classes.add(SetIconTabLayoutActivity.class);
        classes.add(ImageSpanTabLayoutActivity.class);
        classes.add(CustomViewTabLayoutActivity.class);
        classes.add(CoordinatorLayoutActivity.class);
        classes.add(SensorManagerActivity.class);
        classes.add(MvpActivity.class);
        classes.add(SQLiteActivity.class);
        classes.add(Robotlegs4AndroidActivity.class);
        classes.add(CustomContentProviderActivity.class);
        classes.add(DownloadServiceActivity.class);
        classes.add(AIDLActivity.class);
        classes.add(ReflectionUtilActivity.class);
        classes.add(StaticReceiverActivity.class);
        classes.add(DynamicReceiverActivity.class);
        classes.add(DownloadReceiverActivity.class);
        classes.add(ResourcesUtilActivity.class);
        classes.add(LocationManagerActivity.class);
        classes.add(RxSyncActivity.class);

        MainRecyclerViewAdapter adapter = new MainRecyclerViewAdapter();
        adapter.setList(classes);
        adapter.setOnItemClickListener(new EasyRecyclerViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int i) {
                Class c = MainActivity.this.classes.get(i);
                MainActivity.this.startActivity(new Intent(MainActivity.this, c));
            }
        });
        // 实例化LinearLayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // 设置垂直布局
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // 设置布局管理器
        this.menuRV.setLayoutManager(linearLayoutManager);
        this.menuRV.setItemAnimator(new DefaultItemAnimator());
        this.menuRV.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        // 使RecyclerView保持固定的大小，该信息被用于自身的优化
        this.menuRV.setHasFixedSize(true);
        this.menuRV.setAdapter(adapter);
        setSupportActionBar(toolbar);
    }

    public class MainRecyclerViewAdapter extends EasyRecyclerViewAdapter {

        @Override
        public int[] getItemLayouts() {
            return new int[]{
                    R.layout.item_main
            };
        }

        @Override
        public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int i) {
            Class c = (Class) this.getList().get(i);
            if (c == null) return;
            TextView textView = easyRecyclerViewHolder.findViewById(R.id.main_item_tv);
            textView.setText(c.getSimpleName());
        }

        @Override
        public int getRecycleViewItemType(int i) {
            return 0;
        }
    }

}
