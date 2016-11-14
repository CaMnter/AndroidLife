package com.camnter.newlife.utils.camera;

import android.app.Activity;
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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseActivity;
import com.camnter.newlife.utils.BitmapUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Description：IdCardCameraActivity
 * Created by：CaMnter
 */

public class IdCardCameraActivity extends BaseActivity implements
    SurfaceHolder.Callback,
    Camera.PictureCallback {

    public static final String RESULT_DATA_KEY_WATER_MARK_BITMAP
        = "result_data_key_water_mark_bitmap";

    private static final int LAYOUT_TYPE_TAKE_PICTURE = 0x261;
    private static final int LAYOUT_TYPE_PICTURE_PREVIEW = 0x262;


    @IntDef({ LAYOUT_TYPE_TAKE_PICTURE, LAYOUT_TYPE_PICTURE_PREVIEW })
    @Retention(RetentionPolicy.SOURCE)
    public @interface LayoutType {
    }


    @BindView(R.id.id_card_camera_surface_view)
    SurfaceView cameraSurfaceView;
    @BindView(R.id.id_card_surface_preview)
    CameraPreviewView surfacePreview;
    @BindView(R.id.id_card_camera_operation_layout)
    RelativeLayout cameraOperationLayout;

    @BindView(R.id.id_card_preview_operation_layout)
    RelativeLayout previewOperationLayout;
    @BindView(R.id.id_card_preview_image)
    ImageView previewImage;

    private CameraManager cameraManager;
    private boolean hasSurface;
    private boolean openLight;
    private boolean isTaking;

    // 用于缓存最后一次 takePicture 的 bitmap（带水印）
    private Bitmap watermarkBitmap;


    public static void startActivity(@NonNull final Context context) {
        context.startActivity(new Intent(context, IdCardCameraActivity.class));
    }


    public static void startActivityForResult(@NonNull final Activity activity,
                                              final int requestCode) {
        activity.startActivityForResult(new Intent(activity, IdCardCameraActivity.class),
            requestCode);
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
        this.initCameraSurface();
    }


    private void initCameraSurface() {
        this.cameraManager = CameraManager.getInstance();
        SurfaceHolder holder = this.cameraSurfaceView.getHolder();
        if (this.hasSurface) {
            // activity 在 paused 时但不会 stopped,因此 surface 仍旧存在；
            // surfaceCreated() 不会调用，因此在这里初始化 camera
            this.initCamera(holder);
        } else {
            // 重置 callback，等待 surfaceCreated() 来初始化camera
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
        if (holder == null) return;
        try {
            this.cameraManager.openCamera(holder);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.cameraManager.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
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


    @OnClick({ R.id.id_card_camera_take_button, R.id.id_card_camera_light_button,
                 R.id.id_card_preview_reset_text, R.id.id_card_preview_confirm_text })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_card_camera_take_button:
                if (this.isTaking) return;
                this.cameraManager.takePicture(null, null, this);
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
            case R.id.id_card_preview_reset_text:
                this.switchLayoutType(LAYOUT_TYPE_TAKE_PICTURE);
                this.watermarkBitmap = null;
                break;
            case R.id.id_card_preview_confirm_text:
                new FileSaveTask(this.getApplicationContext()).execute(this.watermarkBitmap);
                Intent data = new Intent();
                data.putExtra(RESULT_DATA_KEY_WATER_MARK_BITMAP, this.watermarkBitmap);
                this.setResult(RESULT_OK, data);
                this.finish();
                break;
        }
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        final BitmapFactory.Options tempOptions = new BitmapFactory.Options();
        tempOptions.inJustDecodeBounds = false;
        final Bitmap originalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
            tempOptions);

        // 压缩
        final Bitmap expectBitmap = this.cameraManager.compressForIdCard(originalBitmap);
        // TODO 水印
        this.watermarkBitmap = expectBitmap;
        // 旋转后的图片记录，为了在确认的时候保存
        final Bitmap previewBitmap = BitmapUtils.rotate(expectBitmap, 90.f);
        this.previewImage.setImageBitmap(previewBitmap);

        this.showToast("拍照成功", Toast.LENGTH_SHORT);
        this.cameraManager.mustPreview();
        this.isTaking = false;

        this.switchLayoutType(LAYOUT_TYPE_PICTURE_PREVIEW);
    }


    private void switchLayoutType(@LayoutType final int layoutType) {
        switch (layoutType) {
            case LAYOUT_TYPE_TAKE_PICTURE:
                this.cameraSurfaceView.setVisibility(View.VISIBLE);
                this.surfacePreview.setVisibility(View.VISIBLE);
                this.cameraOperationLayout.setVisibility(View.VISIBLE);
                this.previewImage.setVisibility(View.GONE);
                this.previewOperationLayout.setVisibility(View.GONE);
                this.cameraManager = CameraManager.getInstance();
                break;
            case LAYOUT_TYPE_PICTURE_PREVIEW:
                this.cameraSurfaceView.setVisibility(View.GONE);
                this.surfacePreview.setVisibility(View.GONE);
                this.cameraOperationLayout.setVisibility(View.GONE);
                this.previewImage.setVisibility(View.VISIBLE);
                this.previewOperationLayout.setVisibility(View.VISIBLE);
                break;
        }
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
