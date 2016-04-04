package com.camnter.newlife.framework.rxandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.ImageUtil;
import com.camnter.newlife.utils.ThreadUtils;
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
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Description：RxAsyncActivity
 * Created by：CaMnter
 * Time：2015-12-01 15:49
 */
public class RxAsyncActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RxAsyncActivity";
    private static final String OBJECT_IMAGE_URL = "http://img.blog.csdn.net/20150913233900119";

    private static final int HANDLER_LOADING = 262;

    private ImageView asyncRxOneIV;
    private ImageView asyncRxTwoIV;
    private Button asyncRxSaveBT;

    private CustomProgressBarDialog dialog;

    /**
     * 刷新Dialog显示的进度
     */
    private static class LoadingHandler extends Handler {
        private final WeakReference<RxAsyncActivity> mActivity;


        public LoadingHandler(RxAsyncActivity activity) {
            mActivity = new WeakReference<>(activity);
        }


        /**
         * Subclasses must implement this to receive messages.
         */
        @Override public void handleMessage(Message msg) {
            final RxAsyncActivity activity = this.mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case HANDLER_LOADING: {
                        final int progressValue = (int) msg.obj;
                        activity.runOnUiThread(new Runnable() {
                            @Override public void run() {
                                try {
                                    activity.dialog.setLoadPrompt(progressValue + "%");
                                    activity.dialog.show();
                                } catch (Exception ignored) {
                                }
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    private final LoadingHandler loadingHandler = new LoadingHandler(RxAsyncActivity.this);


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_rx_async;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        TextView syncRxSaveTV = (TextView) this.findViewById(R.id.rx_async_save_tv);
        syncRxSaveTV.setText(OBJECT_IMAGE_URL);
        this.asyncRxOneIV = (ImageView) this.findViewById(R.id.rx_async_one_iv);
        this.asyncRxTwoIV = (ImageView) this.findViewById(R.id.rx_async_two_iv);
        this.asyncRxSaveBT = (Button) this.findViewById(R.id.rx_async_save_bt);
        this.dialog = new CustomProgressBarDialog(this);
    }


    @Override protected void initListeners() {
        this.asyncRxSaveBT.setOnClickListener(this);
    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {

    }


    /**
     * 下载图片异步任务
     */
    public class DownloadImageAsyncTask extends AsyncTask<String, Integer, String> {

        private Activity activity;
        private String localFilePath;
        private Subscriber<? super String> subscriber;


        public DownloadImageAsyncTask(Activity activity, Subscriber<? super String> subscriber) {
            super();
            this.activity = activity;
            this.subscriber = subscriber;
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
             * 通知订阅者
             */
            subscriber.onNext(localFilePath);
            subscriber.onCompleted();
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
            Message message = RxAsyncActivity.this.loadingHandler.obtainMessage();
            message.obj = values[0];
            message.what = HANDLER_LOADING;
            // 给主线程Handler发送消息
            RxAsyncActivity.this.loadingHandler.handleMessage(message);
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
            RxAsyncActivity.this.asyncRxSaveBT.setEnabled(true);
            super.onCancelled();
        }
    }


    /**
     * 检查线程
     */
    private void checkThread(String info) {
        Log.i(TAG, ThreadUtils.getThreadMsg(info));
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rx_async_save_bt:
                /**
                 * create添加OnSubscribe对象（观察者）
                 * 此时要实现OnSubscribe（观察者）的call方法，如果是异步的话，这里的call就应该写后台的I/O操作
                 * 当你调用了subscribe方法时，就会自动调用OnSubscribe对象（观察者）的call方法去执行
                 * （可以参考subscribe方法的源码）
                 * 失败会走到onError方法
                 * 成功的话，因为call方法有Subscriber对象，这是添加的订阅者，可以调用它的onNext或onCompleted
                 *
                 *
                 * subscribeOn(Schedulers.io()) 表示观察者做子线程I/O操作
                 * observeOn(AndroidSchedulers.mainThread()) 表示订阅者做主线程操作
                 */
                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override public void call(Subscriber<? super String> subscriber) {
                        RxAsyncActivity.this.checkThread("create -> OnSubscribe.create()");
                        new RxAsyncActivity.DownloadImageAsyncTask(RxAsyncActivity.this, subscriber)
                                .execute(OBJECT_IMAGE_URL);
                    }
                })
                          .subscribeOn(Schedulers.io())
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe(new Subscriber<String>() {
                              @Override public void onCompleted() {

                              }


                              @Override public void onError(Throwable e) {

                              }


                              @Override public void onNext(String s) {
                                  RxAsyncActivity.this.checkThread("create -> Subscriber.onNext()");
                                  /**
                                   * 设置按钮可用，并隐藏Dialog
                                   */
                                  RxAsyncActivity.this.asyncRxSaveBT.setEnabled(true);
                                  RxAsyncActivity.this.dialog.hide();

                                  DisplayMetrics metrics = new DisplayMetrics();
                                  getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                  int screenWidth = metrics.widthPixels;
                                  int screenHeight = metrics.heightPixels;
                                  /**
                                   * ImageUtil.decodeScaleImage 解析图片
                                   */
                                  Bitmap bitmap = ImageUtil.decodeScaleImage(s, screenWidth,
                                          screenHeight);
                                  RxAsyncActivity.this.asyncRxOneIV.setImageBitmap(bitmap);
                                  RxAsyncActivity.this.asyncRxTwoIV.setImageBitmap(bitmap);
                              }
                          });
                break;
        }
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        this.dialog.dismiss();
    }
}
