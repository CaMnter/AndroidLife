package com.camnter.smartsave.save;

import com.camnter.smartsave.adapter.Adapter;

/**
 * @author CaMnter
 */

public interface Save<T> {

    void save(T target, Adapter adapter);

    void unSave(T target, Adapter adapter);

}
