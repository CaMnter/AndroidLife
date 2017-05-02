package com.camnter.newlife.core.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.camnter.mvvm.view.MVVMActivity;
import com.camnter.newlife.R;
import com.camnter.newlife.utils.ToastUtils;
import com.camnter.newlife.widget.titilebar.TitleBar;
import java.lang.reflect.Method;

/**
 * Description：BaseMVVMActivity
 * Created by：CaMnter
 */

public abstract class BaseMVVMActivity extends MVVMActivity {

    protected Activity activity;

    private TitleBar titleBar;
    private RelativeLayout contentLayout;


    @Override
    protected void baseActivityInit() {
        super.baseActivityInit();

        // 5.0 以上 状态栏 颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        this.setContentView(R.layout.activity_base_mvvm);

        this.initBaseActivityViews();

        this.renderingTitle();
        this.renderingContent();
    }


    private void initBaseActivityViews() {
        this.contentLayout = (RelativeLayout) this.findViewById(R.id.base_activity_content_layout);
        this.titleBar = (TitleBar) this.findViewById(R.id.base_activity_title_bar);
    }


    private void renderingTitle() {
        final TitleBar titleBar = this.titleBar;
        if (titleBar == null) return;
        if (!this.getTitleBar(this.titleBar)) this.titleBar.setVisibility(View.GONE);
    }


    private void renderingContent() {
        final int layoutId = this.getLayoutId();
        if (layoutId <= 0) {
            final View layoutView = this.getLayoutView();
            if (layoutView != null) {
                this.contentLayout.removeAllViews();
                this.contentLayout.addView(layoutView);
            } else {
                LayoutInflater.from(this).inflate(layoutId, this.contentLayout, true);
            }
        }
    }


    /**
     * 整个布局渲染这个 View
     *
     * @return View
     */
    protected View getLayoutView() {
        return null;
    }


    protected abstract boolean getTitleBar(TitleBar titleBar);

    //**************//
    // 系统 Dialog *//
    //**************//


    public void showAlertDialog(@NonNull final String title,
                                @NonNull final CharSequence message,
                                @NonNull final String buttonText,
                                @NonNull
                                final DialogInterface.OnClickListener onPositiveClickListener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (!TextUtils.isEmpty(title)) builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton(buttonText, onPositiveClickListener);
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Dialog showAlertDialog(@NonNull final String title,
                                  @NonNull final CharSequence message,
                                  @NonNull final String positiveText,
                                  @NonNull
                                  final DialogInterface.OnClickListener onPositiveClickListener,
                                  @NonNull final String negativeText,
                                  @NonNull
                                  final DialogInterface.OnClickListener onNegativeClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        builder.setMessage(message);
        if (!TextUtils.isEmpty(positiveText)) {
            builder.setPositiveButton(positiveText, onPositiveClickListener);
        }
        if (!TextUtils.isEmpty(negativeText)) {
            builder.setNegativeButton(negativeText, onNegativeClickListener);
        }
        Dialog dialog = builder.create();
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dialog;
    }

    //***************//
    // Toast Method *//
    //***************//


    protected void showShortToast(@StringRes final int resId) {
        ToastUtils.show(this, resId, Toast.LENGTH_SHORT);
    }


    protected void showShortToast(@NonNull final String text) {
        ToastUtils.show(this, text, Toast.LENGTH_SHORT);
    }


    protected void showLongToast(@StringRes final int resId) {
        ToastUtils.show(this, resId, Toast.LENGTH_LONG);
    }


    protected void showLongToast(@NonNull final String text) {
        ToastUtils.show(this, text, Toast.LENGTH_LONG);
    }


    @SuppressLint("ShowToast")
    public void showToast(@NonNull final String text) {
        ToastUtils.show(this, text, Toast.LENGTH_LONG);
    }

    //***************//
    // Input Method *//
    //***************//


    public void closeInputMethod(IBinder token) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void closeInputMethod() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void openInputMethod(View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isInputMethodVisible() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        return imm.isActive();
    }


    public void hideSoftInputMethod(EditText editText, Boolean visible) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        final int currentVersion = android.os.Build.VERSION.SDK_INT;
        String methodName = null;
        if (currentVersion >= 16) {
            // 4.2
            methodName = "setShowSoftInputOnFocus";
        } else if (currentVersion >= 14) {
            // 4.0
            methodName = "setSoftInputShownOnFocus";
        }
        if (methodName == null) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> clazz = EditText.class;
            Method setShowSoftInputOnFocus;
            try {
                setShowSoftInputOnFocus = clazz.getMethod(methodName, boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(editText, visible);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
