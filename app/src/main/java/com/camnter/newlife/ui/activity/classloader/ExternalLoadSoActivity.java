package com.camnter.newlife.ui.activity.classloader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Description：ExternalLoadSoActivity
 * Created by：CaMnter
 */

public class ExternalLoadSoActivity extends BaseAppCompatActivity {

    @Bind(R.id.external_load_so_image) ImageView externalLoadSoImage;
    @Bind(R.id.external_load_so_button) Button externalLoadSoButton;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_external_load_so;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @SuppressLint("UnsafeDynamicallyLoadedCode")
    @Override
    protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        File dir = this.getDir("jniLibs", Activity.MODE_PRIVATE);
        File distFile = new File(dir.getAbsolutePath() + File.separator + "libstackblur.so");
        if (copyFileFromAssets(getApplicationContext(), "libstackblur.so", distFile.getAbsolutePath())) {
            //使用load方法加载内部储存的SO库
            System.load(distFile.getAbsolutePath());
            NativeBlurProcess.isLoadLibraryOk.set(true);
        }
    }


    public void onDoBlur(View view) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
            R.drawable.img_mm_1);
        Bitmap blur = NativeBlurProcess.blur(bitmap, 20, false);
        externalLoadSoImage.setImageBitmap(blur);
    }


    public static boolean copyFileFromAssets(Context context, String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity", "[copyFileFromAssets] IOException "+e.toString());
        }
        return copyIsFinish;
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }
}
