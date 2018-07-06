package com.camnter.newlife.ui.activity.asm;

import android.app.Activity;
import java.util.HashMap;

/**
 * @author CaMnter
 */

public class ActivityTimeManger {

    public static HashMap<String, Long> startTimeMap = new HashMap<>();


    public static void onCreateStart(Activity activity) {
        startTimeMap.put(activity.toString(), System.currentTimeMillis());
    }


    public static void onCreateEnd(Activity activity) {
        Long startTime = startTimeMap.get(activity.toString());
        if (startTime == null) {
            return;
        }
        long coastTime = System.currentTimeMillis() - startTime;
        System.out.println("「" + activity.toString() + "」" + " onCreate coast 「Time」" + coastTime);
        startTimeMap.remove(activity.toString());
    }

}
