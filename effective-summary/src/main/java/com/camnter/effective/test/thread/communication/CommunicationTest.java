package com.camnter.effective.test.thread.communication;

/**
 * Description：CommunicationTest
 * Created by：CaMnter
 */

public class CommunicationTest {
    public static void main(String[] args) {
        final CustomHandler[] h2 = new CustomHandler[1];
        CustomThread t2 = new CustomThread() {
            @Override public void run() {
                h2[0] = new CustomHandler(this);
                Message msg = new Message("h2 send");
                msg.h = h2[0];
                h2[0].sendMessage(msg);
                super.run();
            }
        };
        t2.start();
    }
}
