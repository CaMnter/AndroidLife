package com.camnter.newlife.mvp.iview;

import com.camnter.newlife.mvp.bean.Topic;
import java.util.List;

/**
 * Description：IMvpView
 * Created by：CaMnter
 * Time：2015-11-02 23:53
 */
public interface IMvpView {
    /**
     * 添加成功
     */
    void addSuccess(Topic topic);

    /**
     * 删除成功
     */
    void delSuccess(Topic topic);

    /**
     * 修改成功
     */
    void modSuccess(Topic newTopic);

    /**
     * 查询成功
     */
    void querySuccess(List<Topic> topics);

    /**
     * 发生错误
     */
    void error();
}
