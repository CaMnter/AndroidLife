package com.camnter.newlife.widget.synchorizontalscrollview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import java.util.ArrayList;
import java.util.List;

/**
 * Description：SyncHorizontalScrollView
 * Created by：CaMnter
 */

public class SyncHorizontalScrollView extends HorizontalScrollView {

    private ScrollViewObserver scrollViewObserver = new ScrollViewObserver();


    public SyncHorizontalScrollView(Context context) {
        super(context);
    }


    public SyncHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public SyncHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SyncHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        if (this.scrollViewObserver != null) {
            this.scrollViewObserver.notifyOnScrollChanged(l, t, oldL, oldT);
        }
        super.onScrollChanged(l, t, oldL, oldT);
    }


    public void addOnScrollChangedListener(OnScrollChangedListener listener) {
        this.scrollViewObserver.addOnScrollChangedListener(listener);
    }


    public void removeOnScrollChangedListener(OnScrollChangedListener listener) {
        this.scrollViewObserver.removeOnScrollChangedListener(listener);
    }


    private static class ScrollViewObserver {

        @NonNull
        private final List<OnScrollChangedListener> scrollChangedListeners;


        ScrollViewObserver() {
            this.scrollChangedListeners = new ArrayList<>();
        }


        void addOnScrollChangedListener(
            @Nullable final OnScrollChangedListener onScrollChangedListener) {
            if (onScrollChangedListener == null) return;
            this.scrollChangedListeners.add(onScrollChangedListener);
        }


        void removeOnScrollChangedListener(
            @Nullable final OnScrollChangedListener onScrollChangedListener) {
            if (onScrollChangedListener == null) return;
            this.scrollChangedListeners.remove(onScrollChangedListener);
        }


        void notifyOnScrollChanged(int l, int t, int oldL, int oldT) {
            if (this.scrollChangedListeners.size() == 0) return;
            for (OnScrollChangedListener onScrollChangedListener : this.scrollChangedListeners) {
                onScrollChangedListener.onScrollChanged(l, t, oldL, oldT);
            }
        }

    }


    public interface OnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldL, int oldT);
    }


    public static class SimpleOnScrollChangedListener implements OnScrollChangedListener {

        private final SyncHorizontalScrollView syncHorizontalScrollView;


        public SimpleOnScrollChangedListener(
            @NonNull final SyncHorizontalScrollView syncHorizontalScrollView) {
            this.syncHorizontalScrollView = syncHorizontalScrollView;
        }


        @Override
        public void onScrollChanged(int l, int t, int oldL, int oldT) {
            this.syncHorizontalScrollView.smoothScrollTo(l, t);
        }

    }


    @Override
    public void fling(int velocityX) {
        super.fling(velocityX / 15);
    }

}
