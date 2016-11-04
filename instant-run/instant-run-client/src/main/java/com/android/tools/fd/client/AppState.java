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

package com.android.tools.fd.client;

/**
 * The application state on device/emulator: the app is not running (or we cannot find it due to
 * connection problems etc), or it's in the foreground or background.
 */
public enum AppState {
    /** The app is not running (or we cannot find it due to connection problems etc) */
    NOT_RUNNING,
    /** The app is running an obsolete/older version of the runtime library */
    OBSOLETE,
    /** The app is actively running in the foreground */
    FOREGROUND,
    /** The app is running, but is not in the foreground */
    BACKGROUND
}
