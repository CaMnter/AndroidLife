/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.content.res;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * CompatibilityInfo class keeps the information about compatibility mode that the application is
 * running under.
 *
 * {@hide}
 *
 * @author CaMnter
 */

public class CompatibilityInfo implements Parcelable {

    /** default compatibility info object for compatible applications */
    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo() {
    };


    private CompatibilityInfo() {
        throw new RuntimeException("Stub!");
    }


    private CompatibilityInfo(Parcel source) {
        throw new RuntimeException("Stub!");
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }


    @Override
    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator<CompatibilityInfo> CREATOR
        = new Parcelable.Creator<CompatibilityInfo>() {
        @Override
        public CompatibilityInfo createFromParcel(Parcel source) {
            return new CompatibilityInfo(source);
        }


        @Override
        public CompatibilityInfo[] newArray(int size) {
            return new CompatibilityInfo[size];
        }
    };

}