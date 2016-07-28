package com.camnter.test.thread.communication;

/**
 * Description：Handler
 * Created by：CaMnter
 */

public class CustomHandler implements ThreadCallback {

    private final CustomThread customThread;


    public CustomHandler(CustomThread customThread) {this.customThread = customThread;}


    @Override public void callback(String what) {
        System.out.println(
            "Thread(" + Thread.currentThread().getName() + ")  #  dispatch  #  " + what);
    }


    public void sendMessage(Message e) {
        this.customThread.getMessages().add(e);
    }

}
