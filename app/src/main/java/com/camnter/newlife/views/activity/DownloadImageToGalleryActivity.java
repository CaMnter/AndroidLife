package com.camnter.newlife.views.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.ImageUtil;
import com.camnter.newlife.widget.CustomProgressBarDialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Description：DownloadImageToGalleryActivity
 * Created by：CaMnter
 * Time：2015-10-20 15:00
 */
public class DownloadImageToGalleryActivity extends BaseAppCompatActivity
        implements View.OnClickListener {

    private static final String OBJECT_IMAGE_URL = "http://img.blog.csdn.net/20150913233900119";

    private Button saveBT;
    private ImageView saveIV;
    private CustomProgressBarDialog dialog;

    private static final int HANDLER_LOADING = 262;

    /**
     * 刷新Dialog显示的进度
     */
    private static class LoadingHandler extends Handler {
        private final WeakReference<DownloadImageToGalleryActivity> mActivity;


        public LoadingHandler(DownloadImageToGalleryActivity activity) {
            mActivity = new WeakReference<>(activity);
        }


        /**
         * Subclasses must implement this to receive messages.
         */
        @Override public void handleMessage(Message msg) {
            DownloadImageToGalleryActivity activity = this.mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case HANDLER_LOADING: {
                        int progressValue = (int) msg.obj;
                        activity.dialog.setLoadPrompt(progressValue + "%");
                        activity.dialog.show();
                        break;
                    }
                }
            }
        }
    }

    private final LoadingHandler loadingHandler = new LoadingHandler(
            DownloadImageToGalleryActivity.this);


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_download_image_save_to_gallery;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.saveBT = (Button) this.findViewById(R.id.save_bt);
        this.saveIV = (ImageView) this.findViewById(R.id.save_iv);
        this.dialog = new CustomProgressBarDialog(this);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {
        this.saveBT.setOnClickListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        ((TextView) this.findViewById(R.id.save_tv)).setText(OBJECT_IMAGE_URL);
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_bt: {
                v.setEnabled(false);
                /**
                 * 设置按钮不可用，开始执行任务
                 */
                new DownloadImageAsyncTask(this).execute(OBJECT_IMAGE_URL);
                break;
            }
        }
    }


    /**
     * 下载图片异步任务
     */
    public class DownloadImageAsyncTask extends AsyncTask<String, Integer, String> {

        private Activity activity;
        private String localFilePath;


        public DownloadImageAsyncTask(Activity activity) {
            super();
            this.activity = activity;
        }


        /**
         * 对应AsyncTask第一个参数
         * 异步操作，不在主UI线程中，不能对控件进行修改
         * 可以调用publishProgress方法中转到onProgressUpdate(这里完成了一个handler.sendMessage(...)的过程)
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override protected String doInBackground(String... params) {
            URL fileUrl = null;
            try {
                fileUrl = new URL(params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (fileUrl == null) return null;
            try {
                HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();

                //计算文件长度
                int lengthOfFile = connection.getContentLength();
                /**
                 * 不存在SD卡，就放到缓存文件夹内
                 */
                File cacheDir = this.activity.getCacheDir();
                File downloadFile = new File(cacheDir, UUID.randomUUID().toString() + ".jpg");
                this.localFilePath = downloadFile.getPath();
                if (!downloadFile.exists()) {
                    File parent = downloadFile.getParentFile();
                    if (parent != null) parent.mkdirs();
                }
                FileOutputStream output = new FileOutputStream(downloadFile);
                InputStream input = connection.getInputStream();
                InputStream bitmapInput = connection.getInputStream();
                //下载
                byte[] buffer = new byte[1024];
                int len;
                long total = 0;
                // 计算进度
                while ((len = input.read(buffer)) > 0) {
                    total += len;
                    this.publishProgress((int) ((total * 100) / lengthOfFile));
                    output.write(buffer, 0, len);
                }
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        /**
         * 对应AsyncTask第三个参数 (接受doInBackground的返回值)
         * 在doInBackground方法执行结束之后在运行，此时已经回来主UI线程当中 能对UI控件进行修改
         *
         * @param string The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override protected void onPostExecute(String string) {
            /**
             * 设置按钮可用，并隐藏Dialog
             */
            DownloadImageToGalleryActivity.this.saveBT.setEnabled(true);
            DownloadImageToGalleryActivity.this.dialog.hide();

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;
            /**
             * ImageUtil.decodeScaleImage 解析图片
             */
            Bitmap bitmap = ImageUtil.decodeScaleImage(this.localFilePath, screenWidth,
                    screenHeight);
            DownloadImageToGalleryActivity.this.saveIV.setImageBitmap(bitmap);
            /**
             * 保存图片到相册
             */
            String imageName = System.currentTimeMillis() + ".jpg";
            MediaStore.Images.Media.insertImage(
                    DownloadImageToGalleryActivity.this.getApplicationContext()
                                                       .getContentResolver(), bitmap, imageName,
                    "camnter");
            Toast.makeText(this.activity, "已保存：" + imageName, Toast.LENGTH_LONG).show();
        }


        /**
         * 对应AsyncTask第二个参数
         * 在doInBackground方法当中，每次调用publishProgress方法都会中转(handler.sendMessage(...))到onProgressUpdate
         * 在主UI线程中，可以对控件进行修改
         *
         * @param values The values indicating progress.
         * @see #publishProgress
         * @see #doInBackground
         */
        @Override protected void onProgressUpdate(Integer... values) {
            // 主线程Handler实例消息
            Message message = DownloadImageToGalleryActivity.this.loadingHandler.obtainMessage();
            message.obj = values[0];
            message.what = HANDLER_LOADING;
            // 给主线程Handler发送消息
            DownloadImageToGalleryActivity.this.loadingHandler.handleMessage(message);
        }


        /**
         * 运行在主UI线程中，此时是预执行状态，下一步是doInBackground
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        @Override protected void onPreExecute() {
            super.onPreExecute();
        }


        /**
         * <p>Applications should preferably override {@link #onCancelled(Object)}.
         * This method is invoked by the default implementation of
         * {@link #onCancelled(Object)}.</p>
         * <p/>
         * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
         * {@link #doInBackground(Object[])} has finished.</p>
         *
         * @see #onCancelled(Object)
         * @see #cancel(boolean)
         * @see #isCancelled()
         */
        @Override protected void onCancelled() {
            /**
             * 设置按钮可用
             */
            DownloadImageToGalleryActivity.this.saveBT.setEnabled(true);
            super.onCancelled();
        }
    }
}
