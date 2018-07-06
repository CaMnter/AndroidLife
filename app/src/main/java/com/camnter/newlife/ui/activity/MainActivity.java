package com.camnter.newlife.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.camnter.annotation.processor.annotation.SaveTest;
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.easyrecyclerview.widget.EasyRecyclerView;
import com.camnter.easyrecyclerview.widget.decorator.EasyDividerItemDecoration;
import com.camnter.load.simple.plugin.LoadSimplePluginActivity;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.newlife.ui.activity.agera.AgeraActivity;
import com.camnter.newlife.ui.activity.asm.AsmActivity;
import com.camnter.newlife.ui.activity.classloader.ClassLoaderActivity;
import com.camnter.newlife.ui.activity.classloader.ExternalLoadSoActivity;
import com.camnter.newlife.ui.activity.classloader.InternalLoadSoActivity;
import com.camnter.newlife.ui.activity.design.CoordinatorLayoutActivity;
import com.camnter.newlife.ui.activity.design.FloatingActionButtonActivity;
import com.camnter.newlife.ui.activity.design.NavigationViewActivity;
import com.camnter.newlife.ui.activity.design.SnackbarActivity;
import com.camnter.newlife.ui.activity.design.TextInputLayoutActivity;
import com.camnter.newlife.ui.activity.design.tablayout.CustomViewTabLayoutActivity;
import com.camnter.newlife.ui.activity.design.tablayout.ImageSpanTabLayoutActivity;
import com.camnter.newlife.ui.activity.design.tablayout.NormalTabLayoutActivity;
import com.camnter.newlife.ui.activity.design.tablayout.SetIconTabLayoutActivity;
import com.camnter.newlife.ui.activity.easylikearea.EasyLikeAreaQZoneActivity;
import com.camnter.newlife.ui.activity.easylikearea.EasyLikeAreaStyleActivity;
import com.camnter.newlife.ui.activity.easylikearea.EasyLikeAreaTopicActivity;
import com.camnter.newlife.ui.activity.easyrecyclerviewsidebar.CircleImageSectionActivity;
import com.camnter.newlife.ui.activity.easyrecyclerviewsidebar.LetterSectionActivity;
import com.camnter.newlife.ui.activity.easyrecyclerviewsidebar.RoundImageSectionActivity;
import com.camnter.newlife.ui.activity.hotfix.HotFixActivity;
import com.camnter.newlife.ui.activity.ipc.AIDLActivity;
import com.camnter.newlife.ui.activity.ipc.MessengerActivity;
import com.camnter.newlife.ui.activity.javassist.JavassistActivity;
import com.camnter.newlife.ui.activity.jsbridge.JsBridgeActivity;
import com.camnter.newlife.ui.activity.lrucache.LruCacheActivity;
import com.camnter.newlife.ui.activity.rxjava.RxJavaAsyncActivity;
import com.camnter.newlife.ui.activity.rxjava.RxJavaMapActivity;
import com.camnter.newlife.ui.activity.singletask.LaunchModeActivity;
import com.camnter.newlife.ui.activity.smartrouter.SmartRouterActivity;
import com.camnter.newlife.ui.activity.smartsave.SmartSaveExampleActivity;
import com.camnter.newlife.ui.activity.tabalphaview.TabAlphaViewActivity;
import com.camnter.newlife.ui.activity.util.DateUtilActivity;
import com.camnter.newlife.ui.activity.util.DeviceUtilActivity;
import com.camnter.newlife.ui.activity.util.ReflectionUtilActivity;
import com.camnter.newlife.ui.activity.util.ResourcesUtilActivity;
import com.camnter.newlife.ui.activity.xfermode.XfermodesActivity;
import com.camnter.newlife.ui.databinding.view.RatingRankActivity;
import com.camnter.newlife.utils.permissions.AppSettingsDialog;
import com.camnter.newlife.utils.permissions.EasyPermissions;
import com.camnter.newlife.widget.autoresizetextview.Log;
import com.camnter.newlife.widget.screenshots.ScreenshotsListener;
import com.camnter.smartsave.SmartSave;
import com.camnter.smartsave.annotation.Save;
import java.util.ArrayList;
import java.util.List;

