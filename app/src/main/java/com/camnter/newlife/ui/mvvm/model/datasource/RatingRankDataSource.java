package com.camnter.newlife.ui.mvvm.model.datasource;

import android.app.Activity;
import android.support.annotation.NonNull;
import com.camnter.newlife.bean.ratingrank.RatingFund;
import java.util.List;

/**
 * Description：RatingRankDataSource
 * Created by：CaMnter
 */

public interface RatingRankDataSource {

    interface QueryRanksCallback {
        void onRanksLoaded(@NonNull final List<RatingFund> funds);
    }

    void query(@NonNull final Activity activity, @NonNull final QueryRanksCallback callback);

}
