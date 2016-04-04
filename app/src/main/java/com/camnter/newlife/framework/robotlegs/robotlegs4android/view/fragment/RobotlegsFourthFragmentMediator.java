package com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.event.LoginEvent;
import com.camnter.robotlegs4android.base.Event;
import com.camnter.robotlegs4android.base.Listener;
import com.camnter.robotlegs4android.core.IListener;
import com.camnter.robotlegs4android.core.IMediator;
import com.camnter.robotlegs4android.mvcs.Mediator;
import java.util.LinkedList;
import java.util.List;

/**
 * Description：RobotlegsFourthFragmentMediator
 * Created by：CaMnter
 * Time：2015-11-10 15:00
 */
public class RobotlegsFourthFragmentMediator extends Mediator {

    private RobotlegsFourthFragment fragment;

    private RecyclerView fourthRV;


    /**
     * {@inheritDoc}
     * {@linkplain IMediator #onRegister}
     */
    @Override public void onRegister() {
        super.onRegister();
        this.fragment = (RobotlegsFourthFragment) this.getViewComponent();
        this.initViews();
        this.initListeners();
    }


    private void initViews() {
        this.fourthRV = (RecyclerView) this.fragment.self.findViewById(R.id.fourth_rv);
        FourthRecyclerViewAdapter adapter = new FourthRecyclerViewAdapter();
        List<Integer> resIds = new LinkedList<>();
        resIds.add(R.mipmap.mm_8);
        resIds.add(R.mipmap.mm_9);
        resIds.add(R.mipmap.mm_10);
        resIds.add(R.mipmap.mm_11);
        resIds.add(R.mipmap.mm_12);
        resIds.add(R.mipmap.mm_13);
        resIds.add(R.mipmap.mm_14);
        resIds.add(R.mipmap.mm_15);
        resIds.add(R.mipmap.mm_16);
        resIds.add(R.mipmap.mm_17);
        resIds.add(R.mipmap.mm_18);
        resIds.add(R.mipmap.mm_19);
        resIds.add(R.mipmap.mm_20);
        this.fourthRV.setAdapter(adapter);
        adapter.setList(resIds);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);

        this.fourthRV.setLayoutManager(staggeredGridLayoutManager);
        this.fourthRV.setItemAnimator(new DefaultItemAnimator());
        this.fourthRV.setHasFixedSize(true);
    }


    private void initListeners() {
        /*
         * listening your custom event（such as listening to an USER_LOGIN_SUCCESS type of LoginEvent）
         * listening from Controller layer to View layer in here
         * 监听你的自定义事件（例如监听一个USER_LOGIN_SUCCESS_FROM_CONTROLLER_TO_VIEW类型的LoginEvent）
         * 在这里监听从Controller层到View层
         */
        this.getEventMap()
            .mapListener(this.getEventDispatcher(),
                    LoginEvent.USER_LOGIN_SUCCESS_FROM_MODEL_TO_VIEW, new Listener() {
                        /**
                         * {@inheritDoc}
                         * <p/>
                         * {@linkplain IListener #onHandle}
                         *
                         * @param event
                         */
                        @Override public void onHandle(Event event) {
                            if (event instanceof LoginEvent) {
                                RobotlegsFourthFragmentMediator.this.fourthRV.setVisibility(
                                        View.VISIBLE);
                            }
                        }
                    }, null, false, 0, true);
    }


    public class FourthRecyclerViewAdapter extends EasyRecyclerViewAdapter {

        private static final long MIN = 500L;
        private static final long MAX = 1000L;


        @Override public int[] getItemLayouts() {
            return new int[] { R.layout.item_robotlegs_fourth_recycler };
        }


        @Override
        public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int i) {
            int resId = (int) this.getList().get(i);
            ImageView fourthRV = easyRecyclerViewHolder.findViewById(R.id.fourth_recycler_iv);
            fourthRV.setImageResource(resId);
            ViewGroup.LayoutParams params = fourthRV.getLayoutParams();
            params.height = (int) Math.round(Math.random() * (MAX - MIN) + MIN);
            fourthRV.setLayoutParams(params);
        }


        @Override public int getRecycleViewItemType(int i) {
            return 0;
        }
    }
}
