package com.camnter.newlife.ui.mvvm.model;

import android.app.Activity;
import android.support.annotation.NonNull;
import com.camnter.newlife.ui.mvvm.model.datasource.RatingRankDataSource;
import com.camnter.newlife.ui.mvvm.model.local.RatingRankLocalRepository;
import com.camnter.newlife.ui.mvvm.model.remote.RatingRankRemoteRepository;
import com.google.common.base.Preconditions;

/**
 * Description：RatingRankRepository
 * Created by：CaMnter
 */

public class RatingRankRepository implements RatingRankDataSource {

    private final RatingRankLocalRepository localRepository;
    private final RatingRankRemoteRepository remoteRepository;

    private static RatingRankRepository INSTANCE;


    public RatingRankRepository(
        @NonNull final RatingRankLocalRepository localRepository,
        @NonNull final RatingRankRemoteRepository remoteRepository) {
        this.localRepository = Preconditions.checkNotNull(localRepository);
        this.remoteRepository = Preconditions.checkNotNull(remoteRepository);
    }


    public static RatingRankRepository getInstance(
        @NonNull final RatingRankLocalRepository localRepository,
        @NonNull final RatingRankRemoteRepository remoteRepository) {
        if (INSTANCE == null) {
            INSTANCE = new RatingRankRepository(localRepository, remoteRepository);
        }
        return INSTANCE;
    }


    @Override
    public void query(@NonNull final Activity activity, @NonNull QueryRanksCallback callback) {
        this.remoteRepository.query(activity, callback);
    }

}
