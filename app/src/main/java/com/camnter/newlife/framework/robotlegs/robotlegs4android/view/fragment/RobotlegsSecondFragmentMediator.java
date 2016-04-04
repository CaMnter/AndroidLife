package com.camnter.newlife.framework.robotlegs.robotlegs4android.view.fragment;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.event.LoginEvent;
import com.camnter.newlife.widget.decorator.DividerItemDecoration;
import com.camnter.robotlegs4android.base.Event;
import com.camnter.robotlegs4android.base.Listener;
import com.camnter.robotlegs4android.core.IListener;
import com.camnter.robotlegs4android.core.IMediator;
import com.camnter.robotlegs4android.mvcs.Mediator;
import java.util.LinkedList;
import java.util.List;

/**
 * Description：RobotlegsSecondFragmentMediator
 * Created by：CaMnter
 * Time：2015-11-09 14:39
 */
public class RobotlegsSecondFragmentMediator extends Mediator {

    private RobotlegsSecondFragment fragment;

    private RecyclerView secondRV;


    /**
     * {@inheritDoc}
     * {@linkplain IMediator #onRegister}
     */
    @Override public void onRegister() {
        super.onRegister();
        this.fragment = (RobotlegsSecondFragment) this.getViewComponent();
        this.initViews();
        this.initListeners();
    }


    private void initViews() {
        this.secondRV = (RecyclerView) this.fragment.self.findViewById(R.id.second_rv);
        SecondRecyclerViewAdapter adapter = new SecondRecyclerViewAdapter();
        List<Integer> resIds = new LinkedList<>();
        resIds.add(R.mipmap.mm_1);
        resIds.add(R.mipmap.mm_2);
        resIds.add(R.mipmap.mm_3);
        this.secondRV.setAdapter(adapter);
        adapter.setList(resIds);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this.fragment.getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        this.secondRV.setLayoutManager(linearLayoutManager);
        this.secondRV.setItemAnimator(new DefaultItemAnimator());
        this.secondRV.addItemDecoration(new DividerItemDecoration(this.fragment.getActivity(),
                DividerItemDecoration.VERTICAL_LIST));
        this.secondRV.setHasFixedSize(true);
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
                                RobotlegsSecondFragmentMediator.this.secondRV.setVisibility(
                                        View.VISIBLE);
                            }
                        }
                    }, null, false, 0, true);
    }


    public class SecondRecyclerViewAdapter extends EasyRecyclerViewAdapter {

        @Override public int[] getItemLayouts() {
            return new int[] { R.layout.item_robotlegs_second_recycler };
        }


        @Override
        public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int i) {
            int resId = (int) this.getList().get(i);
            ImageView secondIV = easyRecyclerViewHolder.findViewById(R.id.second_recycler_iv);
            secondIV.setImageResource(resId);
        }


        @Override public int getRecycleViewItemType(int i) {
            return 0;
        }
    }
}
