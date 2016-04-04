package com.camnter.newlife.framework.robotlegs.robotlegs4android.event;

import com.camnter.newlife.framework.robotlegs.robotlegs4android.bean.User;
import com.camnter.robotlegs4android.base.Event;

/**
 * Description：LoginEvent
 * Created by：CaMnter
 * Time：2015-11-07 23:05
 */
public class LoginEvent extends Event {

    public static final String USER_LOGIN = "user_login";
    public static final String USER_LOGOUT = "user_logout";

    public static final String USER_LOGIN_SUCCESS_FROM_MODEL_TO_CONTROLLER
            = "user_login_success_from_model_to_controller";
    public static final String USER_LOGIN_SUCCESS_FROM_MODEL_TO_VIEW
            = "user_login_success_from_model_to_view";
    public static final String USER_LOGIN_SUCCESS_FROM_CONTROLLER_TO_VIEW
            = "user_login_success_from_controller_to_view";

    public String name;
    public String password;

    public User user;


    public LoginEvent(String type) {
        super(type);
    }
}
