package com.camnter.newlife.widget.autoresizetextview;/*
 * Copyright (C) 2010 Michael Pardo
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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import com.camnter.newlife.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class Log {

    private static final int DUMP_LENGTH = 4000;
    public static final int LEVEL_VERBOSE = 0;
    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_INFO = 2;
    public static final int LEVEL_WARNING = 3;
    public static final int LEVEL_ERROR = 4;
    public static final int LEVEL_NONE = 5;

    private static String mTag = "AndroidUtils";
    private static boolean mEnabled = false;

    private static String mRemoteUrl;
    private static String mPackageName;
    private static String mPackageVersion;


    public static void initialize(Context context) {
        initialize(context, null, null, BuildConfig.DEBUG);
    }


    public static void initialize(Context context, String tag, String url, boolean enabled) {
        if (tag != null) {
            mTag = tag;
        }

        mEnabled = enabled;
        mRemoteUrl = url;

        if (context != null) {
            try {
                PackageInfo pi = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
                mPackageName = pi.packageName;
                mPackageVersion = pi.versionName;

                if (tag == null && pi.applicationInfo.labelRes > 0) {
                    mTag = context.getString(pi.applicationInfo.labelRes);
                }
            } catch (NameNotFoundException e) {
            }
        }
    }


    public static int v(String msg) {
        if (mEnabled) {
            return android.util.Log.v(mTag, msg);
        }
        return 0;
    }


    public static int v(String tag, String msg) {
        if (mEnabled) {
            return android.util.Log.v(tag, msg);
        }
        return 0;
    }


    public static int v(String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.v(mTag, msg, tr);
        }
        return 0;
    }


    public static int v(String tag, String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.v(tag, msg, tr);
        }
        return 0;
    }


    public static int d(String msg) {
        if (mEnabled) {
            return android.util.Log.d(mTag, msg);
        }
        return 0;
    }


    public static int d(String tag, String msg) {
        if (mEnabled) {
            return android.util.Log.d(tag, msg);
        }
        return 0;
    }


    public static int d(String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.d(mTag, msg, tr);
        }
        return 0;
    }


    public static int d(String tag, String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.d(tag, msg, tr);
        }
        return 0;
    }


    public static int i(String msg) {
        if (mEnabled) {
            return android.util.Log.i(mTag, msg);
        }
        return 0;
    }


    public static int i(String tag, String msg) {
        if (mEnabled) {
            return android.util.Log.i(tag, msg);
        }
        return 0;
    }


    public static int i(String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.i(mTag, msg, tr);
        }
        return 0;
    }


    public static int i(String tag, String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.i(tag, msg, tr);
        }
        return 0;
    }


    public static int w(String msg) {
        if (mEnabled) {
            return android.util.Log.w(mTag, msg);
        }
        return 0;
    }


    public static int w(String tag, String msg) {
        if (mEnabled) {
            return android.util.Log.w(tag, msg);
        }
        return 0;
    }


    public static int w(String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.w(mTag, msg, tr);
        }
        return 0;
    }


    public static int w(String tag, String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.w(tag, msg, tr);
        }
        return 0;
    }


    public static int e(String msg) {
        if (mEnabled) {
            return android.util.Log.e(mTag, msg);
        }
        return 0;
    }


    public static int e(String tag, String msg) {
        if (mEnabled) {
            return android.util.Log.e(tag, msg);
        }
        return 0;
    }


    public static int e(String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.e(mTag, msg, tr);
        }
        return 0;
    }


    public static int e(String tag, String msg, Throwable tr) {
        if (mEnabled) {
            return android.util.Log.e(tag, msg, tr);
        }
        return 0;
    }


    public static int t(String msg, Object... args) {
        if (mEnabled) {
            return android.util.Log.v("test", String.format(msg, args));
        }
        return 0;
    }


    public static void remote(final String msg) {
        if (mRemoteUrl == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(mRemoteUrl);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("package_name", mPackageName));
                    params.add(new BasicNameValuePair("package_version", mPackageVersion));
                    params.add(new BasicNameValuePair("phone_model", Build.MODEL));
                    params.add(new BasicNameValuePair("sdk_version", Build.VERSION.RELEASE));
                    params.add(new BasicNameValuePair("message", msg));

                    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    httpClient.execute(httpPost);
                } catch (Exception e) {
                }
            }
        }).start();
    }


    public static void dump(String longMsg) {
        dump(mTag, longMsg, LEVEL_INFO);
    }


    public static void dump(String longMsg, int level) {
        dump(mTag, longMsg, level);
    }


    public static void dump(String tag, String longMsg) {
        dump(tag, longMsg, LEVEL_INFO);
    }


    public static void dump(String tag, String longMsg, int level) {
        int len = longMsg.length();
        String curr;
        for (int a = 0; a < len; a += DUMP_LENGTH) {
            if (a + DUMP_LENGTH < len) {
                curr = longMsg.substring(a, a + DUMP_LENGTH);
            } else {
                curr = longMsg.substring(a);
            }

            switch (level) {
                case LEVEL_ERROR:
                    Log.e(tag, curr);
                    break;
                case LEVEL_WARNING:
                    Log.w(tag, curr);
                    break;
                case LEVEL_INFO:
                    Log.i(tag, curr);
                    break;
                case LEVEL_DEBUG:
                    Log.d(tag, curr);
                    break;
                case LEVEL_VERBOSE:
                default:
                    Log.v(tag, curr);
                    break;
            }
        }
    }

}