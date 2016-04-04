package com.camnter.newlife.mvp.presenter;

import com.camnter.newlife.mvp.bean.Topic;
import com.camnter.newlife.mvp.iview.IMvpView;
import com.camnter.newlife.mvp.model.ITopicModel;
import com.camnter.newlife.mvp.model.callback.TopicModelCallBack;
import com.camnter.newlife.mvp.model.impl.TopicModel;
import java.util.List;

/**
 * Description：MvpPresenter
 * Created by：CaMnter
 * Time：2015-11-02 23:52
 */
public class MvpPresenter {
    private IMvpView iView;
    private ITopicModel model;


    public MvpPresenter(IMvpView iView) {
        this.iView = iView;
        this.model = new TopicModel(new TopicModelCallBack() {
            /**
             * 添加成功
             *
             * @param topic topic
             */
            @Override public void addSuccess(Topic topic) {
                MvpPresenter.this.iView.addSuccess(topic);
            }


            /**
             * 删除成功
             *
             * @param topic topic
             */
            @Override public void delSuccess(Topic topic) {
                MvpPresenter.this.iView.delSuccess(topic);
            }


            /**
             * 修改成功
             *
             * @param newTopic newTopic
             */
            @Override public void modSuccess(Topic newTopic) {
                MvpPresenter.this.iView.modSuccess(newTopic);
            }


            /**
             * 查询成功
             *
             * @param topics topics
             */
            @Override public void querySuccess(List<Topic> topics) {
                MvpPresenter.this.iView.querySuccess(topics);
            }


            /**
             * 发生错误
             */
            @Override public void error() {
                MvpPresenter.this.iView.error();
            }
        });
    }


    /**
     * 添加主题
     */
    public void addTopic(Topic topic) {
        this.model.add(topic);
    }


    /**
     * 删除主题
     */
    public void delTopic(Topic topic) {
        this.model.del(topic);
    }


    /**
     * 修改主题
     */
    public void modTopic(Topic topic) {
        this.model.mod(topic.topicId, topic);
    }


    /**
     * 查询主题
     */
    public void queryTopic() {
        this.model.query();
    }
}
