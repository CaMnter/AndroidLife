package com.camnter.newlife.component.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Description：DownloadService
 * Created by：CaMnter
 * Time：2015-11-16 14:50
 */
public class DownloadService extends Service {

    private static final String TAG = "DownloadService";
    private IBinder binder;


    @Nullable @Override public IBinder onBind(Intent intent) {
        return this.binder;
    }


    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override public void onCreate() {
        super.onCreate();
        this.binder = new DownloadServiceBinder();
    }


    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     * The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Service Binder
     */
    public class DownloadServiceBinder extends Binder {
        public IBinderView iBinderView;


        public DownloadService getService() {
            return DownloadService.this;
        }
    }


    public void startDownload(String imageUrl) {
        ((DownloadServiceBinder) DownloadService.this.binder).iBinderView.downloadStart();
        new DownloadImageAsyncTask(this).execute(imageUrl);
    }


    /**
     * 下载图片异步任务
     */
    public class DownloadImageAsyncTask extends AsyncTask<String, Integer, String> {

        private Service service;
        private String localFilePath;


        public DownloadImageAsyncTask(Service service) {
            super();
            this.service = service;
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
                File cacheDir = this.service.getCacheDir();
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
            super.onPostExecute(string);
            ((DownloadServiceBinder) DownloadService.this.binder).iBinderView.downloadSuccess(
                    this.localFilePath);
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
            super.onProgressUpdate(values);
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
            super.onCancelled();
            ((DownloadServiceBinder) DownloadService.this.binder).iBinderView.downloadFailure();
        }
    }
}
