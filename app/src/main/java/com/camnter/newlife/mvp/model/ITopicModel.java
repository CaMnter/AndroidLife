package com.camnter.newlife.mvp.model;

import com.camnter.newlife.mvp.bean.Topic;

/**
 * Description：ITopicModel
 * Created by：CaMnter
 * Time：2015-11-02 23:45
 */
public interface ITopicModel {
    /**
     * 添加Topic
     */
    void add(Topic topic);

    /**
     * 删除主题
     */
    void del(Topic topic);

    /**
     * 修改主题
     */
    void mod(Long topicId, Topic topic);

    /**
     * 查询主题
     */
    void query();
}
