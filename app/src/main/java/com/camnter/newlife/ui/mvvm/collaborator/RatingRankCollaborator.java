package com.camnter.newlife.ui.mvvm.collaborator;

import android.app.Activity;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.widget.Toast;
import com.camnter.newlife.bean.ratingrank.RatingFund;
import com.camnter.newlife.ui.mvvm.model.RatingRankRepository;
import com.camnter.newlife.ui.mvvm.model.datasource.RatingRankDataSource;
import java.util.List;

/**
 * Description：RatingRankViewModel
 * Created by：CaMnter
 */

public class RatingRankCollaborator implements
    RatingRankDataSource.QueryRanksCallback {

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);
    private final ObservableField<List<RatingFund>> funds = new ObservableField<>();

    private Context context;
    private final RatingRankRepository repository;


    public RatingRankCollaborator(Context context, RatingRankRepository repository) {
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
    }


    public void onActivityDestroyed() {
        this.context = null;
    }


    public void itemClick(int position, RatingFund fund) {
        Toast.makeText(this.context, "position = " + position + "  name = " + fund.getName(),
            Toast.LENGTH_SHORT).show();
    }

}
