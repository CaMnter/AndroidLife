package com.camnter.newlife.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.TextView;
import com.camnter.newlife.R;

/**
 * Description：CustomDialog
 * Created by：CaMnter
 * Time：2015-12-09 17:38
 */
public class CustomDialog extends Dialog {

    private Context context;
    private TextView dialogTV;

    private static final long DEFAULT_DURATION = 1000L;
    private static final String DEFAULT_CONTENT = "";

    private long duration;
    private String content;

    private DialogCallback callback;


    public CustomDialog(Context context) {
        super(context, R.style.custom_dialog);
        this.initViews(context);
    }


    /**
     * Creates a dialog window that uses a custom dialog style.
     * <p/>
     * The supplied {@code context} is used to obtain the window manager and
     * base theme used to present the dialog.
     * <p/>
     * The supplied {@code theme} is applied on top of the context's theme. See
     * <a href="{@docRoot}guide/topics/resources/available-resources.html#stylesandthemes">
     * Style and Theme Resources</a> for more information about defining and
     * using styles.
     *
     * @param context the context in which the dialog should run
     * @param themeResId a style resource describing the theme to use for the
     * window, or {@code 0} to use the default dialog theme
     */
    public CustomDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.initViews(context);
    }


    public CustomDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.initViews(context);
    }


    private void initViews(Context context) {
        this.context = context;
        this.setContentView(R.layout.dialog_custom);
        this.dialogTV = (TextView) this.findViewById(R.id.custom_dialog_tv);
    }


    @Override public void show() {
        super.show();
        this.dialogTV.setText(TextUtils.isEmpty(this.content) ? DEFAULT_CONTENT : this.content);
        long showDuration = this.duration > 0L ? this.duration : DEFAULT_DURATION;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (CustomDialog.this.isShowing()) {
                    if (CustomDialog.this.context != null) CustomDialog.this.dismiss();
                    if (CustomDialog.this.callback != null) CustomDialog.this.callback.onDismiss();
                }
            }
        }, showDuration);
    }


    public void setTextDrawable(Drawable drawable) {
        if (drawable == null) return;
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        this.dialogTV.setCompoundDrawables(drawable, null, null, null);
    }


    public interface DialogCallback {
        void onDismiss();
    }


    public CustomDialog setDuration(long duration) {
        this.duration = duration;
        return this;
    }


    public CustomDialog setContent(String content) {
        this.content = content;
        return this;
    }


    public CustomDialog setDrawable(Drawable drawable) {
        this.setTextDrawable(drawable);
        return this;
    }


    public CustomDialog setCallback(DialogCallback callback) {
        this.callback = callback;
        return this;
    }
}
