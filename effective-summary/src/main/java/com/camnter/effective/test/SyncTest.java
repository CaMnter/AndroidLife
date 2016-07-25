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
                System.out.println("t2:  test.number = "+test.number);
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

}
