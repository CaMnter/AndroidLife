package com.camnter.newlife.utils.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import com.camnter.newlife.utils.BitmapUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Description：CameraManager
 * Created by：CaMnter
 */

public final class CameraManager {

    /*******************************
     * 输出格式尽量保持 1280 x 720  *
     *******************************/

    private static final int PICTURE_OUTPUT_WIDTH = 1280;
    private static final int PICTURE_OUTPUT_HEIGHT = 720;

    private static final String TAG = CameraManager.class.getSimpleName();
    // 1kb
    private static final int ONE_KB = 1024;
    private static final int ONE_MB = ONE_KB * 1024;
    private static final int REQUEST_CAMERA_ID = -1;
    private volatile static CameraManager instance = null;
    private Camera camera;
    private Camera.Parameters cameraParameters;
    private AutoFocusManager autoFocusManager;
    private boolean isPreviewing = false;

    private static final int ID_CARD_EXPECT_HEIGHT = 720;

    private final Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width > rhs.width) {
                return -1;
            } else if (lhs.width == rhs.width) {
                return 0;
            } else {
                // lhs.width < rhs.width
                return 1;
            }
        }
    };


    private CameraManager() {

    }


    public static CameraManager getInstance() {
        CameraManager inst = instance;
        if (inst == null) {
            synchronized (CameraManager.class) {
                inst = instance;
                if (inst == null) {
                    inst = new CameraManager();
                    instance = inst;
                }
            }
        }
        return inst;
    }


    /**
     * 打开 Camera
     *
     * @param surfaceHolder surfaceHolder
     * @throws Exception e
     */
    public synchronized void openCamera(@NonNull final SurfaceHolder surfaceHolder)
        throws Exception {
        Log.i(TAG, "[openCamera]:......");
        Camera cameraTemp = this.camera;
        if (cameraTemp == null) {
            cameraTemp = this.obtainCamera(REQUEST_CAMERA_ID);
            if (cameraTemp == null) {
                throw new Exception(TAG + "\t\t\t[openCamera]\t\t\t camera == null ");
            }
            this.camera = cameraTemp;
        }
        this.adjustCamera();
        this.camera.setPreviewDisplay(surfaceHolder);

        this.cameraParameters = this.camera.getParameters();
        this.cameraParameters.setPictureFormat(ImageFormat.JPEG);
        this.cameraParameters.setPictureSize(1920, 1080);
        this.cameraParameters.setJpegQuality(100);
        this.setBestPreviewAndPictureSize(this.cameraParameters);
        this.camera.setParameters(this.cameraParameters);
    }


    public void adjustCamera() {
        if (this.camera != null) {
            this.camera.setDisplayOrientation(90);
        }
    }


    /**
     * 查看了源码，没有进行排序
     * 不同机型 和 系统版本会导致不同版本
     * 所有这里会先进行排序
     *
     * 尽量优先找 1280 x 720 的
     * 然后再按照一下规则找
     *
     * 由于是自定义相机，所以采用最好的 previewSize
     * 然后为了图片查看页面和预览页面的图片一致 pictureSize 采用和 previewSize 相同尺寸
     *
     * @param parameters parameters
     */
    private void setBestPreviewAndPictureSize(@NonNull Camera.Parameters parameters) {
        final List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        if (previewSizes == null || previewSizes.size() == 0) return;

        Collections.sort(previewSizes, sizeComparator);

        /**********************
         * 1280 x 720 优先逻辑 *
         **********************/

        // 1280 x 720
        boolean foundExpectPreivew = false;
        for (Camera.Size previewSize : previewSizes) {
            if (previewSize.width == PICTURE_OUTPUT_WIDTH &&
                previewSize.height == PICTURE_OUTPUT_HEIGHT) {
                foundExpectPreivew = true;
            }
        }
        if (foundExpectPreivew) {
            boolean foundExpectSize = false;
            for (Camera.Size size : parameters.getSupportedPictureSizes()) {
                if (size.width == PICTURE_OUTPUT_WIDTH && size.height == PICTURE_OUTPUT_HEIGHT) {
                    foundExpectSize = true;
                }
            }
            if (foundExpectSize) {
                parameters.setPictureSize(PICTURE_OUTPUT_WIDTH, PICTURE_OUTPUT_HEIGHT);
                parameters.setPictureSize(PICTURE_OUTPUT_WIDTH, PICTURE_OUTPUT_HEIGHT);
                return;
            }
        }

        /********************
         * 最髙 Preview 逻辑 *
         ********************/

        Camera.Size bestPreviewSize = previewSizes.get(0);
        // preview size
        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        boolean foundPicture = false;
        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (size.width == bestPreviewSize.width && size.height == bestPreviewSize.height) {
                foundPicture = true;
            }
        }
        if (foundPicture) {
            this.cameraParameters.setPictureSize(bestPreviewSize.width, bestPreviewSize.height);
        } else {
            final List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
            if (pictureSizes == null || pictureSizes.size() == 0) return;

            Collections.sort(pictureSizes, sizeComparator);
            final Camera.Size bestPictureSize = pictureSizes.get(0);
            this.cameraParameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
        }
    }


    /**
     * 关闭 Camera
     */
    public synchronized void closeCamera() {
        Log.i(TAG, "[closeCamera]:......");
        if (this.camera == null) return;
        this.camera.release();
        this.camera = null;
    }


    /**
     * 打开预览
     */
    public synchronized void startPreview() {
        Log.i(TAG, "[startPreview]:......");
        final Camera cameraTemp = this.camera;
        if (cameraTemp != null && !this.isPreviewing) {
            cameraTemp.startPreview();
            this.isPreviewing = true;
            this.autoFocusManager = new AutoFocusManager(this.camera);
        }
    }


    public synchronized void mustPreview() {
        Log.i(TAG, "[mustPreview]:......");
        final Camera cameraTemp = this.camera;
        if (cameraTemp == null) return;
        cameraTemp.startPreview();
    }


    /**
     * 关闭预览
     */
    public synchronized void stopPreview() {
        Log.i(TAG, "[stopPreview]:......");
        if (this.autoFocusManager != null) {
            this.autoFocusManager.stopFocus();
            this.autoFocusManager = null;
        }
        if (this.camera != null && this.isPreviewing) {
            this.camera.stopPreview();
            this.isPreviewing = false;
        }
    }


    /**
     * 打开闪光灯
     */
    public synchronized void openLight() {
        Log.i(TAG, "[openLight]:......");
        if (this.camera == null) return;
        this.cameraParameters = this.camera.getParameters();
        this.cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        this.camera.setParameters(this.cameraParameters);
    }


    /**
     * 关闭闪光灯
     */
    public synchronized void closeLight() {
        Log.i(TAG, "[closeLight]:......");
        if (this.camera == null) return;
        this.cameraParameters = this.camera.getParameters();
        this.cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        this.camera.setParameters(this.cameraParameters);
    }


    public synchronized void takePicture(@Nullable final Camera.ShutterCallback shutter,
                                         @Nullable final Camera.PictureCallback raw,
                                         @Nullable final Camera.PictureCallback jpeg) {
        Log.i(TAG, "[takePicture]:......");
        this.camera.takePicture(shutter, raw, jpeg);
    }


    /**
     * 是否打开 Camera
     *
     * @return 是否打开
     */
    public synchronized boolean isOpen() {
        return this.camera != null;
    }


    /**
     * 获取 背面摄像头
     *
     * @param cameraId cameraId
     * @return Camera
     */
    private Camera obtainCamera(int cameraId) {
        final int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras <= 0) {
            Log.e(TAG, "[obtainCamera]\t\t\t the number of cameras was 0");
            return null;
        }

        final boolean explicitRequest = cameraId >= 0;
        if (!explicitRequest) {
            int backFocusIndex = 0;
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    backFocusIndex = i;
                    break;
                }
            }
            cameraId = backFocusIndex;
        }

        Camera camera;
        if (cameraId < numberOfCameras) {
            Log.i(TAG, "[obtainCamera]\t\t\t open the camera: " + cameraId);
            camera = Camera.open(cameraId);
        } else {
            if (explicitRequest) {
                Log.e(TAG, "[obtainCamera]\t\t\t Requested camera does not exist: " + cameraId);
                camera = null;
            } else {
                Log.e(TAG, "[obtainCamera]\t\t\t No camera facing back; returning camera #0");
                camera = Camera.open(0);
            }
        }
        return camera;
    }


    private int getBitmapSize(@NonNull final Bitmap bitmap) {
        // API 19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        }
        // API 12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        // earlier version
        return bitmap.getRowBytes() * bitmap.getHeight();
    }


    /**
     * 压缩图片
     * 根据 height 压到 720，width 的话，根据 高度缩放比例，压到对应的宽度
     *
     * @param originalBitmap originalBitmap
     * @return Bitmap
     */
    public Bitmap compressForIdCard(@NonNull final Bitmap originalBitmap) {
        if (this.getBitmapSize(originalBitmap) > ONE_MB) {
            return BitmapUtils.getBitmapCompressedByHeight(originalBitmap, ID_CARD_EXPECT_HEIGHT);
        } else {
            return originalBitmap;
        }
    }


    /**
     * 添加 身份证水印
     *
     * @param context context
     * @param originalBitmap originalBitmap
     * @param watermarkRes watermarkRes
     * @return watermarkBitmap
     */
    public Bitmap addWatermarkBitmap(@NonNull final Context context,
                                     @NonNull final Bitmap originalBitmap,
                                     @DrawableRes final int watermarkRes) {
        if (watermarkRes == 0) return originalBitmap;

        final float backgroundBitmapWidth = originalBitmap.getWidth();
        final float backgroundBitmapHeight = originalBitmap.getHeight();

        final Bitmap backgroundBitmap = Bitmap.createBitmap((int) backgroundBitmapWidth,
            (int) backgroundBitmapHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(backgroundBitmap);

        // 背景
        canvas.drawBitmap(originalBitmap, 0, 0, null);

        final Bitmap markBitmap = BitmapFactory.decodeResource(context.getResources(),
            watermarkRes);
        final float markBitmapWidth = markBitmap.getWidth();
        final float markBitmapHeight = markBitmap.getHeight();

        final float marginTop = backgroundBitmapHeight / 2 - markBitmapHeight / 2;
        final float marginLeft = backgroundBitmapWidth / 2 - markBitmapWidth / 2;

        canvas.drawBitmap(markBitmap, marginLeft, marginTop, null);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return backgroundBitmap;
    }

}
