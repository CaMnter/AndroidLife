package com.camnter.newlife.mvp.model;


import com.camnter.newlife.mvp.bean.Topic;

/**
 * Description：
 * Created by：CaMnter
 * Time：2015-11-02 23:45
 */
public interface ITopicModel {
    /**
     * 添加Topic
     *
     * @param topic
     */
    void add(Topic topic);

    /**
     * 删除主题
     *
     * @param topic
     */
    void del(Topic topic);

    /**
     * 修改主题
     *
     * @param topicId
     * @param topic
     */
    void mod(Long topicId, Topic topic);

    /**
     * 查询主题
     *
     * @return
     */
    void query();
}
