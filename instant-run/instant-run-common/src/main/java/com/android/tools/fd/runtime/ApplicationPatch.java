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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.android.tools.fd.common.Log.logging;

// This class is used in both the Android runtime and in the IDE.
// Technically we only need the write protocol on the IDE side and the
// read protocol on the Android app size, but keeping it all together and
// in sync right now.
public class ApplicationPatch {
    @NonNull public final String path;
    @NonNull public final byte[] data;


    public ApplicationPatch(@NonNull String path, @NonNull byte[] data) {
        this.path = path;
        this.data = data;
    }


    @Override
    public String toString() {
        return "ApplicationPatch{" +
            "path='" + path + '\'' +
            ", data.length='" + data.length + '\'' +
            '}';
    }


    // Only needed on the Android side
    @Nullable
    public static List<ApplicationPatch> read(@NonNull DataInputStream input) throws IOException {
        int changeCount = input.readInt();

        if (logging != null && logging.isLoggable(Level.FINE)) {
            logging.log(Level.FINE, "Receiving " + changeCount + " changes");
        }

        List<ApplicationPatch> changes = new ArrayList<ApplicationPatch>(changeCount);
        for (int i = 0; i < changeCount; i++) {
            String path = input.readUTF();
            int size = input.readInt();
            byte[] bytes = new byte[size];
            input.readFully(bytes);
            changes.add(new ApplicationPatch(path, bytes));
        }

        return changes;
    }


    @NonNull
    public String getPath() {
        return path;
    }


    @NonNull
    public byte[] getBytes() {
        return data;
    }
}
