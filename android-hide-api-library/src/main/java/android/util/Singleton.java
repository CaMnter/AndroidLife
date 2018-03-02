package android.util;

/**
 * Refer from VirtualAPK
 *
 * @author CaMnter
 */

public abstract class Singleton<T> {

    public Singleton() {
        throw new RuntimeException("Stub!");
    }


    protected abstract T create();


    public T get() {
        throw new RuntimeException("Stub!");
    }

}
