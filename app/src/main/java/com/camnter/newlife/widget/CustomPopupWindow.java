package com.camnter.newlife.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.camnter.newlife.R;

/**
 * Description：TopicFieldPopWindow
 * Created by：CaMnter
 * Time：2015-12-11 11:31
 */
public class CustomPopupWindow extends android.widget.PopupWindow {

    private Activity activity;

    private View popupWindowView;

    public CustomPopupWindow(Activity activity) {
        super(activity);
        this.activity = activity;
        this.initPopupWindow();
    }

    private void initPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.popupWindowView = inflater.inflate(R.layout.popupwindow_topic_field, null);
        setContentView(popupWindowView);
        //设置弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置弹出窗体可点击
        setTouchable(true);
        setFocusable(true);
        //
        setOutsideTouchable(true);

        //设置弹出窗体动画效果
        this.setAnimationStyle(R.style.PopupAnimation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable drawable = new ColorDrawable(0x4f000000);
        //设置弹出窗体的背景
        this.setBackgroundDrawable(drawable);
    }


    public static class PopupWindowBuilder {
        private static CustomPopupWindow popupWindow;
        public static PopupWindowBuilder ourInstance;

        public static PopupWindowBuilder getInstance(Activity activity) {
            if (ourInstance == null) ourInstance = new PopupWindowBuilder(activity);
            popupWindow = new CustomPopupWindow(activity);
            return ourInstance;
        }

        private PopupWindowBuilder(Activity activity) {

        }

        public CustomPopupWindow getPopupWindow() {
            return popupWindow;
        }

    }


}