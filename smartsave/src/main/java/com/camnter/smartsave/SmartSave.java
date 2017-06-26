package com.camnter.smartsave;

import android.app.Activity;
import android.view.View;
import com.camnter.smartsave.adapter.ActivityAdapter;
import com.camnter.smartsave.adapter.Adapter;
import com.camnter.smartsave.adapter.ViewAdapter;
import com.camnter.smartsave.save.Save;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CaMnter
 */

public final class SmartSave {

    private static final ActivityAdapter ACTIVITY_ADAPTER = new ActivityAdapter();
    private static final ViewAdapter VIEW_ADAPTER = new ViewAdapter();

    private static final Map<String, Save> SAVE_MAP = new HashMap<>();


    public static void save(final Activity activity) {
        save(activity, ACTIVITY_ADAPTER);
    }


    public static void save(final View view) {
        save(view, VIEW_ADAPTER);
    }


    @SuppressWarnings("unchecked")
    private static void save(final Object target, final Adapter adapter) {
        final String targetFullName = target.getClass().getName();
        try {
            Save save = SAVE_MAP.get(targetFullName);
            if (save == null) {
                Class<?> saveClass = Class.forName(targetFullName + "_Save");
                save = (Save) saveClass.newInstance();
                SAVE_MAP.put(targetFullName, save);
            }
            save.save(target, adapter);
        } catch (Exception e) {
            throw new RuntimeException("[SuperSave]   [save]   " + targetFullName, e);
        }
    }

}
