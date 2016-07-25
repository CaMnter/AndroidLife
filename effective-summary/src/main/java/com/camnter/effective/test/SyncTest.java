package com.camnter.effective.test;

/**
 * Description：SynchonizedTest
 * Created by：CaMnter
 */

class SyncTest {

    public static void main(String[] args) {
        Student test = new Student();
        Thread t1 = new Thread(new Runnable() {
            @Override public void run() {
                System.out.println("t1:  test.setNumber(6)");
                test.setNumber(6);
                try {
                    System.out.println("t1:  Thread.sleep(3000)");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override public void run() {
                System.out.println("t2:  test.setName(\"2333\")");
                test.setName("2333");
            }
        });
        t1.start();
        t2.start();
    }

}
