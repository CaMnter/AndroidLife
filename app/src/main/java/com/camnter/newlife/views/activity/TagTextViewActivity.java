package com.camnter.newlife.views.activity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Toast;
import com.camnter.newlife.R;
import com.camnter.newlife.bean.Tag;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.ToastUtils;
import com.camnter.newlife.widget.TagTextView;
import com.camnter.newlife.widget.span.ClickableSpanNoUnderline;
import java.util.ArrayList;

/**
 * Description：TagTextViewActivity
 * Created by：CaMnter
 * Time：2015-12-22 11:42
 */
public class TagTextViewActivity extends BaseAppCompatActivity
        implements ClickableSpanNoUnderline.OnClickListener<TagTextView.TagClickableSpan> {

    private TagTextView tagTV;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_tag_textview;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.tagTV = (TagTextView) this.findViewById(R.id.tag_text_view_tv);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    @Override protected void initData() {
        ArrayList<Tag> tags = new ArrayList<>();

        Tag tag1 = new Tag();
        tag1.setId(2601L);
        tag1.setContent("初心不改");
        Tag tag2 = new Tag();
        tag2.setId(2602L);
        tag2.setContent("方能始终");
        Tag tag3 = new Tag();
        tag3.setId(2603L);
        tag3.setContent("Save You From Anything");

        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);

        String sign = "这个世上不存在束缚人的枷锁......Save You From Anything......";
        this.tagTV.setText(this.tagTV.addTagClickableSpan(tags, sign, this));
        // 在单击链接时凡是有要执行的动作，都必须设置MovementMethod对象
        this.tagTV.setMovementMethod(LinkMovementMethod.getInstance());
        // 设置点击后的颜色，这里涉及到ClickableSpan的点击背景
        this.tagTV.setHighlightColor(0xff8FABCC);
    }


    /**
     * ClickableSpan被点击
     *
     * @param widget widget
     * @param span span
     */
    @Override public void onClick(View widget, TagTextView.TagClickableSpan span) {
        ToastUtils.show(this, span.getId() + ":" + span.getContent(), Toast.LENGTH_SHORT);
    }
}
