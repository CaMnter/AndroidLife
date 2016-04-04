package com.camnter.newlife.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import com.camnter.newlife.R;

public class PhotoDialog extends Dialog implements android.view.View.OnClickListener {
    private PhotoListener mPhotoListener;


    public PhotoDialog(Context context, PhotoListener photoListener) {
        super(context, R.style.photo_dialog);
        getWindow().setGravity(Gravity.CENTER);
        setContentView(R.layout.dialog_photo);
        setCanceledOnTouchOutside(true);
        mPhotoListener = photoListener;

        findViewById(R.id.dialog_gallery).setOnClickListener(this);
        findViewById(R.id.dialog_camera).setOnClickListener(this);
    }


    @Override public void onClick(View v) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            Toast.makeText(getContext(), "SD卡不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.dialog_gallery:
                mPhotoListener.onStartPickPic();
                break;
            case R.id.dialog_camera:
                mPhotoListener.onStartTakePic();
                break;
            default:
                break;
        }
        dismiss();
    }


    public interface PhotoListener {
        void onStartTakePic();

        void onStartPickPic();
    }
}
