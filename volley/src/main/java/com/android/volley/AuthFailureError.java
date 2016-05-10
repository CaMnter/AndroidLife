/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley;

import android.content.Intent;

/**
 * Error indicating that there was an authentication failure when performing a Request.
 */

/* @formatter:off
 *
 * AuthFailureError 继承了 VolleyError
 * 表示：请求认证失败错误，如 RespondCode 的 401 和 403
 *
 * 实现了很多 基于 VolleyError 的构造方法
 *
 * 特殊的地方在于提供了一个 Intent mResolutionIntent
 * 比如 认证失败了，要通过这个 Intent 弹出一个密码对话框之类的事情
 */
@SuppressWarnings("serial")
public class AuthFailureError extends VolleyError {
    /** An intent that can be used to resolve this exception. (Brings up the password dialog.) */
    private Intent mResolutionIntent;

    public AuthFailureError() { }

    public AuthFailureError(Intent intent) {
        mResolutionIntent = intent;
    }

    public AuthFailureError(NetworkResponse response) {
        super(response);
    }

    public AuthFailureError(String message) {
        super(message);
    }

    public AuthFailureError(String message, Exception reason) {
        super(message, reason);
    }

    public Intent getResolutionIntent() {
        return mResolutionIntent;
    }

    @Override
    public String getMessage() {
        if (mResolutionIntent != null) {
            return "User needs to (re)enter credentials.";
        }
        return super.getMessage();
    }
}
