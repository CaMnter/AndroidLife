package com.camnter.newlife.adapter;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter;
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.SpanData;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Description：SpanRecyclerAdapter
 * Created by：CaMnter
 * Time：2015-12-27 13:41
 */
public class SpanRecyclerAdapter extends EasyRecyclerViewAdapter {

    private Context context;

    private static final int URL_SPAN = 1;
    private static final int UNDERLINE_SPAN = 2;
    private static final int TYPEFACE_SPAN = 3;
    private static final int TEXT_APPERARANCE_SPAN = 4;
    private static final int TAB_STOP_SPAN = 5;
    private static final int SUPERS_SCRIPT_SPAN = 6;
    private static final int SUB_SCRIPT_SPAN = 7;
    private static final int STYLE_SPAN = 8;
    private static final int STRIKE_THROUGH_SPAN = 9;
    private static final int SCALE_X_SPAN = 10;
    private static final int RELATIVE_SIZE_SPAN = 11;
    private static final int RASTERIZER_SPAN = 12;
    private static final int QUOTO_SPAN = 13;
    private static final int MASK_FILTER_SPAN = 14;
    private static final int LEADING_MARGIN_SPAN = 15;
    private static final int IMAGE_SPAN = 16;
    private static final int ICON_MARGIN_SPAN = 17;
    private static final int FOREGROUND_COLOR_SPAN = 18;
    private static final int DRAWABLE_MARGIN_SPAN = 19;
    private static final int BULLET_SPAN = 20;
    private static final int BACKGROUND_COLOR_SPAN = 21;
    private static final int ALIGNMENT_SPAN_STANDARD = 22;
    private static final int ABSOLUTE_SIZE_SPAN = 23;

    public SpanRecyclerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int[] getItemLayouts() {
        return new int[]{R.layout.item_span_content, R.layout.item_span_title};
    }

    @Override
    public void onBindRecycleViewHolder(EasyRecyclerViewHolder easyRecyclerViewHolder, int i) {
        SpanData spanData = this.getItem(i);
        if (spanData == null) return;
        int itemType = this.getRecycleViewItemType(i);
        switch (itemType) {
            case SpanData.CONTENT: {
                TextView labelTV = easyRecyclerViewHolder.findViewById(R.id.span_label_tv);
                TextView contentTV = easyRecyclerViewHolder.findViewById(R.id.span_content_tv);
                if (spanData.getContent() != null) {
                    this.setSpanContent(labelTV, contentTV, spanData.getContent(), i);
                } else {
                    contentTV.setText("??????");
                    labelTV.setText("??????");
                }
                break;
            }
        }
    }

    @Override
    public int getRecycleViewItemType(int i) {
        SpanData spanData = this.getItem(i);
        return spanData.getType();
    }

    public void setSpanContent(TextView labelTV, TextView contentTV, String content, int position) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(content);
        String sub = "Save";
        int start = content.indexOf("Save");
        switch (position) {
            case URL_SPAN: {
                labelTV.setText("URLSpan");
                ssb.setSpan(new URLSpan("https://github.com/CaMnter"), start, sub.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                contentTV.setText(ssb);
                // 在单击链接时凡是有要执行的动作，都必须设置MovementMethod对象
                contentTV.setMovementMethod(LinkMovementMethod.getInstance());
                // 设置点击后的颜色，这里涉及到ClickableSpan的点击背景
                contentTV.setHighlightColor(0xff8FABCC);
                break;
            }
            case UNDERLINE_SPAN: {
                labelTV.setText("UnderlineSpan");
                ssb.setSpan(new UnderlineSpan(), start, "Save".length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                contentTV.setText(ssb);
                break;
            }
            case TYPEFACE_SPAN: {
                labelTV.setText("TypefaceSpan ( Examples include \"monospace\", \"serif\", and \"sans-serif\". )");
                ssb.setSpan(new TypefaceSpan("serif"), start, sub.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                contentTV.setText(ssb);
                break;
            }
            case TEXT_APPERARANCE_SPAN: {
                labelTV.setText("TextAppearanceSpan");
                ColorStateList colorStateList = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    colorStateList = this.context.getColorStateList(R.color.selector_apperarance_span);
                } else {
                    try {
                        colorStateList = ColorStateList.createFromXml(this.context.getResources(), this.context.getResources().getXml(R.color.selector_apperarance_span));
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }
                }
                ssb.setSpan(new TextAppearanceSpan("serif", Typeface.BOLD_ITALIC, this.context.getResources().getDimensionPixelSize(R.dimen.text_appearance_span), colorStateList, colorStateList), start, sub.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                contentTV.setText(ssb);
                break;
            }
        }
    }

}
