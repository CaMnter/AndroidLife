package com.camnter.newlife.views.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.camnter.newlife.R;
import com.camnter.newlife.widget.text.ClickableSpanNoUnderline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Description：TagTextViewActivity
 * Created by：CaMnter
 * Time：2015-12-22 11:42
 */
public class TagTextViewActivity extends AppCompatActivity {

    private TextView tagTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_tag_textview);
        this.tagTV = (TextView) this.findViewById(R.id.tag_text_view_tv);
        this.initData();
    }

    private void initData() {
        ArrayList<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag();
        tag1.setId(2601L);
        tag1.setContent("初心不改");
        Tag tag2 = new Tag();
        tag2.setId(2602L);
        tag2.setContent("方能始终");
        Tag tag3 = new Tag();
        tag3.setId(2603L);
        tag3.setContent("CaMnter");
        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);

        String sign = "Save you from anything 06。";
        this.tagTV.setText(this.addTagClickableSpan(tags, sign));
        // 在单击链接时凡是有要执行的动作，都必须设置MovementMethod对象
        this.tagTV.setMovementMethod(LinkMovementMethod.getInstance());
        // 设置点击后的颜色，这里涉及到ClickableSpan的点击背景
        this.tagTV.setHighlightColor(Color.TRANSPARENT);
    }

    private class Tag {
        private Long id;
        private String content;

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

    private class TagClickableSpan extends ClickableSpanNoUnderline {
        private Long tagId;

        public TagClickableSpan(int color) {
            super(color);
        }

        public void setTagId(Long tagId) {
            this.tagId = tagId;
        }

        /**
         * Performs the click action associated with this span.
         *
         * @param widget widget
         */
        @Override
        public void onClick(View widget) {
            super.onClick(widget);
            Toast.makeText(TagTextViewActivity.this, "tagId = " + this.tagId, Toast.LENGTH_SHORT).show();
        }
    }

    private SpannableStringBuilder addTagClickableSpan(ArrayList<Tag> tags, String content) {
        StringBuilder sbTag = new StringBuilder();
        Map<String, Tag> content2TagDict = new HashMap<>();
        /**
         * 添加#
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
                TagClickableSpan span = new TagClickableSpan(0xffFF4081);
                Tag tag = content2TagDict.get(model[i]);
                if (tag != null && tag.getId() != null) {
                    span.setTagId(tag.getId());
                }
                sb.setSpan(span, index - 1, index + mLength + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return sb;
    }

}
