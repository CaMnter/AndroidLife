package com.camnter.newlife.ui.mvvm.v;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.easyrecyclerview.widget.EasyRecyclerView;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.ratingrank.RatingFund;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.newlife.databinding.ActivityRatingRankBinding;
import com.camnter.newlife.ui.mvvm.mock.Injection;
import com.camnter.newlife.ui.mvvm.vm.RatingRankViewModel;
import com.camnter.newlife.widget.RatingBar;

/**
 * Description：RatingRankActivity
 * Created by：CaMnter
 */

public class RatingRankActivity extends BaseAppCompatActivity {

    private ActivityRatingRankBinding rankBinding;

    private RatingRankViewModel viewModel;

    private EasyRecyclerView recyclerView;
    private RatingRankAdapter adapter;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_rating_rank;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        this.rankBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
            R.layout.activity_rating_rank, null, false);
        this.viewModel = this.findOrCreateViewModel();
        this.recyclerView = this.rankBinding.ratingRecyclerView;
        this.rankBinding.setViewModel(this.viewModel);
    }


    private RatingRankViewModel findOrCreateViewModel() {
        return new RatingRankViewModel(this.getApplicationContext(),
            Injection.provideRatingRankRepository());
    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {
        this.viewModel.query(this);
    }


    @Override protected void onDestroy() {
        this.viewModel.onActivityDestroyed();
        super.onDestroy();
    }

}
