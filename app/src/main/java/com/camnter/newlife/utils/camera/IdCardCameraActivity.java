package com.camnter.newlife.utils.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseActivity;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Description：IdCardCameraActivity
 * Created by：CaMnter
 */

public class IdCardCameraActivity extends BaseActivity implements
    SurfaceHolder.Callback,
    Camera.PictureCallback {

    @BindView(R.id.id_card_camera_surface_view)
    SurfaceView cameraSurfaceView;
    @BindView(R.id.id_card_surface_preview)
    CameraPreviewView surfacePreview;

    private CameraManager cameraManager;
    private boolean hasSurface;
    private boolean openLight;
    private boolean isTaking;


    public static void startActivity(@NonNull final Context context) {
        context.startActivity(new Intent(context, IdCardCameraActivity.class));
    }


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_id_card_camera;
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

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }


    @Override
    protected void onResume() {
        super.onResume();
        this.cameraManager = CameraManager.getInstance();
        SurfaceHolder holder = this.cameraSurfaceView.getHolder();
        if (hasSurface) {
            this.initCamera(holder);
        } else {
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.cameraManager.stopPreview();
        this.cameraManager.closeCamera();
        if (this.hasSurface) return;
        SurfaceHolder holder = this.cameraSurfaceView.getHolder();
        holder.removeCallback(this);
    }


    private void initCamera(SurfaceHolder holder) {
        if (holder == null || this.cameraManager.isOpen()) return;
        try {
            this.cameraManager.openCamera(holder);
            this.cameraManager.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (this.hasSurface) return;
        this.hasSurface = true;
        this.initCamera(holder);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!this.hasSurface) return;
        this.hasSurface = false;
    }


    @OnClick({ R.id.id_card_camera_take_button, R.id.id_card_camera_light_button })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_card_camera_take_button:
                if (this.isTaking) return;
                this.cameraManager.takePicture(null, null, this);
                this.isTaking = true;
                break;
            case R.id.id_card_camera_light_button:
                if (this.openLight) {
                    this.openLight = false;
                    this.cameraManager.closeLight();
                } else {
                    this.openLight = true;
                    this.cameraManager.openLight();
                }
                break;
        }
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        final BitmapFactory.Options tempOptions = new BitmapFactory.Options();
        tempOptions.inJustDecodeBounds = false;
        final Bitmap originalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
            tempOptions);

        Bitmap expectBitmap = this.surfacePreview.cropToPreviewBitmap(originalBitmap);
        expectBitmap = this.cameraManager.adjustBitmap(expectBitmap);

        this.showToast("拍照成功", Toast.LENGTH_SHORT);
        this.cameraManager.mustPreview();
        this.isTaking = false;

        new FileSaveTask(this.getApplicationContext()).execute(expectBitmap);
    }


    private static class FileSaveTask extends AsyncTask<Bitmap, Object, String[]> {

        private static final String DEFAULT_PACKAGE_NAME = "Lmlc";
        private static final String DEFAULT_PICTURE_EXTENSION_NAME = ".jpg";

        private final WeakReference<Context> contextWeakReference;


        private FileSaveTask(Context context) {
            this.contextWeakReference = new WeakReference<>(context);
        }


        @Override
        protected String[] doInBackground(Bitmap... params) {
            if (params == null || params.length == 0) return null;
            return this.savePicture(DEFAULT_PACKAGE_NAME, params[0],
                DEFAULT_PICTURE_EXTENSION_NAME);
        }


        @Override
        protected void onPostExecute(String[] results) {
            Context context;
            if (results == null || (context = this.contextWeakReference.get()) == null) return;
            if (results.length < 2) return;
            this.notificationMediaStore(context, results[0], results[1]);
        }


        /**
         * @param packageName packageName
         * @param expectBitmap expectBitmap
         * @param extensionName extensionName
         * @return absolutePath , name
         */
        public String[] savePicture(@NonNull final String packageName,
                                    @NonNull final Bitmap expectBitmap,
                                    @NonNull final String extensionName) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File expectFile = new File(
                    Environment.getExternalStorageDirectory().getAbsoluteFile() +
                        File.separator +
                        packageName +
                        File.separator +
                        UUID.randomUUID().toString() +
                        extensionName
                );
                if (!expectFile.getParentFile().exists()) {
                    boolean mkdirsSuccess = expectFile.mkdirs();
                    if (!mkdirsSuccess) return null;
                }
                try {
                    FileOutputStream outStream = new FileOutputStream(expectFile);
                    expectBitmap.compress(Bitmap.CompressFormat.JPEG,
                        100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new String[] { expectFile.getAbsolutePath(), expectFile.getName() };
            } else {
                return null;
            }
        }


        private void notificationMediaStore(@NonNull final Context context,
                                            @NonNull final String absolutePath,
                                            @NonNull final String fileName) {
            // 其次把文件插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    absolutePath, fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 最后通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + absolutePath)));
        }

    }

}
