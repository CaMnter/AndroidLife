package com.camnter.newlife.ui.databinding.model.local;

import android.app.Activity;
import android.support.annotation.NonNull;
import com.camnter.newlife.bean.ratingrank.RatingFund;
import com.camnter.newlife.ui.databinding.model.datasource.RatingRankDataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Description：RatingRankLocalRepository
 * Created by：CaMnter
 */

public class RatingRankLocalRepository implements RatingRankDataSource {

    private static RatingRankLocalRepository INSTANCE;


    // Prevent direct instantiation.
    private RatingRankLocalRepository() {}


    public static RatingRankLocalRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RatingRankLocalRepository();
        }
        return INSTANCE;
    }


    @Override
    public void query(@NonNull final Activity activity, @NonNull final QueryRanksCallback callback) {
        final String namePrefix = "二次元-";
        List<RatingFund> funds = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            RatingFund fund = new RatingFund();
            fund.setLevel(Math.abs(i - 5));
            fund.setName(namePrefix + i);
            fund.setFundCode(String.valueOf(i));
            funds.add(fund);
        }
        callback.onRanksLoaded(funds);
    }

}
