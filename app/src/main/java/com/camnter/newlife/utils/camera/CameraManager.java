package com.camnter.newlife.utils.camera;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import com.camnter.newlife.utils.BitmapUtils;

/**
 * Description：CameraManager
 * Created by：CaMnter
 */

public final class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();
    // 1kb
    private static final int ONE_KB = 1024;
    private static final int ONE_MB = 1024 * 1024;
    private static final int REQUEST_CAMERA_ID = -1;
    private volatile static CameraManager instance = null;
    private Camera camera;
    private Camera.Parameters cameraParameters;
    private AutoFocusManager autoFocusManager;
    private boolean isInitialized = false;
    private boolean isPreviewing = false;


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
        cameraTemp.setPreviewDisplay(surfaceHolder);

        if (this.isInitialized) return;

        this.isInitialized = true;
        this.cameraParameters = this.camera.getParameters();
        this.cameraParameters.setPictureFormat(ImageFormat.JPEG);
        this.cameraParameters.setPictureSize(1280, 960);
        this.cameraParameters.setJpegQuality(100);
        cameraTemp.setParameters(this.cameraParameters);
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


    public Bitmap adjustBitmap(@NonNull final Bitmap originalBitmap) {
        if (this.getBitmapSize(originalBitmap) > ONE_MB) {
            return BitmapUtils.getBitmapCompressed(originalBitmap, 800);
        } else {
            return originalBitmap;
        }
    }

}
