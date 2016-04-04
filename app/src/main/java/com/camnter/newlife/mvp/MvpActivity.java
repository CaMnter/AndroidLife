package com.camnter.newlife.mvp;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.mvp.bean.Topic;
import com.camnter.newlife.mvp.iview.IMvpView;
import com.camnter.newlife.mvp.presenter.MvpPresenter;
import java.util.List;

/**
 * Description：MvpActivity
 * Created by：CaMnter
 * Time：2015-11-02 23:34
 */
public class MvpActivity extends BaseAppCompatActivity implements IMvpView {

    private TextView addTV;
    private TextView delTV;
    private TextView modTV;
    private TextView queryTV;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_mvp;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.addTV = (TextView) this.findViewById(R.id.add_tv);
        this.delTV = (TextView) this.findViewById(R.id.del_tv);
        this.modTV = (TextView) this.findViewById(R.id.mod_tv);
        this.queryTV = (TextView) this.findViewById(R.id.query_tv);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        MvpPresenter presenter = new MvpPresenter(this);

        Topic topic = new Topic();
        topic.userId = 267L;
        topic.topicId = 1267L;
        topic.content = "Save you from anything";

        presenter.addTopic(topic);
        presenter.delTopic(topic);
        presenter.modTopic(topic);
        presenter.queryTopic();
    }


    /**
     * 添加成功
     */
    @Override public void addSuccess(Topic topic) {
        Log.i("MvpActivity", topic.content);
        this.addTV.setText("Add:" + topic.content);
    }


    /**
     * 删除成功
     */
    @Override public void delSuccess(Topic topic) {
        Log.i("MvpActivity", topic.content);
        this.delTV.setText("Del:" + topic.content);
    }


    /**
     * 修改成功
     */
    @Override public void modSuccess(Topic newTopic) {
        Log.i("MvpActivity", newTopic.content);
        this.modTV.setText("Mod:" + newTopic.content);
    }


    /**
     * 发生错误
     */
    @Override public void error() {

    }


    /**
     * 查询成功
     */
    @Override public void querySuccess(List<Topic> list) {
        Topic topic = list.get(0);
        Log.i("MvpActivity", topic.content);
        this.queryTV.setText("Query:" + topic.content);
    }
}
