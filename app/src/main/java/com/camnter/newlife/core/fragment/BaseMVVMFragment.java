package com.camnter.newlife.core.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.camnter.mvvm.view.MVVMFragment;
import com.camnter.newlife.utils.ToastUtils;
import java.lang.reflect.Method;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Description：BaseMVVMFragment
 * Created by：CaMnter
 */

public abstract class BaseMVVMFragment extends MVVMFragment {

    private static final String EMPTY_LENGTH_STRING = "";

    //***************//
    // Magic Method *//
    //***************//


    protected void safetySetText(@NonNull final TextView textView, @Nullable final String text) {
        textView.setText(TextUtils.isEmpty(text) ? EMPTY_LENGTH_STRING : text);
    }

    //**************//
    // 系统 Dialog *//
    //**************//


    public void showAlertDialog(@NonNull final String title,
                                @NonNull final CharSequence message,
                                @NonNull final String buttonText,
                                @NonNull
                                final DialogInterface.OnClickListener onPositiveClickListener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
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
        ToastUtils.show(this.activity, resId, Toast.LENGTH_SHORT);
    }


    protected void showShortToast(@NonNull final String text) {
        ToastUtils.show(this.activity, text, Toast.LENGTH_SHORT);
    }


    protected void showLongToast(@StringRes final int resId) {
        ToastUtils.show(this.activity, resId, Toast.LENGTH_LONG);
    }


    protected void showLongToast(@NonNull final String text) {
        ToastUtils.show(this.activity, text, Toast.LENGTH_LONG);
    }


    @SuppressLint("ShowToast")
    public void showToast(@NonNull final String text) {
        ToastUtils.show(this.activity, text, Toast.LENGTH_LONG);
    }

    //***************//
    // Input Method *//
    //***************//


    public void closeInputMethod(IBinder token) {
        try {
            InputMethodManager inputMethodManager
                = (InputMethodManager) this.activity.getSystemService(
                INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void closeInputMethod() {
        try {
            InputMethodManager inputMethodManager
                = (InputMethodManager) this.activity.getSystemService(
                INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                this.activity.getCurrentFocus().getApplicationWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void openInputMethod(View view) {
        try {
            InputMethodManager inputMethodManager
                = (InputMethodManager) this.activity.getSystemService(
                INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isInputMethodVisible() {
        InputMethodManager imm = (InputMethodManager) this.activity.getSystemService(
            INPUT_METHOD_SERVICE);
        return imm.isActive();
    }


    public void hideSoftInputMethod(EditText editText, Boolean visible) {
        this.activity.getWindow()
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
