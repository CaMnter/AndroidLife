package com.camnter.newlife.core;

import android.Manifest;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import com.camnter.newlife.utils.permissions.AfterPermissionGranted;
import com.camnter.newlife.utils.permissions.EasyPermissions;
import java.util.List;

/**
 * Description：BasePermissionActivity
 * Created by：CaMnter
 * Time：2016-05-26 11:55
 */
public abstract class BasePermissionActivity extends BaseAppCompatActivity implements
    EasyPermissions.PermissionCallbacks {

    public static final int PERMISSION_REQUEST_CODE_CAMERA = 0x104;
    public static final int PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE = 0x105;
    public static final int PERMISSION_REQUEST_CODE_READ_CONTACTS = 0x106;
    public static final int PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE_AND_RECORD_AUDIO = 0x107;


    @AfterPermissionGranted(PERMISSION_REQUEST_CODE_CAMERA)
    public void getCameraPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            this.cameraPermissionsGranted();
        } else {
            EasyPermissions.requestPermissions(this, "允许获取 相机权限",
                PERMISSION_REQUEST_CODE_CAMERA, Manifest.permission.CAMERA);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @AfterPermissionGranted(PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE)
    public void getReadExternalStoragePermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            this.readExternalStorageGranted();
        } else {
            EasyPermissions.requestPermissions(this, "允许获取 相册权限",
                PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }


    @AfterPermissionGranted(PERMISSION_REQUEST_CODE_READ_CONTACTS)
    public void getReadContactsPermission() {
        String[] perms = { Manifest.permission.READ_CONTACTS };
        if (EasyPermissions.hasPermissions(this, perms)) {
            this.readContactsGranted();
        } else {
            EasyPermissions.requestPermissions(this, "允许获取 通讯录权限",
                PERMISSION_REQUEST_CODE_READ_CONTACTS, perms);
        }
    }


    @AfterPermissionGranted(PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE_AND_RECORD_AUDIO)
    public void getWriteExternalStorageAndRecordPermission() {
        String[] perms = { Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            this.writeExternalStorageAndRecordAudioGranted();
        } else {
            EasyPermissions.requestPermissions(this, "允许获取 录音权限",
                PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE_AND_RECORD_AUDIO, perms);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_CAMERA:
                this.cameraPermissionsGranted();
                break;
            case PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE:
                this.readExternalStorageGranted();
                break;
            case PERMISSION_REQUEST_CODE_READ_CONTACTS:
                this.readContactsGranted();
                break;
            case PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE_AND_RECORD_AUDIO:
                this.writeExternalStorageAndRecordAudioGranted();
                break;
        }
    }


    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_CAMERA:
                this.showToast("相册权限 授权失败");
                break;
            case PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE:
                this.showToast("相册权限 授权失败");
                break;
            case PERMISSION_REQUEST_CODE_READ_CONTACTS:
                this.showToast("通讯录权限 授权失败");
                break;
            case PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE_AND_RECORD_AUDIO:
                this.showToast("录音权限 授权失败");
                break;
        }
    }


    protected void cameraPermissionsGranted() {
        // No implementation
    }


    protected void readExternalStorageGranted() {
        // No implementation
    }


    protected void readContactsGranted() {
        // No implementation
    }


    protected void writeExternalStorageAndRecordAudioGranted() {
        // No implementation
    }

}
