package com.camnter.newlife.utils.agera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import com.google.android.agera.ActivationHandler;
import com.google.android.agera.Observable;
import com.google.android.agera.Observables;
import com.google.android.agera.Updatable;
import com.google.android.agera.UpdateDispatcher;

import static com.google.android.agera.Preconditions.checkNotNull;

/**
 * Description：BroadcastAgeraObservable
 * Created by：CaMnter
 * Time：2016-05-31 17:57
 */
public class BroadcastAgeraObservable extends BroadcastReceiver
        implements ActivationHandler, Observable {

    @NonNull
    private final UpdateDispatcher updateDispatcher;

    @NonNull
    private final Context context;

    @NonNull
    private final IntentFilter intentFilter;


    public BroadcastAgeraObservable(
            @NonNull Context applicationContext, @NonNull final String... actions) {
        this.context = checkNotNull(applicationContext);
        this.updateDispatcher = Observables.updateDispatcher(this);
        this.intentFilter = new IntentFilter();
        for (String action : actions) {
            this.intentFilter.addAction(action);
        }
    }


    @Override public void onReceive(Context context, Intent intent) {
        this.updateDispatcher.update();
    }


    /**
     * Called when the the {@code caller} changes state from having no {@link Updatable}s to
     * having at least one {@link Updatable}.
     */
    @Override public void observableActivated(@NonNull UpdateDispatcher caller) {
        this.context.registerReceiver(this, this.intentFilter);
    }


    /**
     * Called when the the {@code caller} changes state from having {@link Updatable}s to
     * no longer having {@link Updatable}s.
     */
    @Override public void observableDeactivated(@NonNull UpdateDispatcher caller) {
        this.context.unregisterReceiver(this);
    }


    /**
     * Adds {@code updatable} to the {@code Observable}.
     *
     * @throws IllegalStateException if the {@link Updatable} was already added or if it was called
     * from a non-Looper thread
     */
    @Override public void addUpdatable(@NonNull Updatable updatable) {
        this.updateDispatcher.addUpdatable(updatable);
    }


    /**
     * Removes {@code updatable} from the {@code Observable}.
     *
     * @throws IllegalStateException if the {@link Updatable} was not added
     */
    @Override public void removeUpdatable(@NonNull Updatable updatable) {
        this.updateDispatcher.removeUpdatable(updatable);
    }
}
