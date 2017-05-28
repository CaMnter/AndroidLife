package com.camnter.utils.wrapper;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

/**
 * Description：SmartToastWrapper
 * Created by：CaMnter
 */

public abstract class SmartToastWrapper {

    private Toast toast;
    private final Handler handler;
    private final Runnable runnable;

    private static final int MAX_DURATION = 2222;


    protected SmartToastWrapper() {
        this.toast = this.getToast();
        this.handler = new Handler(Looper.getMainLooper());
        this.runnable = new Runnable() {
            @Override
            public void run() {
                if (toast == null) return;
                toast.cancel();
                toast = null;
            }
        };
    }


    public void show(@NonNull final CharSequence message) {
        if (this.toast == null) {
            this.toast = this.getToast();
        }

        this.toast.setText(message);
        this.toast.show();

        this.handler.removeCallbacks(this.runnable);
        this.handler.postDelayed(this.runnable, MAX_DURATION);
    }


    protected abstract Toast getToast();

}
