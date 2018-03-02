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

package android.os;

/**
 * @author CaMnter
 * @hide
 */

public final class UserId {

    /**
     * Range of IDs allocated for a user.
     *
     * @hide
     */
    public static final int PER_USER_RANGE = 100000;
    public static final int USER_ALL = -1;
    /**
     * Enable multi-user related side effects. Set this to false if there are problems with single
     * user usecases.
     */
    public static final boolean MU_ENABLED = true;


    /**
     * Returns the user id for a given uid.
     *
     * @hide
     */
    public static final int getUserId(int uid) {
        if (MU_ENABLED) {
            return uid / PER_USER_RANGE;
        } else {
            return 0;
        }
    }


    public static final int getCallingUserId() {
        return getUserId(Binder.getCallingUid());
    }

}