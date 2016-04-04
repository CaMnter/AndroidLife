package com.camnter.newlife.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import com.camnter.newlife.R;

/**
 * Description：CustomDialog
 * Created by：CaMnter
 * Time：2015-12-10 14:38
 */
public class MenuDialog extends Dialog {

    private TextView caseTV;
    private TextView helpTV;


    public MenuDialog(Context context) {
        super(context, R.style.menu_dialog);
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
    public MenuDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.initViews(context);
    }


    public MenuDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.initViews(context);
    }


    private void initViews(Context context) {
        this.setContentView(R.layout.dialog_menu);
        this.caseTV = (TextView) this.findViewById(R.id.dialog_menu_case_tv);
        this.helpTV = (TextView) this.findViewById(R.id.dialog_menu_help_tv);
    }


    /**
     * @param listener listener
     */
    public void setCaseListener(View.OnClickListener listener) {
        this.caseTV.setOnClickListener(listener);
    }


    /**
     * @param listener listener
     */
    public void setHelpListener(View.OnClickListener listener) {
        this.helpTV.setOnClickListener(listener);
    }


    public void setTextDrawable(Drawable drawable) {
        if (drawable == null) return;
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
    }
}