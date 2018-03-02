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
 * Representation of a user on the device.
 *
 * @author CaMnter
 */

public final class UserHandle implements Parcelable {

    /** @hide */
    public static int getCallingUserId() {
        throw new RuntimeException("Stub!");
    }


    public static final Parcelable.Creator<UserHandle> CREATOR
        = new Parcelable.Creator<UserHandle>() {
        public UserHandle createFromParcel(Parcel in) {
            throw new RuntimeException("Stub!");
        }


        public UserHandle[] newArray(int size) {
            throw new RuntimeException("Stub!");
        }
    };


    public UserHandle(Parcel in) {
        throw new RuntimeException("Stub!");
    }


    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }

}