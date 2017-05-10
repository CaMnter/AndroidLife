package com.camnter.newlife.core.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.camnter.mvvm.view.BindingFragment;
import com.camnter.newlife.R;
import com.camnter.newlife.databinding.FragmentBaseMvvmBinding;
import com.camnter.newlife.utils.ToastUtils;
import java.lang.reflect.Method;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Description：BaseMVVMFragment
 * Created by：CaMnter
 */

public abstract class BaseBindingFragment extends BindingFragment {

    private static final String EMPTY_LENGTH_STRING = "";

    private ViewDataBinding contentBinding;
    private FragmentBaseMvvmBinding castedRootBinding;

    private RelativeLayout contentLayout;

    private ObservableBoolean firstLoading = new ObservableBoolean(true);


    /**
     * default true
     *
     * @return auto ?
     */
    @Override
    protected boolean autoInflateView() {
        return false;
    }


    /**
     * on casting root binding
     *
     * @param rootBinding rootBinding
     */
    @Override
    protected void onCastingRootBinding(@Nullable ViewDataBinding rootBinding) {
        if (rootBinding != null) {
            this.castToBaseMVVMBinding(rootBinding);
        } else {
            // reset content view, because auto == false
            this.rootBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_base_mvvm,
                container, false);
            this.castToBaseMVVMBinding(this.rootBinding);
        }
    }


    private void castToBaseMVVMBinding(@NonNull ViewDataBinding rootBinding) {
        if (rootBinding instanceof FragmentBaseMvvmBinding) {
            this.castedRootBinding = (FragmentBaseMvvmBinding) rootBinding;
            this.castedRootBinding.setFirstLoading(this.firstLoading);
        }
    }


    public ViewDataBinding getContentBinding() {
        return this.contentBinding;
    }


    public FragmentBaseMvvmBinding getCastedRootBinding() {
        return this.castedRootBinding;
    }


    /**
     * baseFragment init
     */
    @Override
    protected void baseFragmentInit() {
        super.baseFragmentInit();

        this.initBaseFragmentViews();
        // rendering content
        this.renderingContent();
    }


    private void initBaseFragmentViews() {
        if (this.castedRootBinding == null) return;
        this.contentLayout = this.castedRootBinding.baseFragmentContentLayout;
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

    //*************************//
    // First loading progress *//
    //*************************//


    protected void closeFirstLoadingProgress() {
        this.firstLoading.set(false);
    }

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
