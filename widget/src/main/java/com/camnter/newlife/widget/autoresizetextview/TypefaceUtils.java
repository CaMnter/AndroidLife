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
import android.graphics.Typeface;
import android.os.Build;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

public final class TypefaceUtils {

    public static final String ROBOTO_BLACK = "fonts/Roboto-Black.ttf";
    public static final String ROBOTO_BLACK_ITALIC = "fonts/Roboto-BlackItalic.ttf";
    public static final String ROBOTO_BOLD = "fonts/Roboto-Bold.ttf";
    public static final String ROBOTO_BOLD_CONDENSED = "fonts/Roboto-BoldCondensed.ttf";
    public static final String ROBOTO_BOLD_CONDENSED_ITALIC
        = "fonts/Roboto-BoldCondensedItalic.ttf";
    public static final String ROBOTO_BOLD_ITALIC = "fonts/Roboto-BoldItalic.ttf";
    public static final String ROBOTO_CONDENSED = "fonts/Roboto-Condensed.ttf";
    public static final String ROBOTO_CONDENSED_ITALIC = "fonts/Roboto-CondensedItalic.ttf";
    public static final String ROBOTO_ITALIC = "fonts/Roboto-Italic.ttf";
    public static final String ROBOTO_LIGHT = "fonts/Roboto-Light.ttf";
    public static final String ROBOTO_LIGHT_ITALIC = "fonts/Roboto-LightItalic.ttf";
    public static final String ROBOTO_MEDIUM = "fonts/Roboto-Medium.ttf";
    public static final String ROBOTO_MEDIUM_ITALIC = "fonts/Roboto-MediumItalic.ttf";
    public static final String ROBOTO_REGULAR = "fonts/Roboto-Regular.ttf";
    public static final String ROBOTO_THIN = "fonts/Roboto-Thin.ttf";
    public static final String ROBOTO_THIN_ITALIC = "fonts/Roboto-ThinItalic.ttf";

    private static final Map<String, Typeface> FONTS = new HashMap<String, Typeface>();

    @SuppressWarnings("serial")
    private static final Map<String, Integer> DEFAULT_FONTS = new HashMap<String, Integer>() {
        {
            put(ROBOTO_BOLD, Typeface.BOLD);
            put(ROBOTO_REGULAR, Typeface.NORMAL);
            put(ROBOTO_ITALIC, Typeface.ITALIC);
        }
    };


    private TypefaceUtils() {
    }


    public static Typeface getTypeface(Context context, String font) {
        font = font.intern();

        if (!FONTS.containsKey(font)) {
            FONTS.put(font, Typeface.createFromAsset(context.getAssets(), font));
        }

        Typeface typeface = TypefaceUtils.FONTS.get(font);

        return typeface;
    }


    public static void loadTypeface(TextView view, String font) {
        if (view == null || view.isInEditMode()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
            DEFAULT_FONTS.containsKey(font)) {
            view.setTypeface(Typeface.defaultFromStyle(DEFAULT_FONTS.get(font)));
        }

        Typeface typeface = getTypeface(view.getContext(), font);
        if (typeface == null) {
            Log.v("Font " + font + " not found in assets");
            return;
        }

        view.setTypeface(typeface);
    }

}