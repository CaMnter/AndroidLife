package com.camnter.newlife.ui.mvvm.view;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.camnter.mvvm.MVVMViewAdapter;
import com.camnter.mvvm.view.MVVMActivity;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.ratingrank.RatingFund;
import com.camnter.newlife.databinding.ActivityRatingRankBinding;
import com.camnter.newlife.ui.mvvm.mock.Injection;
import com.camnter.newlife.ui.mvvm.vm.RatingRankViewModel;

/**
 * Description：RatingRankActivity
 * Created by：CaMnter
 */

public class RatingRankActivity extends MVVMActivity {

    private ActivityRatingRankBinding binding;
    private RatingRankViewModel<RatingFund> viewModel;


    @Override protected int getLayoutId() {
        return R.layout.activity_rating_rank;
    }


    /**
     * @param binding binding
     */
    @Override protected void castingBinding(@NonNull ViewDataBinding binding) {
        if (binding instanceof ActivityRatingRankBinding) {
            this.binding = (ActivityRatingRankBinding) binding;
        }
    }


    /**
     * on after data binding
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void onAfterDataBinding(@NonNull Bundle savedInstanceState) {
        this.viewModel = new RatingRankViewModel<>(this,
            Injection.provideRatingRankRepository());
        MVVMViewAdapter<RatingFund> adapter = new MVVMViewAdapter<RatingFund>(this) {
            @Override public int[] getItemLayouts() {
                return new int[] { R.layout.item_rating_ranking };
            }
        };
        adapter.setVHandler(this.viewModel);
        this.viewModel.setAdapter(adapter);
        this.binding.setAdapter(adapter);
        this.binding.setViewModel(this.viewModel);
        this.viewModel.query(this);
    }

}
