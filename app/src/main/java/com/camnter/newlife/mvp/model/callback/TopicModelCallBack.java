package com.camnter.newlife.mvp.model.callback;

import com.camnter.newlife.mvp.bean.Topic;

import java.util.List;

/**
 * Description：TopicModelCallBack
 * Created by：CaMnter
 * Time：2015-11-03 16:55
 */
public abstract class TopicModelCallBack {
    /**
     * 添加成功
     *
     * @param topic
     */
    public void addSuccess(Topic topic) {
    }

    /**
     * 删除成功
     *
     * @param topic
     */
    public void delSuccess(Topic topic) {
    }

    /**
     * 修改成功
     *
     * @param newTopic
     */
    public void modSuccess(Topic newTopic) {
    }

    /**
     * 查询成功
     *
     * @return
     */
    public void querySuccess(List<Topic> topics) {
    }

    /**
     * 发生错误
     */
    public abstract void error();
}
