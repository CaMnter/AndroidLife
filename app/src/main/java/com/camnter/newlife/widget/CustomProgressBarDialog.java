package com.camnter.newlife.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.camnter.newlife.R;

/**
 * Description：CustomProgressBarDialog 自定义进度条Dialog
 * Created by：CaMnter
 * Time：2015-12-01 12:41
 */
public class CustomProgressBarDialog extends Dialog {

    private LayoutInflater mInflater;
    private Context mContext;
    private WindowManager.LayoutParams params;
    private View mView;
    private TextView promptTV;


    public CustomProgressBarDialog(Context context) {
        super(context);
        this.init(context);
    }


    public CustomProgressBarDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.init(context);
    }


    protected CustomProgressBarDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.init(context);
    }


    private void init(Context context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.mView = this.mInflater.inflate(R.layout.dialog_progressbar, null);
        setContentView(this.mView);

        // 设置window属性
        this.params = getWindow().getAttributes();
        this.params.gravity = Gravity.CENTER;
        // 去背景遮盖
        this.params.dimAmount = 0;
        this.params.alpha = 1.0f;
        // 不能关掉
        this.setCancelable(false);
        this.getWindow().setAttributes(this.params);
        this.promptTV = (TextView) findViewById(R.id.load_info_text);
    }


    /**
     * 设置内容
     */
    public void setLoadPrompt(String prompt) {
        this.promptTV.setText(prompt);
    }
}
