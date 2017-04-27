package com.camnter.newlife.ui.mvvm.vm;

import android.app.Activity;
import android.content.Context;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import com.camnter.mvvm.MVVMViewModel;
import com.camnter.newlife.bean.ratingrank.RatingFund;
import com.camnter.newlife.ui.mvvm.model.RatingRankRepository;
import com.camnter.newlife.ui.mvvm.model.datasource.RatingRankDataSource;
import java.util.List;

/**
 * Description：RatingRankViewModel
 * Created by：CaMnter
 */

public class RatingRankViewModel<T> extends MVVMViewModel<T> implements
    RatingRankDataSource.QueryRanksCallback {

    @Bindable
    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    @Bindable
    private final ObservableField<List<RatingFund>> funds = new ObservableField<>();

    private Context context;
    private final RatingRankRepository repository;


    public RatingRankViewModel(Context context, RatingRankRepository repository) {
        this.context = context;
        this.repository = repository;
    }


    public void query(@NonNull final Activity activity) {
        this.dataLoading.set(true);
        this.repository.query(activity, this);
    }


    @Override public void onRanksLoaded(@NonNull List<RatingFund> funds) {
        this.dataLoading.set(false);
        this.funds.set(funds);
        this.adapter.setList(funds);
    }


    public void onActivityDestroyed() {
        this.context = null;
    }

}
