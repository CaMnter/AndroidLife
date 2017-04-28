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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.TextUtils.TruncateAt;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import com.camnter.newlife.widget.R;

public class TextView extends android.support.v7.widget.AppCompatTextView {

    private static final TransformationMethod TRANSFORMATION_TEXT_ALL_CAPS
        = new TransformationMethod() {
        @Override
        public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction,
                                   Rect previouslyFocusedRect) {
        }


        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return source.toString()
                .toUpperCase(view.getContext().getResources().getConfiguration().locale);
        }
    };


    public TextView(Context context) {
        super(context);
        init(context, null, 0);
    }


    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }


    public TextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }


    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextView, defStyle,
            0);
        final int textStyle = a.getInt(R.styleable.TextView_textStyle, 0);
        final String typeface = a.getString(R.styleable.TextView_typeface);
        final boolean textAllCaps = a.getBoolean(R.styleable.TextView__textAllCaps, false);

        a.recycle();

        if (typeface != null) {
            Ui.setTypeface(this, typeface);
        } else if (textStyle > 0) {
            Ui.setTypefaceByStyle(this, textStyle);
        }

        if (textAllCaps) {
            setTransformationMethod(TRANSFORMATION_TEXT_ALL_CAPS);
        }
    }


    @Override
    public boolean isFocused() {
        if (getEllipsize() == TruncateAt.MARQUEE) {
            return true;
        }

        return super.isFocused();
    }

}