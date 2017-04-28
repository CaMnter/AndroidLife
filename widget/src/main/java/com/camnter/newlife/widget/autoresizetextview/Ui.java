package com.camnter.newlife.widget.autoresizetextview;

/*
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

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressWarnings("unchecked")
public class Ui {

    private static final int NORMAL = 0;
    private static final int BOLD = 1;
    private static final int ITALIC = 2;
    private static final int BLACK = 8;
    private static final int CONDENSED = 16;
    private static final int LIGHT = 32;
    private static final int MEDIUM = 64;
    private static final int THIN = 128;


    public static Bitmap createBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(canvas);

        return bitmap;
    }


    public static <T extends View> T findView(Activity activity, int resId) {
        return (T) activity.findViewById(resId);
    }


    public static <T extends View> T findView(View view, int resId) {
        return (T) view.findViewById(resId);
    }


    public static <T extends Fragment> T findFragment(FragmentActivity activity, int resId) {
        return (T) activity.getSupportFragmentManager().findFragmentById(resId);
    }


    public static <T extends Fragment> T findFragment(FragmentActivity activity, String tag) {
        return (T) activity.getSupportFragmentManager().findFragmentByTag(tag);
    }

    // Typefaces


    public static void setTypeface(TextView view, String path) {
        if (!view.isInEditMode()) {
            view.setTypeface(TypefaceUtils.getTypeface(view.getContext(), path));
        }
    }


    public static void setTypefaceByStyle(TextView view, int style) {
        switch (style) {
            case BLACK | ITALIC: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_BLACK_ITALIC);
                break;
            }
            case BLACK: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_BLACK);
                break;
            }
            case BOLD | CONDENSED | ITALIC: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_BOLD_CONDENSED_ITALIC);
                break;
            }
            case BOLD | CONDENSED: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_BOLD_CONDENSED);
                break;
            }
            case BOLD: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_BOLD);
                break;
            }
            case CONDENSED | ITALIC: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_CONDENSED_ITALIC);
                break;
            }
            case CONDENSED: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_CONDENSED);
                break;
            }
            case LIGHT | ITALIC: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_LIGHT_ITALIC);
                break;
            }
            case LIGHT: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_LIGHT);
                break;
            }
            case THIN | ITALIC: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_THIN_ITALIC);
                break;
            }
            case THIN: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_THIN);
                break;
            }
            case MEDIUM | ITALIC: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_MEDIUM_ITALIC);
                break;
            }
            case MEDIUM: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_MEDIUM);
                break;
            }
            case ITALIC: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_ITALIC);
                break;
            }
            case NORMAL: {
                TypefaceUtils.loadTypeface(view, TypefaceUtils.ROBOTO_REGULAR);
                break;
            }
        }
    }

    // Measuring


    public static int[] measureRatio(int widthMeasureSpec, int heightMeasureSpec, double aspectRatio) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = widthMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec
            .getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = heightMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec
            .getSize(heightMeasureSpec);

        int measuredWidth;
        int measuredHeight;

        if (heightMode == MeasureSpec.EXACTLY && widthMode == MeasureSpec.EXACTLY) {
            measuredWidth = widthSize;
            measuredHeight = heightSize;

        } else if (heightMode == MeasureSpec.EXACTLY) {
            measuredWidth = (int) Math.min(widthSize, heightSize * aspectRatio);
            measuredHeight = (int) (measuredWidth / aspectRatio);

        } else if (widthMode == MeasureSpec.EXACTLY) {
            measuredHeight = (int) Math.min(heightSize, widthSize / aspectRatio);
            measuredWidth = (int) (measuredHeight * aspectRatio);

        } else {
            if (widthSize > heightSize * aspectRatio) {
                measuredHeight = heightSize;
                measuredWidth = (int) (measuredHeight * aspectRatio);
            } else {
                measuredWidth = widthSize;
                measuredHeight = (int) (measuredWidth / aspectRatio);
            }

        }

        return new int[] { measuredWidth, measuredHeight };
    }

    // Spinners


    public static void setSelection(Spinner spinner, Object selection) {
        setSelection(spinner, selection.toString());
    }


    public static void setSelection(Spinner spinner, String selection) {
        final int count = spinner.getCount();
        for (int i = 0; i < count; i++) {
            String item = spinner.getItemAtPosition(i).toString();
            if (item.equalsIgnoreCase(selection)) {
                spinner.setSelection(i);
            }
        }
    }

    // Keyboard


    public static void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static void showSoftkeyboard(View view) {
        showSoftkeyboard(view, null);
    }


    public static void showSoftkeyboard(View view, ResultReceiver resultReceiver) {
        Configuration config = view.getContext().getResources().getConfiguration();
        if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);

            if (resultReceiver != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT, resultReceiver);
            } else {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

}