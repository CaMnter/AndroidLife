/*
 * Copyright (C) 2017 Beijing Didi Infinity Technology and Development Co.,Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.didi.virtualapk.internal;

import android.content.pm.ActivityInfo;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.Log;
import java.util.HashMap;

/**
 * 插桩 Activity 信息
 *
 * 用于分配 插件 Activity 启动时，所用到的 插桩 Activity 信息
 *
 * -------------------------------------------------------------------------------------------------
 *
 * standard
 * 最大 standard Activity 数量是 1
 *
 * singleTop
 * 最大 singleTop Activity 数量是 8
 *
 * singleTask
 * 最大 singleTask Activity 数量是 8
 *
 * singleInstance
 * 最大 singleInstance Activity 数量是 8
 *
 * -------------------------------------------------------------------------------------------------
 *
 * 规定了 插桩 Activity 的命名
 *
 * standard
 * com.didi.virtualapk.core.A$1 - com.didi.virtualapk.core.A$2
 *
 * singleTop
 * com.didi.virtualapk.core.B$0 - com.didi.virtualapk.core.B$7
 *
 * singleTask
 * com.didi.virtualapk.core.C$0 - com.didi.virtualapk.core.C$7
 *
 * singleInstance
 * com.didi.virtualapk.core.D$0 - com.didi.virtualapk.core.D$7
 *
 * -------------------------------------------------------------------------------------------------
 *
 * Created by renyugang on 16/8/15.
 */
class StubActivityInfo {

    /**
     * standard
     * 最大 standard Activity 数量是 1
     *
     * singleTop
     * 最大 singleTop Activity 数量是 8
     *
     * singleTask
     * 最大 singleTask Activity 数量是 8
     *
     * singleInstance
     * 最大 singleInstance Activity 数量是 8
     */
    public static final int MAX_COUNT_STANDARD = 1;
    public static final int MAX_COUNT_SINGLETOP = 8;
    public static final int MAX_COUNT_SINGLETASK = 8;
    public static final int MAX_COUNT_SINGLEINSTANCE = 8;

    /**
     * 规定了 插桩 Activity 的命名
     *
     * standard
     * com.didi.virtualapk.core.A$1 - com.didi.virtualapk.core.A$2
     *
     * singleTop
     * com.didi.virtualapk.core.B$0 - com.didi.virtualapk.core.B$7
     *
     * singleTask
     * com.didi.virtualapk.core.C$0 - com.didi.virtualapk.core.C$7
     *
     * singleInstance
     * com.didi.virtualapk.core.D$0 - com.didi.virtualapk.core.D$7
     */
    public static final String corePackage = "com.didi.virtualapk.core";
    public static final String STUB_ACTIVITY_STANDARD = "%s.A$%d";
    public static final String STUB_ACTIVITY_SINGLETOP = "%s.B$%d";
    public static final String STUB_ACTIVITY_SINGLETASK = "%s.C$%d";
    public static final String STUB_ACTIVITY_SINGLEINSTANCE = "%s.D$%d";

    /**
     * 记录 几种模式的 插桩 Activity 序号
     * 序号范围在 0 - 7 之间
     */
    public final int usedStandardStubActivity = 1;
    public int usedSingleTopStubActivity = 0;
    public int usedSingleTaskStubActivity = 0;
    public int usedSingleInstanceStubActivity = 0;

    private HashMap<String, String> mCachedStubActivity = new HashMap<>();


    /**
     * 获取对应的 插桩 Activity name
     *
     * 1. 根据 插件 Activity class name，去获取对应 插桩 Activity class full name 缓存
     * 2. 没有缓存的话，约束一个 插桩 Activity class full name，对应这个 插件 Activity class name
     * -  然后放入缓存
     * 3. 由于用了 求余方式 ，所以 几种模式的 插桩 Activity 序号范围在 0 - 7 之间
     * 4. 根据主题是否是半透明，会在 standard 模式，多加一个 序号 2
     *
     * @param className className
     * @param launchMode launchMode
     * @param theme theme
     * @return String
     */
    public String getStubActivity(String className, int launchMode, Theme theme) {
        String stubActivity = mCachedStubActivity.get(className);
        if (stubActivity != null) {
            return stubActivity;
        }

        TypedArray array = theme.obtainStyledAttributes(new int[] {
            android.R.attr.windowIsTranslucent,
            android.R.attr.windowBackground
        });
        boolean windowIsTranslucent = array.getBoolean(0, false);
        array.recycle();
        if (Constants.DEBUG) {
            Log.d("StubActivityInfo",
                "getStubActivity, is transparent theme ? " + windowIsTranslucent);
        }
        stubActivity = String.format(STUB_ACTIVITY_STANDARD, corePackage, usedStandardStubActivity);
        switch (launchMode) {
            case ActivityInfo.LAUNCH_MULTIPLE: {
                stubActivity = String.format(STUB_ACTIVITY_STANDARD, corePackage,
                    usedStandardStubActivity);
                if (windowIsTranslucent) {
                    stubActivity = String.format(STUB_ACTIVITY_STANDARD, corePackage, 2);
                }
                break;
            }
            case ActivityInfo.LAUNCH_SINGLE_TOP: {
                usedSingleTopStubActivity = usedSingleTopStubActivity % MAX_COUNT_SINGLETOP + 1;
                stubActivity = String.format(STUB_ACTIVITY_SINGLETOP, corePackage,
                    usedSingleTopStubActivity);
                break;
            }
            case ActivityInfo.LAUNCH_SINGLE_TASK: {
                usedSingleTaskStubActivity = usedSingleTaskStubActivity % MAX_COUNT_SINGLETASK + 1;
                stubActivity = String.format(STUB_ACTIVITY_SINGLETASK, corePackage,
                    usedSingleTaskStubActivity);
                break;
            }
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE: {
                usedSingleInstanceStubActivity =
                    usedSingleInstanceStubActivity % MAX_COUNT_SINGLEINSTANCE + 1;
                stubActivity = String.format(STUB_ACTIVITY_SINGLEINSTANCE, corePackage,
                    usedSingleInstanceStubActivity);
                break;
            }

            default:
                break;
        }

        mCachedStubActivity.put(className, stubActivity);
        return stubActivity;
    }

}
