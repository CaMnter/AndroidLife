package com.camnter.newlife.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description：SpanData
 * Created by：CaMnter
 * Time：2015-12-27 13:49
 */
public class SpanData implements Parcelable {

    public static final int TITLE = 1;
    public static final int CONTENT = 0;

    private String content;
    private int type;


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public int getType() {
        return type;
    }


    public void setType(int type) {
        this.type = type;
    }


    @Override public int describeContents() {
        return 0;
    }


    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeInt(this.type);
    }


    public SpanData() {
    }


    protected SpanData(Parcel in) {
        this.content = in.readString();
        this.type = in.readInt();
    }


    public static final Creator<SpanData> CREATOR = new Creator<SpanData>() {
        public SpanData createFromParcel(Parcel source) {
            return new SpanData(source);
        }


        public SpanData[] newArray(int size) {
            return new SpanData[size];
        }
    };
}
