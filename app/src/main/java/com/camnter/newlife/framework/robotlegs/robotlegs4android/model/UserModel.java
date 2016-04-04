package com.camnter.newlife.framework.robotlegs.robotlegs4android.model;

import com.camnter.newlife.framework.robotlegs.robotlegs4android.bean.User;
import com.camnter.newlife.framework.robotlegs.robotlegs4android.event.LoginEvent;
import com.camnter.robotlegs4android.mvcs.Actor;

/**
 * Description：UserModel
 * Created by：CaMnter
 * Time：2015-11-07 22:58
 */
public class UserModel extends Actor {

    public void login(String name, String password) {

        // TODO Do you want to network requests

        User user = new User();
        user.name = "CaMnter";
        user.sign = "Save you from anything";

        /*
         * you can send a your custom event from Model layer to View layer
         * 你可以发送一个你自定义的事件从Model层到View层
         */
        LoginEvent loginEvent = new LoginEvent(LoginEvent.USER_LOGIN_SUCCESS_FROM_MODEL_TO_VIEW);
        loginEvent.user = user;
        this.dispatch(loginEvent);
        this.getEventDispatcher()
            .dispatchEvent(new LoginEvent(LoginEvent.USER_LOGIN_SUCCESS_FROM_MODEL_TO_CONTROLLER));
    }


    public boolean logout() {

        // TODO Do you want to network requests

        return true;
    }
}
