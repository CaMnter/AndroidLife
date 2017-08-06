package com.camnter.newlife.ui.activity.smartrouter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.easyrecyclerview.widget.EasyRecyclerView;
import com.camnter.easyrecyclerview.widget.decorator.EasyDividerItemDecoration;
import com.camnter.newlife.MainApplication;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.smartrouter.RouterCenterActivity;
import com.camnter.smartrouter.SmartRouters;
import java.util.ArrayList;

/**
 * @author CaMnter
 */

public class SmartRouterActivity extends BaseAppCompatActivity {

    protected ArrayList<Class> classes;
    private MenuRecyclerViewAdapter adapter;

    private EasyRecyclerView recyclerView;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_smart_router;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.recyclerView = this.findView(R.id.recycler_view);
        this.recyclerView.addItemDecoration(
            new EasyDividerItemDecoration(this, EasyDividerItemDecoration.VERTICAL_LIST));

    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {
        this.adapter.setOnItemClickListener((view, i) -> {
            final String className = classes.get(i).getSimpleName();
            if (SmartRouterSampleActivity.class.getSimpleName().equals(className)) {
                SmartRouters.start(this,
                    MainApplication.getScheme() + "://" +
                        "router-0x01?" +
                        "char=z&" +
                        "byte=x&" +
                        "short=2&" +
                        "int=233&" +
                        "float=233.233&" +
                        "double=2333.2333&" +
                        "long=2333&" +
                        "boolean=true&" +
                        "boxedCharacter=Z&" +
                        "boxedByte=u&" +
                        "boxedShort=3&" +
                        "boxedInteger=1233&" +
                        "boxedFloat=1233.233&" +
                        "boxedDouble=12333.2333&" +
                        "boxedLong=12333&" +
                        "boxedBoolean=true&" +
                        "boxedString=CaMnter"
                );
            } else if (CustomRouterActivity.class.getSimpleName().equals(className)) {
                /*
                 * In Application:
                 *
                 * SmartRouters.register(new Router<CustomRouterActivity>() {
                 *
                 *   @Override
                 *   public void register(@NonNull Map<String, Class<? extends Activity>> routerMapping) {
                 *       routerMapping.put(SmartRouters.getScheme() + "://" + "router-0x02",
                 *           CustomRouterActivity.class);
                 *   }
                 *
                 *
                 *   @Override
                 *   public void setFieldValue(@NonNull CustomRouterActivity activity) {
                 *       final Intent intent = activity.getIntent();
                 *       final Uri uri = intent.getData();
                 *       if (uri == null) return;
                 *
                 *       activity.exampleBoxedString = uri.getQueryParameter("boxedString");
                 *   }
                 *
                 * });
                 */
                SmartRouters.start(this,
                    MainApplication.getScheme() + "://" +
                        "router-0x02?" +
                        "boxedString=CaMnter"
                );
            } else if (RouterCenterActivity.class.getSimpleName().equals(className)) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("smart-routers://router-0x02?boxedString=CaMnter")));
            }
        });
    }


    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {
        this.classes = new ArrayList<>();
        this.classes.add(SmartRouterSampleActivity.class);
        this.classes.add(CustomRouterActivity.class);
        this.classes.add(RouterCenterActivity.class);
        this.adapter = new MenuRecyclerViewAdapter();
        this.adapter.setList(classes);
        this.recyclerView.setAdapter(adapter);
    }


    private class MenuRecyclerViewAdapter extends EasyRecyclerViewAdapter {

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
            type.setVisibility(View.INVISIBLE);
        }


        @Override
        public int getRecycleViewItemType(int i) {
            return 0;
        }
    }

}
