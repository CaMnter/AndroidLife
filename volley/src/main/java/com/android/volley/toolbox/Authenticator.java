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

package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;

/**
 * An interface for interacting with auth tokens.
 */
/*
 * Authenticator 是一个身份认证接口
 * 用于基本认证或者摘要认证
 * Volley内，是用于和身份认证，比如 OAuth
 */
public interface Authenticator {
    /**
     * Synchronously retrieves an auth token.
     *
     * @throws AuthFailureError If authentication did not succeed
     */
    /*
     * 获取授权 token
     */
    public String getAuthToken() throws AuthFailureError;

    /**
     * Invalidates the provided auth token.
     */
    /*
     * 无效授权 token
     */
    public void invalidateAuthToken(String authToken);
}
