package com.camnter.newlife.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Description：ImeUtils
 * Created by：CaMnter
 * Time：2014-12-25 19:21
 */
public class ImeUtils {
    /**
     * 显示软键盘
     * View ： EditText、TextView
     * wantPop : true = show , false = hide
     *
     * @param context context
     * @param view view
     * @param wantPop wantPop
     */
    public static void popSoftKeyboard(Context context, View view, boolean wantPop) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (wantPop) {
            view.requestFocus();
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * 显示软键盘
     *
     * @param view view
     */
    public static void showSoftKeyboard(View view) {
        Context context = view.getContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }


    /**
     * 隐藏软键盘
     *
     * @param view view
     */
    public static void hideSoftKeyboard(View view) {
        Context context = view.getContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
