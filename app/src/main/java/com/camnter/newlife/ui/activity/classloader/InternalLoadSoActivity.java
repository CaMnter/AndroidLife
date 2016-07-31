package com.camnter.newlife.ui.activity.classloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：InternalLoadSoActivity
 * Created by：CaMnter
 */

public class InternalLoadSoActivity extends BaseAppCompatActivity implements View.OnClickListener {

    @Bind(R.id.internal_load_so_image) ImageView internalLoadSoImage;
    @Bind(R.id.internal_load_so_button) Button internalLoadSoButton;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_internal_load_so;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.internalLoadSoButton.setOnClickListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        try {
            System.loadLibrary("stackblur");
            NativeBlurProcess.isLoadLibraryOk.set(true);
            Log.i("InternalLoadSoActivity", "loadLibrary success!");
        } catch (Throwable throwable) {
            Log.i("InternalLoadSoActivity", "loadLibrary error!" + throwable);
        }
    }


    public void onDoBlur() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_mm_1);
        Bitmap blur = NativeBlurProcess.blur(bitmap, 20, false);
        internalLoadSoImage.setImageBitmap(blur);
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.internal_load_so_button:
                this.onDoBlur();
                break;
        }
    }
}
