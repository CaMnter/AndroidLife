package com.camnter.newlife.mvp.model.impl;

import com.camnter.newlife.mvp.bean.Topic;
import com.camnter.newlife.mvp.model.ITopicModel;
import com.camnter.newlife.mvp.model.callback.TopicModelCallBack;
import java.util.LinkedList;
import java.util.List;

/**
 * Description：
 * Created by：CaMnter
 * Time：2015-11-02 23:50
 */
public class TopicModel implements ITopicModel {

    private TopicModelCallBack callBack;


    public TopicModel(TopicModelCallBack callBack) {
        this.callBack = callBack;
    }


    /**
     * 添加Topic
     */
    @Override public void add(Topic topic) {
        // TODO ...网络请求后...

        // TODO 返回数据 这里默认为返回原数据
        this.callBack.addSuccess(topic);
    }


    /**
     * 删除主题
     */
    @Override public void del(Topic topic) {
        // TODO ...网络请求后...

        // TODO 返回数据 这里默认为返回原数据
        this.callBack.delSuccess(topic);
    }


    /**
     * 修改主题
     */
    @Override public void mod(Long topicId, Topic topic) {
        // TODO ...网络请求后...

        // TODO 返回数据 这里默认为返回原数据
        this.callBack.modSuccess(topic);
    }


    /**
     * 查询主题
     */
    @Override public void query() {
        // TODO ...网络请求后...

        // TODO 返回数据
        Topic topic = new Topic();
        topic.userId = 267L;
        topic.topicId = 1267L;
        topic.content = "Save you from anything";
        List<Topic> list = new LinkedList<Topic>();
        list.add(topic);
        this.callBack.querySuccess(list);
    }
}
