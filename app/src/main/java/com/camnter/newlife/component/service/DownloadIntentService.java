package com.camnter.newlife.component.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.camnter.newlife.views.activity.DownloadReceiverActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DOWNLOAD = "com.camnter.newlife.service.action.download";

    // TODO: Rename parameters
    private static final String IMAGE_URL = "com.camnter.newlife.service.extra.image.url";


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionDownload(Context context, String url) {
        Intent intent = new Intent(context, DownloadIntentService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(IMAGE_URL, url);
        context.startService(intent);
    }


    public DownloadIntentService() {
        super("DownIntentService");
    }


    @Override protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                final String url = intent.getStringExtra(IMAGE_URL);
                this.handleActionDownload(url);
            }
        }
    }


    /**
     * Handle action Download in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDownload(String url) {
        // TODO: Handle action Download
        new DownloadImageAsyncTask(this).execute(url);
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
            Intent intent = new Intent(DownloadReceiverActivity.DownloadReceiver.INTENT_ACTION);
            intent.putExtra(DownloadReceiverActivity.DownloadReceiver.INTENT_TYPE,
                    DownloadReceiverActivity.DownloadReceiver.TYPE_DOWNLOAD_SUCCESS);
            intent.putExtra(DownloadReceiverActivity.DownloadReceiver.INTENT_DATA_IMAGE_PATH,
                    this.localFilePath);
            DownloadIntentService.this.sendBroadcast(intent);
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
            Intent intent = new Intent(DownloadReceiverActivity.DownloadReceiver.INTENT_ACTION);
            intent.putExtra(DownloadReceiverActivity.DownloadReceiver.INTENT_TYPE,
                    DownloadReceiverActivity.DownloadReceiver.TYPE_DOWNLOAD_FAILURE);
            DownloadIntentService.this.sendBroadcast(intent);
        }
    }
}
