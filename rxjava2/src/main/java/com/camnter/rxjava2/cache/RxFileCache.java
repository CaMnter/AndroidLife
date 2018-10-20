package com.camnter.rxjava2.cache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 * @author CaMnter
 */

public class RxFileCache {

    private static final String ENCODING = "UTF-8";

    private static Gson gson;


    public <D> Observable<Result> rxSave(@Nullable final Context context,
                                         @Nullable final String fileName,
                                         @Nullable final D data) {
        return
            Observable
                .create(new ObservableOnSubscribe<Result>() {
                    @Override
                    public void subscribe(ObservableEmitter<Result> e) throws Exception {
                        e.onNext(save(context, fileName, data));
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, Result>() {
                    @Override
                    public Result apply(Throwable throwable) throws Exception {
                        return new Result(
                            Result.RESULT_CODE_FAILURE,
                            new CacheException(
                                RxFileCache.class,
                                throwable.getMessage()
                            )
                        );
                    }
                });
    }


    public static <D> Observable<Result> rxSave(@Nullable final String filePath,
                                                @Nullable final D data) {
        return
            Observable
                .create(new ObservableOnSubscribe<Result>() {
                    @Override
                    public void subscribe(ObservableEmitter<Result> e) throws Exception {
                        e.onNext(save(filePath, data));
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, Result>() {
                    @Override
                    public Result apply(Throwable throwable) throws Exception {
                        return new Result(
                            Result.RESULT_CODE_FAILURE,
                            new CacheException(
                                RxFileCache.class,
                                throwable.getMessage()
                            )
                        );
                    }
                });
    }


    public <D> Result save(@Nullable final Context context,
                           @Nullable final String fileName,
                           @Nullable final D data) {
        if (context == null || TextUtils.isEmpty(fileName) || data == null) {
            return new Result(
                Result.RESULT_CODE_FAILURE,
                new CacheException(
                    RxFileCache.class,
                    "「context」= " + context + "「fileName」= " + fileName + "「data」= " + data
                )
            );
        }
        final File targetDir = getDefaultCacheDir(context);
        if (targetDir == null || !targetDir.exists()) {
            return new Result(
                Result.RESULT_CODE_FAILURE,
                new CacheException(
                    RxFileCache.class,
                    "「targetDir」= " + targetDir
                )
            );
        }
        final File file = new File(targetDir, fileName);
        return save(file.getAbsolutePath(), data);
    }


    public static <D> Result save(@Nullable final String filePath,
                                  @Nullable final D data) {
        if (TextUtils.isEmpty(filePath) || data == null) {
            return new Result(
                Result.RESULT_CODE_FAILURE,
                new CacheException(
                    RxFileCache.class,
                    "「filePath」= " + filePath + "「data」= " + data
                )
            );
        }
        checkoutGson();
        final File file = new File(filePath);
        final boolean directoryExists = checkoutDirectory(file);
        if (!directoryExists) {
            return new Result(
                Result.RESULT_CODE_FAILURE,
                new CacheException(
                    RxFileCache.class,
                    "「directoryExists」= false"
                )
            );
        }
        try {
            FileUtils.writeStringToFile(file, gson.toJson(data));
            return new Result(Result.RESULT_CODE_SUCCESS);
        } catch (IOException e) {
            return new Result(
                Result.RESULT_CODE_FAILURE,
                new CacheException(RxFileCache.class, e)
            );
        }
    }


    public static <C> Observable<C> rxLoad(@Nullable final Context context,
                                           @Nullable final String fileName,
                                           @Nullable final Class<C> clazz,
                                           @NonNull final Function<Throwable, C> function) {
        return Observable
            .create(new ObservableOnSubscribe<C>() {
                @Override
                public void subscribe(ObservableEmitter<C> e) throws Exception {
                    final C c = load(context, fileName, clazz);
                    e.onNext(c);
                    e.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn(function);
    }


    public static <C> Observable<C> rxLoad(@Nullable final String filePath,
                                           @Nullable final Class<C> clazz,
                                           @NonNull final Function<Throwable, C> function) {
        return Observable
            .create(new ObservableOnSubscribe<C>() {
                @Override
                public void subscribe(ObservableEmitter<C> e) throws Exception {
                    final C c = load(filePath, clazz);
                    e.onNext(c);
                    e.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn(function);
    }


    @Nullable
    public static <C> C load(@Nullable final Context context,
                             @Nullable final String fileName,
                             @Nullable final Class<C> clazz) {
        if (clazz == null) {
            return null;
        }
        final C c = getDefaultData(clazz);
        if (context == null || TextUtils.isEmpty(fileName)) {
            return c;
        }
        final File targetDir = getDefaultCacheDir(context);
        if (targetDir == null || !targetDir.exists()) {
            return null;
        }
        final File file = new File(targetDir, fileName);
        if (!file.exists()) {
            return null;
        }
        return load(file.getAbsolutePath(), clazz);
    }


    @Nullable
    public static <C> C load(@Nullable final String filePath,
                             @Nullable final Class<C> clazz) {
        if (clazz == null) {
            return null;
        }
        final C c = getDefaultData(clazz);
        if (TextUtils.isEmpty(filePath)) {
            return c;
        }
        final File file = new File(filePath);
        if (!file.exists()) {
            return c;
        }
        checkoutGson();
        try {
            return gson.fromJson(FileUtils.readFileToString(file, ENCODING), clazz);
        } catch (IOException e) {
            return c;
        }
    }


    @Nullable
    private static File getDefaultCacheDir(@Nullable final Context context) {
        if (context == null) {
            return null;
        }
        final File cacheDir = context.getCacheDir();
        final File filesDir = context.getFilesDir();
        final File targetDir;
        if (cacheDir.exists()) {
            targetDir = cacheDir;
        } else if (filesDir.exists()) {
            targetDir = filesDir;
        } else {
            targetDir = null;
        }
        return targetDir;
    }


    private static <C> C getDefaultData(@NonNull final Class<C> clazz) {
        C c;
        try {
            c = clazz.newInstance();
        } catch (Exception e) {
            c = null;
        }
        return c;
    }


    private static void checkoutGson() {
        if (gson == null) {
            gson = new Gson();
        }
    }


    private static boolean checkoutDirectory(@NonNull final File file) {
        final File directoryFile = file.getParentFile();
        // noinspection SimplifiableIfStatement
        if (!directoryFile.exists()) {
            return directoryFile.mkdirs();
        } else {
            return true;
        }
    }

}
