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
import android.widget.FrameLayout;
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
    Camera.PictureCallback,
    CameraPreviewView.PreviewListener {

    private static final String DEFAULT_PACKAGE_NAME = "AndroidLife";
    private static final String DEFAULT_PICTURE_EXTENSION_NAME = ".jpg";

    private static final String INTENT_EXTRA_KEY_PROMPT_VIEW_TYPE
        = "intent_extra_key_prompt_view_type";
    public static final String RESULT_DATA_KEY_WATER_MARK_BITMAP_PATH
        = "result_data_key_water_mark_bitmap_path";

    private static final int LAYOUT_TYPE_TAKE_PICTURE = 0x261;
    private static final int LAYOUT_TYPE_PICTURE_PREVIEW = 0x262;


    @IntDef({ LAYOUT_TYPE_TAKE_PICTURE, LAYOUT_TYPE_PICTURE_PREVIEW })
    @Retention(RetentionPolicy.SOURCE)
    public @interface LayoutType {
    }


    public static final int PROMPT_FRONT = 0x261;
    public static final int PROMPT_REVERSE = 0x262;


    @IntDef({ PROMPT_FRONT, PROMPT_REVERSE })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PromptViewType {
    }


    @PromptViewType
    private int promptViewType;

    @BindView(R.id.id_card_camera_surface_view)
    SurfaceView cameraSurfaceView;
    @BindView(R.id.id_card_surface_preview)
    CameraPreviewView surfacePreview;
    @BindView(R.id.id_card_camera_operation_layout)
    RelativeLayout cameraOperationLayout;
    @BindView(R.id.id_card_camera_top_bar_layout)
    FrameLayout topBarLayout;
    @BindView(R.id.id_card_camera_light_image)
    ImageView lightImage;
    @BindView(R.id.id_card_prompt_front_image)
    ImageView promptFrontImage;
    @BindView(R.id.id_card_prompt_reverse_image)
    ImageView promptReverseImage;

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


    public static void startActivityForResult(@NonNull final Activity activity,
                                              @PromptViewType final int promptViewType,
                                              final int requestCode) {
        Intent intent = new Intent(activity, IdCardCameraActivity.class);
        intent.putExtra(INTENT_EXTRA_KEY_PROMPT_VIEW_TYPE, promptViewType);
        activity.startActivityForResult(intent, requestCode);
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
        this.surfacePreview.setPreviewListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        this.promptViewType =
            this.getIntent().getIntExtra(INTENT_EXTRA_KEY_PROMPT_VIEW_TYPE, PROMPT_FRONT) ==
                PROMPT_FRONT ?
            PROMPT_FRONT : PROMPT_REVERSE;
        this.surfacePreview.setPromptViewType(this.promptViewType);
        this.surfacePreview.setDrawMode(CameraPreviewView.DRAW_MODE_BY_DRAWABLE);
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
            // this.initCamera(holder);
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


    private void initCamera(SurfaceHolder holder, int surfaceViewWidth, int surfaceViewHeight) {
        if (holder == null) return;
        try {
            this.cameraManager.openCamera(holder, surfaceViewWidth, surfaceViewHeight);
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
        this.initCamera(holder, this.cameraSurfaceView.getWidth(),
            this.cameraSurfaceView.getHeight());
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!this.hasSurface) return;
        this.hasSurface = false;
        this.cameraManager.stopPreview();
    }


    @Override
    public void notificationFrontImageView(int frontImageMarginTop, int frontImageMarginLeft) {
        this.promptFrontImage.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams
            = (RelativeLayout.LayoutParams) this.promptFrontImage.getLayoutParams();
        layoutParams.setMargins(frontImageMarginLeft, frontImageMarginTop, 0, 0);
        this.promptFrontImage.setLayoutParams(layoutParams);
    }


    @Override
    public void notificationReverseImageView(int reverseImageMarginTop, int reverseImageMarginLeft) {
        this.promptReverseImage.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams
            = (RelativeLayout.LayoutParams) this.promptReverseImage.getLayoutParams();
        layoutParams.setMargins(reverseImageMarginLeft, reverseImageMarginTop, 0, 0);
        this.promptReverseImage.setLayoutParams(layoutParams);
    }


    @OnClick({ R.id.id_card_camera_take_image,
                 R.id.id_card_camera_light_image,
                 R.id.id_card_preview_reset_text,
                 R.id.id_card_preview_confirm_text,
                 R.id.id_card_camera_close_image })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_card_camera_take_image:
                if (this.isTaking) return;
                this.cameraManager.takePicture(null, null, this);
                break;
            case R.id.id_card_camera_light_image:
                if (this.openLight) {
                    this.openLight = false;
                    this.cameraManager.closeLight();
                    this.lightImage.setImageResource(R.drawable.icon_camera_light_close);
                } else {
                    this.openLight = true;
                    this.cameraManager.openLight();
                    this.lightImage.setImageResource(R.drawable.icon_camera_light_open);
                }
                break;
            case R.id.id_card_preview_reset_text:
                this.switchLayoutType(LAYOUT_TYPE_TAKE_PICTURE);
                this.watermarkBitmap = null;
                break;
            case R.id.id_card_preview_confirm_text:
                final String picturePath = this.createPicturePath();
                new FileSaveTask(this.getApplicationContext(), picturePath).execute(
                    this.watermarkBitmap);

                final Intent data = new Intent();
                data.putExtra(RESULT_DATA_KEY_WATER_MARK_BITMAP_PATH, picturePath);
                this.setResult(RESULT_OK, data);
                this.finish();
                break;
            case R.id.id_card_camera_close_image:
                this.finish();
                break;
        }
    }


    private String createPicturePath() {
        return Environment.getExternalStorageDirectory().getAbsoluteFile() +
            File.separator +
            DEFAULT_PACKAGE_NAME +
            File.separator +
            UUID.randomUUID().toString() +
            DEFAULT_PICTURE_EXTENSION_NAME;
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            final BitmapFactory.Options tempOptions = new BitmapFactory.Options();
            tempOptions.inJustDecodeBounds = false;
            final Bitmap originalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                tempOptions);

            // 压缩
            final Bitmap expectBitmap = this.cameraManager.compressForIdCard(originalBitmap);
            // 水印
            this.watermarkBitmap = this.cameraManager.addWatermarkBitmap(this,
                expectBitmap,
                R.drawable.ic_camnter);
            // 旋转后的图片记录，为了在确认的时候保存
            final Bitmap previewBitmap = BitmapUtils.rotate(watermarkBitmap, 90.f);
            this.previewImage.setImageBitmap(previewBitmap);

            this.showToast("拍照成功", Toast.LENGTH_SHORT);
            this.cameraManager.mustPreview();
            this.isTaking = false;

            this.switchLayoutType(LAYOUT_TYPE_PICTURE_PREVIEW);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    private void switchLayoutType(@LayoutType final int layoutType) {
        switch (layoutType) {
            case LAYOUT_TYPE_TAKE_PICTURE:
                this.cameraSurfaceView.setVisibility(View.VISIBLE);
                this.surfacePreview.setVisibility(View.VISIBLE);
                this.cameraOperationLayout.setVisibility(View.VISIBLE);
                this.topBarLayout.setVisibility(View.VISIBLE);
                this.previewImage.setVisibility(View.GONE);
                this.previewOperationLayout.setVisibility(View.GONE);
                break;
            case LAYOUT_TYPE_PICTURE_PREVIEW:
                // close light
                this.cameraManager.closeLight();
                this.lightImage.setImageResource(this.cameraManager.isLightOpen() ?
                                                 R.drawable.icon_camera_light_open :
                                                 R.drawable.icon_camera_light_close);

                this.cameraSurfaceView.setVisibility(View.GONE);
                this.surfacePreview.setVisibility(View.GONE);
                this.cameraOperationLayout.setVisibility(View.GONE);
                this.topBarLayout.setVisibility(View.GONE);
                this.previewImage.setVisibility(View.VISIBLE);
                this.previewOperationLayout.setVisibility(View.VISIBLE);
                break;
        }
    }


    private static class FileSaveTask extends AsyncTask<Bitmap, Object, String[]> {

        private final String picturePath;
        private final WeakReference<Context> contextWeakReference;


        private FileSaveTask(Context context, String picturePath) {
            this.contextWeakReference = new WeakReference<>(context);
            this.picturePath = picturePath;
        }


        @Override
        protected String[] doInBackground(Bitmap... params) {
            if (params == null || params.length == 0) return null;
            return this.savePicture(this.picturePath, params[0]);
        }


        @Override
        protected void onPostExecute(String[] results) {
            Context context;
            if (results == null || (context = this.contextWeakReference.get()) == null) return;
            if (results.length < 2) return;
            this.notificationMediaStore(context, results[0], results[1]);
        }


        /**
         * @param picturePath picturePath
         * @param expectBitmap expectBitmap
         * @return absolutePath , name
         */
        public String[] savePicture(@NonNull final String picturePath,
                                    @NonNull final Bitmap expectBitmap) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File expectFile = new File(picturePath);
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
            final String path = "file://" + absolutePath;
            // 其次把文件插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    path, fileName, null);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            // 最后通知图库更新
            context.sendBroadcast(
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(path)));
        }

    }

}
