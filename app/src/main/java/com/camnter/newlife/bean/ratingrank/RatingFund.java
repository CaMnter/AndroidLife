package com.camnter.newlife.bean.ratingrank;

/**
 * Description：RatingFund
 * Created by：CaMnter
 */

public class RatingFund {

    private String name;
    private String fundCode;
    private int latestUpdatedDate;
    private int level;


    public String getName() { return name;}


    public void setName(String name) { this.name = name;}


    public String getFundCode() { return fundCode;}


    public void setFundCode(String fundCode) { this.fundCode = fundCode;}


    public int getLatestUpdatedDate() { return latestUpdatedDate;}


    public void setLatestUpdatedDate(int latestUpdatedDate) {
        this.latestUpdatedDate = latestUpdatedDate;
    }


    public int getLevel() { return level;}


    public void setLevel(int level) { this.level = level;}

}
