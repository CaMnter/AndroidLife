package com.camnter.newlife.ui.databinding.model.remote;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import com.camnter.newlife.bean.ratingrank.RatingRankResponse;
import com.camnter.newlife.ui.databinding.model.datasource.RatingRankDataSource;
import com.google.gson.Gson;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Description：RatingRankRemoteRepository
 * Created by：CaMnter
 */

public class RatingRankRemoteRepository implements RatingRankDataSource {

    private static RatingRankRemoteRepository INSTANCE;


    // Prevent direct instantiation.
    private RatingRankRemoteRepository() {}


    public static RatingRankRemoteRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RatingRankRemoteRepository();
        }
        return INSTANCE;
    }


    @Override
    public void query(@NonNull final Activity activity, @NonNull final QueryRanksCallback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            RatingRankResponse response;
            try {
                Gson gson = new Gson();
                InputStream inputStream = activity
                    .getAssets()
                    .open("json" + File.separator + "ratingrank.json");
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                response = gson.fromJson(inputStreamReader,
                    RatingRankResponse.class);
                callback.onRanksLoaded(response.getData().getFunds());
            } catch (Exception e) {
                callback.onRanksLoaded(new ArrayList<>());
            }
        }, 1777);
    }

}
