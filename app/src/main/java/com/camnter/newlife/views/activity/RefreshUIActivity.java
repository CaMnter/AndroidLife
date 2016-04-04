package com.camnter.newlife.views.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import java.lang.ref.WeakReference;

/**
 * Description：RefreshUIActivity
 * Created by：CaMnter
 * Time：2015-09-21 11:13
 */
public class RefreshUIActivity extends BaseAppCompatActivity {

    private TextView handlerTV;
    private static final int HANDLER_SUCCESS = 206;

    private static class RefreshHandler extends Handler {

        private final WeakReference<RefreshUIActivity> mActivity;


        public RefreshHandler(RefreshUIActivity activity) {
            mActivity = new WeakReference<>(activity);
        }


        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg msg
         */
        @Override public void handleMessage(Message msg) {
            RefreshUIActivity activity = this.mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case HANDLER_SUCCESS: {
                        activity.handlerTV.setText("Use: Handler.sendMessage");
                        break;
                    }
                }
            }
        }
    }

    private final RefreshHandler refreshHandler = new RefreshHandler(RefreshUIActivity.this);
    private final Runnable mRunnable = new Runnable() {
        @Override public void run() {
            Message message = RefreshUIActivity.this.refreshHandler.obtainMessage();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            message.what = HANDLER_SUCCESS;
            refreshHandler.sendMessageDelayed(message, 2000);
        }
    };
    private final Thread mThread = new Thread(mRunnable);

    private TextView asyncTaskTV;
    private MAsyncTask mAsyncTask;

    public class MAsyncTask extends AsyncTask<String, Integer, String> {

        private TextView textview;


        public MAsyncTask(TextView textview) {
            super();
            this.textview = textview;
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
            this.textview.setText("Use: AsyncTask " + value + "%");
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
            this.textview.setText("Use : AsyncTask 执行结束：" + s);
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

    private TextView runOnUiThreadTV;
    private final Runnable uiRunnable = new Runnable() {
        @Override public void run() {
            RefreshUIActivity.this.runOnUiThreadTV.setText("Use: runOnUiThread");
        }
    };

    private class MThread extends Thread {

        /**
         * Calls the <code>run()</code> method of the Runnable object the receiver
         * holds. If no Runnable is set, does nothing.
         *
         * @see Thread#start
         */
        @Override public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            RefreshUIActivity.this.runOnUiThread(RefreshUIActivity.this.uiRunnable);
        }
    }

    private final MThread runThread = new MThread();

    private TextView postHandlerTV;
    private final Runnable postRunnable = new Runnable() {
        @Override public void run() {
            RefreshUIActivity.this.postHandlerTV.setText("Use: Handler.post(...)");
        }
    };


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_refresh_ui;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.handlerTV = (TextView) this.findViewById(R.id.refresh_ui_handler_tv);
        this.asyncTaskTV = (TextView) this.findViewById(R.id.refresh_ui_asynctask_tv);
        this.runOnUiThreadTV = (TextView) this.findViewById(R.id.refresh_ui_run_on_ui_thread_tv);
        this.postHandlerTV = (TextView) this.findViewById(R.id.refresh_ui_post_tv);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    @Override protected void initData() {
        this.mThread.start();

        this.mAsyncTask = new MAsyncTask(this.asyncTaskTV);
        this.mAsyncTask.execute("%");

        this.runThread.start();

        Handler postHandler = new Handler();
        postHandler.post(this.postRunnable);
    }


    @Override protected void onDestroy() {
        this.mAsyncTask.onCancelled();
        super.onDestroy();
    }
}
