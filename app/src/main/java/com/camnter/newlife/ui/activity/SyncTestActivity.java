package com.camnter.newlife.ui.activity;

import android.os.Bundle;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;

/**
 * Description：SyncTestActivity
 * Created by：CaMnter
 */

public class SyncTestActivity extends BaseAppCompatActivity {
    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_sync_test;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {

    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        final Student test = new Student();
        Thread t1 = new Thread(new Runnable() {
            @Override public void run() {
                test.setNumber(6);
                System.out.println("t1:  test.number = "+test.number);
                try {
                    System.out.println("t1:  Thread.sleep(4000)  start");
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t1:  Thread.sleep(4000)  end");
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override public void run() {
                test.setNumber2(2);
                test.setName("23333");
                System.out.println("t2:  test.number = "+test.number);
                System.out.println("t2:  test.name = "+test.name);
                try {
                    System.out.println("t2:  Thread.sleep(3000)  start");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t2:  Thread.sleep(3000)  end");
            }
        });
        t1.start();
        t2.start();
    }

    public class Student {

        int number = 0;
        String name = "";


        public void setNumber(int n) {
            synchronized (name) {
                this.number = n;
            }
        }

        public void setNumber2(int n) {
            synchronized (name) {
                this.number = n;
            }
        }


        public synchronized void setName(String s) {
            synchronized (name) {
                this.name = s;
            }
        }
    }

}
