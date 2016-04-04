package com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
 * Description：RobotlegsThirdFragmentMediator
 * Created by：CaMnter
 * Time：2015-11-10 14:28
 */
public class RobotlegsThirdFragmentMediator extends Mediator {

    private RobotlegsThirdFragment fragment;

    private RecyclerView thirdRV;


    /**
     * {@inheritDoc}
     * {@linkplain IMediator #onRegister}
     */
    @Override public void onRegister() {
        super.onRegister();
        this.fragment = (RobotlegsThirdFragment) this.getViewComponent();
        this.initViews();
        this.initListeners();
    }


    private void initViews() {
        this.thirdRV = (RecyclerView) this.fragment.self.findViewById(R.id.third_rv);
        ThirdRecyclerViewAdapter adapter = new ThirdRecyclerViewAdapter();
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
        this.thirdRV.setAdapter(adapter);
        adapter.setList(resIds);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this.fragment.getActivity(), 2);

        this.thirdRV.setLayoutManager(gridLayoutManager);
        this.thirdRV.setItemAnimator(new DefaultItemAnimator());
        this.thirdRV.setHasFixedSize(true);
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
                                RobotlegsThirdFragmentMediator.this.thirdRV.setVisibility(
                                        View.VISIBLE);
                            }
                        }
                    }, null, false, 0, true);
    }


    public class ThirdRecyclerViewAdapter extends EasyRecyclerViewAdapter {

        @Override public int[] getItemLayouts() {
            return new int[] { R.layout.item_robotlegs_third_recycler };
        }


        @Override
        public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int i) {
            int resId = (int) this.getList().get(i);
            ImageView thirdRV = easyRecyclerViewHolder.findViewById(R.id.third_recycler_iv);
            thirdRV.setImageResource(resId);
        }


        @Override public int getRecycleViewItemType(int i) {
            return 0;
        }
    }
}
