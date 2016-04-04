package com.camnter.newlife.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Description：DialUtils
 * Created by：CaMnter
 * Time：2015-10-13 11:53
 */
public class DialUtils {

    /**
     * Have a dial-up view
     * 有拨号界面
     *
     * @param phoneNumber The phone number 电话号码
     * @param activity The Activity type Activity类型
     */
    public static void showViewDial(String phoneNumber, Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        activity.startActivity(intent);
    }


    /**
     * Direct dialing, no view
     * 直接拨号,无界面
     *
     * @param phoneNumber The phone number 电话号码
     * @param activity The Activity type Activity类型
     */
    public static void directDial(String phoneNumber, Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        activity.startActivity(intent);
    }
}