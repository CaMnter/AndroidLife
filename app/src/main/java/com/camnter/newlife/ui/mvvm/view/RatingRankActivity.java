package com.camnter.newlife.ui.mvvm.view;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.camnter.databinding.BindingAdapter;
import com.camnter.databinding.BindingHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.ratingrank.RatingFund;
import com.camnter.newlife.core.activity.BaseBindingActivity;
import com.camnter.newlife.databinding.ActivityRatingRankBinding;
import com.camnter.newlife.databinding.ItemRatingRankingBinding;
import com.camnter.newlife.ui.mvvm.collaborator.RatingRankCollaborator;
import com.camnter.newlife.ui.mvvm.mock.Injection;
import com.camnter.newlife.widget.titilebar.TitleBar;
import java.util.List;

/**
 * Description：RatingRankActivity
 * Created by：CaMnter
 */

public class RatingRankActivity extends BaseBindingActivity {

    private ActivityRatingRankBinding binding;
    private RatingRankCollaborator collaborator;
    private BindingAdapter<RatingFund> adapter;


    @Override protected int getLayoutId() {
        return R.layout.activity_rating_rank;
    }


    @Override
    protected void onCastingContentBinding(@NonNull ViewDataBinding contentBinding) {
        if (contentBinding instanceof ActivityRatingRankBinding) {
            this.binding = (ActivityRatingRankBinding) contentBinding;
        }
    }


    /**
     * on after data binding
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void onAfterDataBinding(@Nullable Bundle savedInstanceState) {
        this.collaborator = new RatingRankCollaborator(this,
            Injection.provideRatingRankRepository());
        this.adapter = new BindingAdapter<RatingFund>(this) {
            @Override
            public int[] getItemLayouts() {
                return new int[] { R.layout.item_rating_ranking };
            }


            @Override
            public void onBindViewHolder(BindingHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                ItemRatingRankingBinding binding = (ItemRatingRankingBinding) holder.getBinding();
                binding.setCollaborator(collaborator);
            }
        };
        this.binding.setAdapter(adapter);
        this.binding.setCollaborator(this.collaborator);
        this.collaborator.query(this);
    }


    @Override protected boolean getTitleBar(TitleBar titleBar) {
        return false;
    }

    public void onQuerySuccess(@NonNull List<RatingFund> funds){
        this.adapter.setList(funds
        );
    }

}
