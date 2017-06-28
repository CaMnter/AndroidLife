package com.camnter.newlife.ui.activity.smartsave;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.camnter.newlife.R;
import com.camnter.newlife.core.activity.BaseAppCompatActivity;
import com.camnter.smartsave.SmartSave;
import com.camnter.smartsave.annotation.Save;
import com.camnter.smartsave.annotation.SaveColor;
import com.camnter.smartsave.annotation.SaveDimension;
import com.camnter.smartsave.annotation.SaveOnClick;
import com.camnter.utils.wrapper.SmartToastWrapper;
import java.util.Locale;

/**
 * @author CaMnter
 */

public class SmartSaveExampleActivity extends BaseAppCompatActivity {

    private static final String FORMAT_TEXT = "%1$s - %2$s";

    @Save(R.id.first_text)
    TextView firstText;
    @Save(R.id.second_text)
    TextView secondText;
    @Save(R.id.third_text)
    TextView thirdText;
    @Save(R.id.extra_image)
    ImageView extraImage;

    @Save(R.string.app_label)
    String appLabel;

    private SmartToastWrapper smartToastWrapper;
    private int firstCount = 0;
    private int secondCount = 0;
    private int thirdCount = 0;

    @Save(R.drawable.img_extra)
    Drawable extraDrawable;

    @SaveColor(R.color.green)
    int green;
    @SaveColor(R.color.yellow)
    int yellow;

    @SaveDimension(R.dimen.smart_save_second_text)
    int secondDimension;
    @SaveDimension(R.dimen.smart_save_third_text)
    float thirdDimension;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_smart_save_example;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void initViews(Bundle savedInstanceState) {
        SmartSave.save(this);
        this.smartToastWrapper = new SmartToastWrapper() {
            @Override
            protected Toast getToast() {
                return Toast.makeText(SmartSaveExampleActivity.this, "", Toast.LENGTH_LONG);
            }
        };
        ViewCompat.setBackground(this.extraImage, this.extraDrawable);
        this.secondText.setTextColor(this.green);
        this.secondText.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.secondDimension);
        this.thirdText.setTextColor(this.yellow);
        this.thirdText.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.thirdDimension);
    }


    /**
     * Initialize the View of the listener
     */
    @Override
    protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override
    protected void initData() {

    }


    @SaveOnClick({ R.id.first_text, R.id.second_text, R.id.third_text })
    void onSaveClick(View v) {
        switch (v.getId()) {
            case R.id.first_text:
                this.firstText.setText(String.format(Locale.getDefault(), FORMAT_TEXT,
                    this.firstText.getText().toString(), ++this.firstCount));
                this.smartToastWrapper.show(this.firstText.getText() + "\n" + this.appLabel);
                break;
            case R.id.second_text:
                this.secondText.setText(String.format(Locale.getDefault(), FORMAT_TEXT,
                    this.secondText.getText().toString(), ++this.secondCount));
                this.smartToastWrapper.show(this.secondText.getText() + "\n" + this.appLabel);
                break;
            case R.id.third_text:
                this.thirdText.setText(String.format(Locale.getDefault(), FORMAT_TEXT,
                    this.thirdText.getText().toString(), ++this.thirdCount));
                this.smartToastWrapper.show(this.thirdText.getText() + "\n" + this.appLabel);
                break;
        }
    }

}
