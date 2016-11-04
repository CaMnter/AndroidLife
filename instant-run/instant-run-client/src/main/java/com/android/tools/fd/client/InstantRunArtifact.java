/*
 * Copyright (C) 2016 The Android Open Source Project
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
import com.google.common.base.MoreObjects;
import java.io.File;

public class InstantRunArtifact {

    @NonNull
    public final InstantRunArtifactType type;

    @NonNull
    public final File file;

    @NonNull
    public final String timestamp;


    public InstantRunArtifact(
        @NonNull InstantRunArtifactType type, @NonNull File file, @NonNull String timestamp) {
        this.type = type;
        this.file = file;
        this.timestamp = timestamp;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(InstantRunArtifact.class)
            .add("type", type)
            .add("file", file)
            .add("timestamp", timestamp)
            .toString();
    }
}
