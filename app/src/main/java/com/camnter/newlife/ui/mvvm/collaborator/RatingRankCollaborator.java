package com.camnter.newlife.ui.mvvm.collaborator;

import android.app.Activity;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.widget.Toast;
import com.camnter.newlife.bean.ratingrank.RatingFund;
import com.camnter.newlife.core.BaseActivityCollaborator;
import com.camnter.newlife.ui.mvvm.model.RatingRankRepository;
import com.camnter.newlife.ui.mvvm.model.datasource.RatingRankDataSource;
import com.camnter.newlife.ui.mvvm.view.RatingRankActivity;
import java.util.List;

/**
 * Description：RatingRankCollaborator
 * Created by：CaMnter
 */

public class RatingRankCollaborator
    extends BaseActivityCollaborator<RatingRankActivity> implements
    RatingRankDataSource.QueryRanksCallback {

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);
    private final ObservableField<List<RatingFund>> funds = new ObservableField<>();

    private final RatingRankRepository repository;


    public RatingRankCollaborator(@NonNull RatingRankActivity activity,
                                  @NonNull RatingRankRepository repository) {
        super(activity);
        this.repository = repository;
    }


    public void query(@NonNull final Activity activity) {
        this.dataLoading.set(true);
        this.repository.query(activity, this);
    }


    @Override
    public void onRanksLoaded(@NonNull List<RatingFund> funds) {
        this.dataLoading.set(false);
        this.funds.set(funds);
        if (this.getActivity() != null) {
            this.getActivity().onQuerySuccess(funds);
        }
    }


    public void onActivityDestroyed() {
        this.clearReference();
    }


    public void itemClick(int position, RatingFund fund) {
        Toast.makeText(this.getActivity(),
            "position = " + position + "  name = " + fund.getName(),
            Toast.LENGTH_SHORT).show();
    }

}
