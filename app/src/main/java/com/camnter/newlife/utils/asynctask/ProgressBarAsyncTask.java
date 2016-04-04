package com.camnter.newlife.utils.asynctask;

import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Description：ProgressBarAsyncTask
 * Created by：CaMnter
 * Time：2015-09-17 14:19
 */
public class ProgressBarAsyncTask extends AsyncTask<String, Integer, String> {

    private TextView textview;
    private ProgressBar progressBar;


    public ProgressBarAsyncTask(ProgressBar progressBar, TextView textview) {
        super();
        this.textview = textview;
        this.progressBar = progressBar;
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
        int i = 0;
        for (; i < 100; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.publishProgress(i);
        }
        return i + params[0];
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
        int value = values[0];
        this.progressBar.setProgress(value);
        this.textview.setText(value + "%");
    }


    /**
     * 对应AsyncTask第三个参数 (接受doInBackground的返回值)
     * 在doInBackground方法执行结束之后在运行，此时已经回来主UI线程当中 能对UI控件进行修改
     *
     * @param s The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override protected void onPostExecute(String s) {
        this.textview.setText("执行结束：" + s);
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
    }
}
