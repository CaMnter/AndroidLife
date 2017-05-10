package com.camnter.newlife.core.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.camnter.mvvm.view.BindingActivity;
import com.camnter.newlife.R;
import com.camnter.newlife.databinding.ActivityBaseMvvmBinding;
import com.camnter.newlife.utils.ToastUtils;
import com.camnter.newlife.widget.titilebar.TitleBar;
import java.lang.reflect.Method;

/**
 * Description：BaseMVVMActivity
 * Created by：CaMnter
 */

public abstract class BaseBindingActivity extends BindingActivity {

    private static final String EMPTY_LENGTH_STRING = "";
    protected Activity activity;
    private ActivityBaseMvvmBinding castedRootBinding;
    private ViewDataBinding contentBinding;

    private TitleBar titleBar;
    private RelativeLayout contentLayout;
    private LayoutInflater inflater;


    /**
     * default true
     *
     * @return auto ?
     */
    @Override protected boolean autoSetContentView() {
        return false;
    }


    /**
     * on casting root binding
     *
     * @param rootBinding rootBinding
     */
    @Override protected void onCastingRootBinding(
        @Nullable ViewDataBinding rootBinding) {
        if (rootBinding != null) {
            this.castToBaseMVVMBinding(rootBinding);
        } else {
            // reset content view, because auto == false
            this.rootBinding = DataBindingUtil.setContentView(this, R.layout.activity_base_mvvm);
            this.castToBaseMVVMBinding(this.rootBinding);
        }
    }


    private void castToBaseMVVMBinding(@NonNull ViewDataBinding rootBinding) {
        if (rootBinding instanceof ActivityBaseMvvmBinding) {
            this.castedRootBinding = (ActivityBaseMvvmBinding) rootBinding;
        }
    }


    public ActivityBaseMvvmBinding getCastedRootBinding() {
        return this.castedRootBinding;
    }


    public ViewDataBinding getContentBinding() {
        return this.contentBinding;
    }


    @Override
    protected void baseActivityInit() {
        super.baseActivityInit();

        // 5.0 以上 状态栏 颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        this.inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.initBaseActivityViews();
        // rendering title
        this.renderingTitle();
        // rendering content
        this.renderingContent();
    }


    private void initBaseActivityViews() {
        if (this.castedRootBinding == null) return;
        this.contentLayout = this.castedRootBinding.baseActivityContentLayout;
        this.titleBar = this.castedRootBinding.baseActivityTitleBar;
    }


    private void renderingTitle() {
        final TitleBar titleBar = this.titleBar;
        if (titleBar == null) return;
        titleBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        if (!this.getTitleBar(this.titleBar)) this.titleBar.setVisibility(View.GONE);
    }


    private void renderingContent() {
        final int layoutId = this.getLayoutId();
        if (layoutId > 0) {
            this.contentBinding = DataBindingUtil.inflate(this.inflater, layoutId,
                this.contentLayout, true);
            this.contentLayout = (RelativeLayout) this.contentBinding.getRoot();
            this.onCastingContentBinding(this.contentBinding);
        } else {
            throw new IllegalArgumentException("Layout id <= 0");
        }
    }


    protected abstract void onCastingContentBinding(@NonNull final ViewDataBinding contentBinding);

    protected abstract boolean getTitleBar(TitleBar titleBar);

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
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(
                INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void closeInputMethod() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(
                INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getApplicationWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void openInputMethod(View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(
                INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isInputMethodVisible() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        return imm.isActive();
    }


    public void hideSoftInputMethod(EditText editText, Boolean visible) {
        this.getWindow()
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
