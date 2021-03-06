package com.camnter.newlife.bean;

/**
 * Description：RxData
 * Created by：CaMnter
 * Time：2015-12-01 17:40
 */
public class RxData {

    private long id;
    private String content;
    private RxChildData[] childData;


    public RxChildData[] getChildData() {
        return childData;
    }


    public void setChildData(RxChildData[] childData) {
        this.childData = childData;
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }

}
