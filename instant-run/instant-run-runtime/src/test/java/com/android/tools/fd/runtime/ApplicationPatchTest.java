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

import com.android.tools.fd.client.ApplicationPatchUtil;
import com.android.tools.fd.client.UpdateMode;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.Expect;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class ApplicationPatchTest {

    @Parameterized.Parameters(name="{0}")
    public static Collection<Object[]> getPatches() {
        ApplicationPatch emptyData = new ApplicationPatch("path", new byte[] {});
        ApplicationPatch patch1 = new ApplicationPatch("path1", new byte[] {54, 23, -128, 4, 127, -5});
        ApplicationPatch patch2 = new ApplicationPatch("patch2", new byte[] {122, -2, 73});

        return Arrays.asList(new Object[][] {
                {ImmutableList.of()},
                {ImmutableList.of(emptyData)},
                {ImmutableList.of(patch1)},
                {ImmutableList.of(patch1, patch2)},
        });
    }

    private final List<ApplicationPatch> mPatches;

    public ApplicationPatchTest(List<ApplicationPatch> patches) {
        mPatches = patches;
    }

    @Rule
    public Expect mExpect = Expect.createAndEnableStackTrace();

    @Test
    public void checkApplicationPatchReadWrite() throws IOException {
        PipedInputStream input = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(input);
        try {
            DataOutputStream output = new DataOutputStream(outputStream);
            ApplicationPatchUtil.write(output, mPatches, UpdateMode.HOT_SWAP);

            List<ApplicationPatch> patches = ApplicationPatch.read(new DataInputStream(input));
            assertNotNull(patches);
            assertEquals("Should not lose or gain patches", mPatches.size(), patches.size());

            for (int i = 0; i < mPatches.size(); i++) {
                ApplicationPatch expected = mPatches.get(i);
                ApplicationPatch actual = patches.get(i);
                mExpect.that(actual.getBytes()).isEqualTo(expected.getBytes());
                mExpect.that(actual.getPath()).isEqualTo(expected.getPath());
            }
        } finally {
            outputStream.close();
        }
    }
}
