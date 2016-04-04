package com.camnter.newlife.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import com.camnter.newlife.bean.Tag;
import com.camnter.newlife.widget.span.ClickableSpanNoUnderline;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Description：TagTextView
 * Created by：CaMnter
 * Time：2015-12-22 16:48
 */
public class TagTextView extends TextView {

    private ClickableSpanNoUnderline.OnClickListener onTagClickListener;


    public TagTextView(Context context) {
        super(context);
    }


    public TagTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public TagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * 添加标签ClickableSpan
     *
     * @param tags tags
     * @param content content
     * @return SpannableStringBuilder
     */
    public SpannableStringBuilder addTagClickableSpan(ArrayList<Tag> tags, String content, ClickableSpanNoUnderline.OnClickListener onTagClickListener) {
        this.onTagClickListener = onTagClickListener;
        StringBuilder sbTag = new StringBuilder();
        Map<String, Tag> content2TagDict = new HashMap<>();
        /**
         * 添加 #
         */
        if (tags != null && tags.size() > 0) {
            for (Tag tag : tags) {
                sbTag.append("#");
                sbTag.append(tag.getContent());
                sbTag.append("#");
                sbTag.append(" ");
                content2TagDict.put(tag.getContent(), tag);
            }
        }
        int tagLength = sbTag.toString().length();
        sbTag.append(content);

        /**
         * 添加颜色
         */
        SpannableStringBuilder sb = new SpannableStringBuilder(sbTag.toString());
        if (tagLength > 0) {
            String s = sb.toString();
            String[] model = s.split("#");
            for (int i = 0; i < model.length - 1; i++) {
                /**
                 * 过滤 "" 和 " "
                 */
                if ("".equals(model[i]) || " ".equals(model[i])) continue;
                int index = s.indexOf(model[i]);
                int mLength = model[i].length();
                TagClickableSpan span = new TagClickableSpan(0xffFF4081, this.onTagClickListener);
                span.setContent(model[i]);
                Tag tag = content2TagDict.get(model[i]);
                if (tag != null && tag.getId() != null) {
                    span.setId(tag.getId());
                }
                /**
                 * 设置TagClickableSpan
                 */
                sb.setSpan(span, index - 1, index + mLength + 1,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return sb;
    }


    /**
     * Tag ClickableSpan
     */
    public class TagClickableSpan extends ClickableSpanNoUnderline {

        private Long id;
        private String content;


        public TagClickableSpan(int color, OnClickListener onClickListener) {
            super(color, onClickListener);
        }


        public TagClickableSpan(OnClickListener onClickListener) {
            super(onClickListener);
        }


        public Long getId() {
            return id;
        }


        public void setId(Long id) {
            this.id = id;
        }


        public String getContent() {
            return content;
        }


        public void setContent(String content) {
            this.content = content;
        }
    }
}
