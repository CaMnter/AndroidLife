/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.tools.fd.runtime;

/**
 * App 的信息类
 *
 * Instant run 运行时，会生成一份这样的类，并且将 App 的相关信息写入进去
 *
 * 1. applicationId: xxx.xxx.xxx 的 packageName
 * 2. applicationClass:  项目自定义的 Application
 * 3. token:  用于检验 Socket 内的 DataInputStream
 * 4. usingApkSplits:  是否使用了 MultiDex
 */
public class AppInfo {
    // Keep the structure of this class in sync with
    // GenerateInstantRunAppInfoTask#writeAppInfoClass


    private AppInfo() {
    }


    /**
     * The application id of this app (e.g. the package name). Used to pick a unique
     * directory for the app's reloaded resources. (We can't look for it in the manifest,
     * since we need this information very early in the app life cycle, and we don't want
     * to call into the framework and cause more parts of it to be initialized before
     * we've monkey-patched the application class and resource loaders.)
     * <p>
     * (Not final: Will be replaced by byte-code manipulation at build time)
     */
    @SuppressWarnings({ "CanBeFinal", "StaticVariableNamingConvention" })
    public static String applicationId = null;

    /**
     * The fully qualified name of the real application to run. This is the user's app,
     * which has been hidden from the manifest during build time. Can be null if the
     * app does not have a custom application (in which case a default android.app.Application
     * is used.)
     * <p>
     */
    @SuppressWarnings({ "CanBeFinal", "StaticVariableNamingConvention" })
    public static String applicationClass = null;

    /**
     * A token assigned to this app at build time. This is used such that the running
     * app socket server can be reasonably sure that it's responding to requests from
     * the IDE.
     */
    @SuppressWarnings("StaticVariableNamingConvention")
    public static long token = 0L;

    /** Set when building on API 23 (or API 22 if multiapk is enabled) */
    public static boolean usingApkSplits = true;
}
