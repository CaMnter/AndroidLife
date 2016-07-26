package com.camnter.effective.test;

/**
 * Description：SyncTest
 * Created by：CaMnter
 */

class SyncTest {

    public static void main(String[] args) {
        Student test = new Student();
        Thread t1 = new Thread(new Runnable() {
            @Override public void run() {
                test.setNumber(6);
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override public void run() {
                test.setNumber2(2);
            }
        });
        t1.start();
        t2.start();
    }

}
