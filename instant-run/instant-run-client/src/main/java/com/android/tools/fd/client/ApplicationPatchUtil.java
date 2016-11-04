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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.fd.runtime.ApplicationPatch;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Application patch methods only needed by the client.
 */
public class ApplicationPatchUtil {

    public static void write(
        @NonNull DataOutputStream output,
        @Nullable List<ApplicationPatch> changes,
        @NonNull UpdateMode updateMode)
        throws IOException {
        if (changes == null) {
            output.writeInt(0);
        } else {
            output.writeInt(changes.size());
            for (ApplicationPatch change : changes) {
                write(output, change);
            }
        }
        output.writeInt(updateMode.getId());
    }


    private static void write(@NonNull DataOutputStream output, @NonNull ApplicationPatch change)
        throws IOException {
        output.writeUTF(change.path);
        byte[] bytes = change.data;
        output.writeInt(bytes.length);
        output.write(bytes);
    }
}
