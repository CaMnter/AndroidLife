package com.camnter.newlife.views.activity.lrucache;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.cache.LruCache;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Description：LruCacheActivity
 * Created by：CaMnter
 * Time：2016-04-21 20:48
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1) public class LruCacheActivity
        extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String LRU_CACHE_ENTRY_REMOVED_NULL_FORMAT = "entryRemoved:";
    private static final String LRU_CACHE_RECENT_FORMAT = "Recent visit:%s";
    private static final String LRU_CACHE_CACHE_DATA_FORMAT = "Cache data:%s";
    private static final String LRU_CACHE_ENTRY_REMOVED_INFO_FORMAT
            = "entryRemoved:\nevicted: %1$s\nkey: %2$s\noldValue: %3$s\nnewValue: %4$s";

    private static final float ONE_MIB = 1024 * 1024;
    // 7MB
    private static final int CACHE_SIZE = (int) (7 * ONE_MIB);

    @Bind(R.id.camnter_size_text) TextView mCamnterSizeText;
    @Bind(R.id.camnter_count_text) TextView mCamnterCountText;
    @Bind(R.id.drakeet_size_text) TextView mDrakeetSizeText;
    @Bind(R.id.drakeet_count_text) TextView mDrakeetCountText;
    @Bind(R.id.ka_size_text) TextView mKaSizeText;
    @Bind(R.id.ka_count_text) TextView mKaCountText;
    @Bind(R.id.peter_size_text) TextView mPeterSizeText;

    @Bind(R.id.entryRemoved_info_text) TextView mEntryRemovedInfoText;
    @Bind(R.id.get_one) Button mGetOne;
    @Bind(R.id.get_two) Button mGetTwo;
    @Bind(R.id.get_three) Button mGetThree;
    @Bind(R.id.clear_remove_info) Button mClearRemoveInfo;
    @Bind(R.id.put_four) Button mPutFour;

    @Bind(R.id.camnter_hashCode_text) TextView mCamnterHashCodeText;
    @Bind(R.id.drakeet_hashCode_text) TextView mDrakeetHashCodeText;
    @Bind(R.id.ka_hashCode_text) TextView mKaHashCodeText;
    @Bind(R.id.peter_hashCode_text) TextView mPeterHashCodeText;
    @Bind(R.id.recent_info_text) TextView mRecentInfoText;
    @Bind(R.id.cache_data_text) TextView mCacheDataText;

    private LruCache<String, Bitmap> bitmapCache;

    private Bitmap camnterBitmap;
    private Bitmap drakeetBitmap;
    private Bitmap kaBitmap;
    private Bitmap peterBitmap;

    private int camnterCount = 0;
    private int drakeetCount = 0;
    private int kaCount = 0;

    private final Map<Bitmap, String> bitmap2Key = new HashMap<>();
    private final List<String> recentList = new LinkedList<>();
    private final List<String> cacheList = new LinkedList<>();


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_lrucache;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
    }


    private void initBitmapAndText() {
        this.camnterBitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_camnter);
        this.drakeetBitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_drakeet);
        this.kaBitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_kaede_akatsuki);
        this.peterBitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_peter_cai);
        this.bitmap2Key.put(this.camnterBitmap, "<1>");
        this.bitmap2Key.put(this.drakeetBitmap, "<2>");
        this.bitmap2Key.put(this.kaBitmap, "<3>");
        this.bitmap2Key.put(this.peterBitmap, "<4>");
        this.mCamnterSizeText.setText(this.getString(R.string.lru_cache_size_format,
                new BigDecimal(this.camnterBitmap.getByteCount() / ONE_MIB).setScale(2,
                        BigDecimal.ROUND_HALF_UP).toString()));
        this.mDrakeetSizeText.setText(this.getString(R.string.lru_cache_size_format,
                new BigDecimal(this.drakeetBitmap.getByteCount() / ONE_MIB).setScale(2,
                        BigDecimal.ROUND_HALF_UP).toString()));
        this.mKaSizeText.setText(this.getString(R.string.lru_cache_size_format,
                new BigDecimal(this.kaBitmap.getByteCount() / ONE_MIB).setScale(2,
                        BigDecimal.ROUND_HALF_UP).toString()));
        this.mPeterSizeText.setText(this.getString(R.string.lru_cache_size_format,
                new BigDecimal(this.peterBitmap.getByteCount() / ONE_MIB).setScale(2,
                        BigDecimal.ROUND_HALF_UP).toString()));
        this.mCamnterCountText.setText(
                this.getString(R.string.lru_cache_count_format, this.camnterCount));
        this.mDrakeetCountText.setText(
                this.getString(R.string.lru_cache_count_format, this.drakeetCount));
        this.mKaCountText.setText(this.getString(R.string.lru_cache_count_format, this.kaCount));

        this.mCamnterHashCodeText.setText(
                this.getString(R.string.lru_cache_hashcode_format, this.camnterBitmap.hashCode()));
        this.mDrakeetHashCodeText.setText(
                this.getString(R.string.lru_cache_hashcode_format, this.drakeetBitmap.hashCode()));
        this.mKaHashCodeText.setText(
                this.getString(R.string.lru_cache_hashcode_format, this.kaBitmap.hashCode()));
        this.mPeterHashCodeText.setText(
                this.getString(R.string.lru_cache_hashcode_format, this.peterBitmap.hashCode()));
        this.mEntryRemovedInfoText.setText(LRU_CACHE_ENTRY_REMOVED_NULL_FORMAT);
        this.mRecentInfoText.setText(
                String.format(Locale.getDefault(), LRU_CACHE_RECENT_FORMAT, ""));
    }


    private void initCache() {
        this.bitmapCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }


            /**
             * 1.当被回收或者删掉时调用。该方法当value被回收释放存储空间时被remove调用
             * 或者替换条目值时put调用，默认实现什么都没做。
             * 2.该方法没用同步调用，如果其他线程访问缓存时，该方法也会执行。
             * 3.evicted=true：如果该条目被删除空间 （表示 进行了trimToSize or remove）  evicted=false：put冲突后 或 get里成功create后 导致
             * 4.newValue!=null，那么则被put()或get()调用。
             */
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                mEntryRemovedInfoText.setText(
                        String.format(Locale.getDefault(), LRU_CACHE_ENTRY_REMOVED_INFO_FORMAT,
                                evicted, key, oldValue != null ? oldValue.hashCode() : "null",
                                newValue != null ? newValue.hashCode() : "null"));
                // 见上述 3.
                if (evicted) {
                    // 进行了trimToSize or remove (一般是溢出了 或 key-value被删除了 )
                    if (recentList.contains(key)) {
                        recentList.remove(key);
                        refreshText(mRecentInfoText, LRU_CACHE_RECENT_FORMAT, recentList);
                    }
                } else {
                    // put冲突后 或 get里成功create 后
                    recentList.remove(key);
                    refreshText(mRecentInfoText, LRU_CACHE_RECENT_FORMAT, recentList);
                }
                if (cacheList.contains(key)) {
                    cacheList.remove(key);
                    refreshText(mCacheDataText, LRU_CACHE_CACHE_DATA_FORMAT, cacheList);
                }
            }
        };
        this.bitmapCache.put(this.bitmap2Key.get(this.camnterBitmap), this.camnterBitmap);
        this.bitmapCache.put(this.bitmap2Key.get(this.drakeetBitmap), this.drakeetBitmap);
        this.bitmapCache.put(this.bitmap2Key.get(this.kaBitmap), this.kaBitmap);
        this.cacheList.add(this.bitmap2Key.get(this.camnterBitmap));
        this.cacheList.add(this.bitmap2Key.get(this.drakeetBitmap));
        this.cacheList.add(this.bitmap2Key.get(this.kaBitmap));
        this.refreshText(this.mCacheDataText, LRU_CACHE_CACHE_DATA_FORMAT, this.cacheList);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.mGetOne.setOnClickListener(this);
        this.mGetTwo.setOnClickListener(this);
        this.mGetThree.setOnClickListener(this);
        this.mClearRemoveInfo.setOnClickListener(this);
        this.mPutFour.setOnClickListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        this.initBitmapAndText();
        this.initCache();
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        boolean isGet = false;
        String key = null;
        switch (v.getId()) {
            case R.id.get_one:
                isGet = true;
                key = this.bitmap2Key.get(this.camnterBitmap);
                this.bitmapCache.get(key);
                this.mCamnterCountText.setText(
                        this.getString(R.string.lru_cache_count_format, ++this.camnterCount));
                break;
            case R.id.get_two:
                isGet = true;
                key = this.bitmap2Key.get(this.drakeetBitmap);
                this.bitmapCache.get(key);
                this.mDrakeetCountText.setText(
                        this.getString(R.string.lru_cache_count_format, ++this.drakeetCount));
                break;
            case R.id.get_three:
                isGet = true;
                key = this.bitmap2Key.get(this.kaBitmap);
                this.bitmapCache.get(key);
                this.mKaCountText.setText(
                        this.getString(R.string.lru_cache_count_format, ++this.kaCount));
                break;
            case R.id.put_four:
                this.bitmapCache.put(this.bitmap2Key.get(this.peterBitmap), this.peterBitmap);
                this.cacheList.add(this.bitmap2Key.get(peterBitmap));
                this.refreshText(this.mCacheDataText, LRU_CACHE_CACHE_DATA_FORMAT, this.cacheList);
                break;
            case R.id.clear_remove_info:
                this.mEntryRemovedInfoText.setText(LRU_CACHE_ENTRY_REMOVED_NULL_FORMAT);
                break;
        }
        if (isGet) {
            if (this.recentList.contains(key)) {
                this.recentList.remove(key);
            }
            this.recentList.add(0, key);
            this.refreshText(this.mRecentInfoText, LRU_CACHE_RECENT_FORMAT, this.recentList);
        }
    }


    private void refreshText(TextView textView, String format, List<String> recentList) {
        Iterator<String> iterator = recentList.iterator();
        String recentText = "";
        while (iterator.hasNext()) {
            recentText += iterator.next() + "  ";
        }
        textView.setText(String.format(Locale.getDefault(), format, recentText));
    }
}
