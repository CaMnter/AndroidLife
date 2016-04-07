package com.camnter.newlife.utils.otto;

import com.camnter.otto.Bus;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more
 * efficient means
 * such as through injection directly into interested classes.
 */
public final class BusProvider {
    private static final Bus BUS = new Bus();


    public synchronized static Bus getInstance() {
        return BUS;
    }


    private BusProvider() {
        // No instances.
    }
}
