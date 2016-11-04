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

package com.android.tools.fd.common;

/**
 * Constants shared between Android Studio and the Instant Run runtime.
 */
public interface ProtocolConstants {

    /**
     * Magic (random) number used to identify the protocol
     */
    long PROTOCOL_IDENTIFIER = 0x35107124L;

    /**
     * Version of the protocol
     */
    int PROTOCOL_VERSION = 4;

    /**
     * Message: sending patches
     */
    int MESSAGE_PATCHES = 1;

    /**
     * Message: ping, send ack back
     */
    int MESSAGE_PING = 2;

    /**
     * Message: look up a very quick checksum of the given path; this
     * may not pick up on edits in the middle of the file but should be a
     * quick way to determine if a path exists and some basic information
     * about it.
     */
    int MESSAGE_PATH_EXISTS = 3;

    /**
     * Message: query whether the app has a given file and if so return
     * its checksum. (This is used to determine whether the app can receive
     * a small delta on top of a (typically resource ) file instead of resending the whole
     * file over again.)
     */
    int MESSAGE_PATH_CHECKSUM = 4;

    /**
     * Message: restart activities
     */
    int MESSAGE_RESTART_ACTIVITY = 5;

    /**
     * Message: show toast
     */
    int MESSAGE_SHOW_TOAST = 6;

    /**
     * Done transmitting
     */
    int MESSAGE_EOF = 7;

    /**
     * No updates
     */
    int UPDATE_MODE_NONE = 0;

    /**
     * Patch changes directly, keep app running without any restarting
     */
    int UPDATE_MODE_HOT_SWAP = 1;

    /**
     * Patch changes, restart activity to reflect changes
     */
    int UPDATE_MODE_WARM_SWAP = 2;

    /**
     * Store change in app directory, restart app
     */
    int UPDATE_MODE_COLD_SWAP = 3;
}
