package com.camnter.effective.test.thread.communication;

/**
 * Description：Message
 * Created by：CaMnter
 */

public class Message {
    String msg;
    CustomHandler h;


    public Message(String msg) {
        this.msg = msg;
    }


    public void setHandler(CustomHandler h) {
        this.h = h;
    }
}