@SaveTest
public class MainActivity extends BaseAppCompatActivity
    implements EasyPermissions.PermissionCallbacks {

    private static final int RC_STORAGE_PERM = 2331;
    protected ArrayList<Class> classes;
    protected boolean showTag = true;
    private MenuRecyclerViewAdapter adapter;

    @Save(R.id.menu_list)
    public EasyRecyclerView menuRecyclerView;
    private ScreenshotsListener screenshotsListener;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        // SmartSave
        SmartSave.save(this);
        this.menuRecyclerView = this.findView(R.id.menu_list);
        this.menuRecyclerView.addItemDecoration(
            new EasyDividerItemDecoration(this, EasyDividerItemDecoration.VERTICAL_LIST));

        this.storagePermissions();
        this.screenshotsListener = ScreenshotsListener.newInstance(this);
        this.screenshotsListener.start();

        this.screenshotsListener.setListener((imagePath) -> {
                Log.d("[imagePath] = " + imagePath);
                this.showToast(imagePath, Toast.LENGTH_LONG);
            }
        );
    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {
        this.adapter.setOnItemClickListener((view, i) -> {
            Class c = MainActivity.this.classes.get(i);
            MainActivity.this.startActivity(new Intent(MainActivity.this, c));
        });
    }


    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {
        this.classes = new ArrayList<>();
        this.setListData();
        this.adapter = new MenuRecyclerViewAdapter();
        this.adapter.setList(classes);
        this.menuRecyclerView.setAdapter(adapter);
        this.replaceAppIcon();
    }


    private void replaceAppIcon() {
        final ComponentName defaultComponentName = this.getComponentName();
        final ComponentName newComponentName = new ComponentName(this.getBaseContext(),
            "com.camnter.newlife.icon.round");
        final PackageManager packageManager = this.getApplicationContext().getPackageManager();

        // this.disableComponent(packageManager,defaultComponentName);
        this.enableComponent(packageManager, newComponentName);
    }


    private void enableComponent(@NonNull final PackageManager packageManager,
                                 @NonNull final ComponentName componentName) {
        packageManager.setComponentEnabledSetting(componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);
    }


    private void disableComponent(@NonNull final PackageManager packageManager,
                                  @NonNull final ComponentName componentName) {
        packageManager.setComponentEnabledSetting(componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);
    }


    protected void setListData() {
        this.classes.add(ImageScaleTypesActivity.class);
        this.classes.add(AsyncTaskActivity.class);
        this.classes.add(TextInputLayoutActivity.class);
        this.classes.add(RefreshUIActivity.class);
        this.classes.add(LaunchModeActivity.class);
        this.classes.add(NavigationViewActivity.class);
        this.classes.add(DeviceUtilActivity.class);
        this.classes.add(FloatingActionButtonActivity.class);
        this.classes.add(SnackbarActivity.class);
        this.classes.add(DateUtilActivity.class);
        this.classes.add(EasySlidingTabsActivity.class);
        this.classes.add(AutoAdjustSizeEditTextActivity.class);
        this.classes.add(AutoAdjustSizeTextViewActivity.class);
        this.classes.add(DownloadImageToGalleryActivity.class);
        this.classes.add(EasyRecyclerViewActivity.class);
        this.classes.add(NormalTabLayoutActivity.class);
        this.classes.add(SetIconTabLayoutActivity.class);
        this.classes.add(ImageSpanTabLayoutActivity.class);
        this.classes.add(CustomViewTabLayoutActivity.class);
        this.classes.add(CoordinatorLayoutActivity.class);
        this.classes.add(SensorManagerActivity.class);
        this.classes.add(SQLiteActivity.class);
        this.classes.add(CustomContentProviderActivity.class);
        this.classes.add(DownloadServiceActivity.class);
        this.classes.add(AIDLActivity.class);
        this.classes.add(MessengerActivity.class);
        this.classes.add(ReflectionUtilActivity.class);
        this.classes.add(StaticReceiverActivity.class);
        this.classes.add(DynamicReceiverActivity.class);
        this.classes.add(DownloadReceiverActivity.class);
        this.classes.add(ResourcesUtilActivity.class);
        this.classes.add(LocationManagerActivity.class);
        this.classes.add(RxJavaAsyncActivity.class);
        this.classes.add(RxJavaMapActivity.class);
        this.classes.add(DialogActivity.class);
        this.classes.add(PopupWindowActivity.class);
        this.classes.add(TagTextViewActivity.class);
        this.classes.add(EasyFlowLayoutActivity.class);
        this.classes.add(SpanActivity.class);
        this.classes.add(OttoActivity.class);
        this.classes.add(AnimatorActivity.class);
        this.classes.add(CanvasClipViewActivity.class);
        this.classes.add(RoundImageViewActivity.class);
        this.classes.add(XfermodesActivity.class);
        this.classes.add(EasyArcLoadingActivity.class);
        this.classes.add(EasyLikeAreaTopicActivity.class);
        this.classes.add(EasyLikeAreaQZoneActivity.class);
        this.classes.add(EasyLikeAreaStyleActivity.class);
        this.classes.add(LetterSectionActivity.class);
        this.classes.add(CircleImageSectionActivity.class);
        this.classes.add(RoundImageSectionActivity.class);
        this.classes.add(LruCacheActivity.class);
        this.classes.add(VolleyActivity.class);
        this.classes.add(AgeraActivity.class);
        this.classes.add(ClassLoaderActivity.class);
        this.classes.add(ExternalLoadSoActivity.class);
        this.classes.add(InternalLoadSoActivity.class);
        this.classes.add(HotFixActivity.class);
        this.classes.add(RemoteViewsActivity.class);
        this.classes.add(JsBridgeActivity.class);
        this.classes.add(RatingRankActivity.class);
        this.classes.add(TabAlphaViewActivity.class);
        this.classes.add(SmartSaveExampleActivity.class);
        this.classes.add(SmartRouterActivity.class);
        this.classes.add(LoadSimplePluginActivity.class);
        this.classes.add(JavassistActivity.class);
        this.classes.add(AsmActivity.class);
    }


    private class MenuRecyclerViewAdapter extends EasyRecyclerViewAdapter {

        final int camnterColor = 0xffC04F90;
        final int drakeetColor = 0xff5B64AF;


        @Override
        public int[] getItemLayouts() {
            return new int[] { R.layout.item_main };
        }


        @SuppressLint("SetTextI18n")
        @Override
        public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int i) {
            Class c = (Class) this.getList().get(i);
            if (c == null) return;
            TextView content = easyRecyclerViewHolder.findViewById(R.id.main_item_tv);
            TextView type = easyRecyclerViewHolder.findViewById(R.id.main_item_type);

            content.setText(c.getSimpleName());

            if (showTag) {
                type.setVisibility(View.VISIBLE);
                if (c.getName().contains("drakeet")) {
                    type.setText("drakeet");
                    type.setTextColor(drakeetColor);
                    type.setBackgroundResource(R.drawable.bg_main_item_type_drakeet);
                } else {
                    type.setText("CaMnter");
                    type.setTextColor(camnterColor);
                    type.setBackgroundResource(R.drawable.bg_main_item_type_camnter);
                }
            } else {
                type.setVisibility(View.GONE);
            }
        }


        @Override
        public int getRecycleViewItemType(int i) {
            return 0;
        }
    }


    public void storagePermissions() {
        String[] perms = { Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permissions, do the thing!
            Toast.makeText(this, "TODO: Location and Contacts things", Toast.LENGTH_LONG).show();
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this,
                " SD 卡权限",
                RC_STORAGE_PERM, perms);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    protected void onDestroy() {
        this.screenshotsListener.stop();
        super.onDestroy();
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        this.showToast("onPermissionsGranted:" + requestCode + ":" + perms.size());
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        this.showToast("onPermissionsDenied:" + requestCode + ":" + perms.size());
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

}
