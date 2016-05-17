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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.android.volley.AuthFailureError;

/**
 * An Authenticator that uses {@link AccountManager} to get auth
 * tokens of a specified type for a specified account.
 */
/*
 * AndroidAuthenticator 实现了 Authenticator 接口
 * 基于 AccountManager，实现了认证交互
 */
public class AndroidAuthenticator implements Authenticator {
    // 用于进行 Android "账户" 的授权
    private final AccountManager mAccountManager;
    // AccountManager 需要的 "账户" Account
    private final Account mAccount;
    // token 的类型
    private final String mAuthTokenType;
    // 是否提示认证错误
    private final boolean mNotifyAuthFailure;


    /**
     * Creates a new authenticator.
     *
     * @param context Context for accessing AccountManager
     * @param account Account to authenticate as
     * @param authTokenType Auth token type passed to AccountManager
     */
    /*
     * 默认 authTokenType ＝ false，不提示认证错误
     */
    public AndroidAuthenticator(Context context, Account account, String authTokenType) {
        this(context, account, authTokenType, false);
    }


    /**
     * Creates a new authenticator.
     *
     * @param context Context for accessing AccountManager
     * @param account Account to authenticate as
     * @param authTokenType Auth token type passed to AccountManager
     * @param notifyAuthFailure Whether to raise a notification upon auth failure
     */
    /*
     * 默认 AccountManager = AccountManager.get(context)，通过 context 去获取一个 AccountManager
     */
    public AndroidAuthenticator(Context context, Account account, String authTokenType, boolean notifyAuthFailure) {
        this(AccountManager.get(context), account, authTokenType, notifyAuthFailure);
    }


    // Visible for testing. Allows injection of a mock AccountManager.
    AndroidAuthenticator(AccountManager accountManager, Account account, String authTokenType, boolean notifyAuthFailure) {
        mAccountManager = accountManager;
        mAccount = account;
        mAuthTokenType = authTokenType;
        mNotifyAuthFailure = notifyAuthFailure;
    }


    /**
     * Returns the Account being used by this authenticator.
     */
    /*
     * 获取 "账户" mAccount
     */
    public Account getAccount() {
        return mAccount;
    }


    /**
     * Returns the Auth Token Type used by this authenticator.
     */
    /*
     * 获取 token 类型
     */
    public String getAuthTokenType() {
        return mAuthTokenType;
    }


    /*
     * 调用 AccountManager 实现 getAuthToken 的逻辑
     */
    // TODO: Figure out what to do about notifyAuthFailure
    @SuppressWarnings("deprecation") @Override public String getAuthToken()
            throws AuthFailureError {
        /*
         * 先通过 Account + tokenType
         * 获取一个 AmsTask<Bundle> （ AccountManagerFuture<Bundle> 的实现类 ）
         */
        AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(mAccount, mAuthTokenType,
                mNotifyAuthFailure, null, null);
        Bundle result;
        try {
            // 从 AmsTask<Bundle> 抽出出 Bundle 数据
            result = future.getResult();
        } catch (Exception e) {
            throw new AuthFailureError("Error while retrieving auth token", e);
        }
        String authToken = null;
        // 判断 AmsTask<Bundle> 是否正常执行
        if (future.isDone() && !future.isCancelled()) {
            if (result.containsKey(AccountManager.KEY_INTENT)) {
                Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
                throw new AuthFailureError(intent);
            }
            // 拿到认证 token 数据
            authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
        }
        if (authToken == null) {
            throw new AuthFailureError("Got null auth token for type: " + mAuthTokenType);
        }

        return authToken;
    }


    /*
     * 调用 AccountManager 实现 invalidateAuthToken 的逻辑
     */
    @Override public void invalidateAuthToken(String authToken) {
        mAccountManager.invalidateAuthToken(mAccount.type, authToken);
    }
}
