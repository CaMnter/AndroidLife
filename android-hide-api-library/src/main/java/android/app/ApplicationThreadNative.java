package android.app;

import android.os.Binder;
import android.os.IBinder;

/**
 * Refer from VirtualAPK
 *
 * @author CaMnter
 */

public abstract class ApplicationThreadNative extends Binder implements IApplicationThread {

    @Override
    public IBinder asBinder() {
        throw new RuntimeException("Stub!");
    }

}
