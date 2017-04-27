package com.camnter.newlife.bean.ratingrank;

import java.util.List;

/**
 * Description：RatingRankResponse
 * Created by：CaMnter
 */

public class RatingRankResponse {

    private int code;
    private String msg;
    private DataEntity data;


    public int getCode() { return code;}


    public void setCode(int code) { this.code = code;}


    public String getMsg() { return msg;}


    public void setMsg(String msg) { this.msg = msg;}


    public DataEntity getData() { return data;}


    public void setData(DataEntity data) { this.data = data;}


    public static class DataEntity {

        private int totalPage;
        private int totalSize;
        private List<RatingFund> funds;


        public int getTotalPage() { return totalPage;}


        public void setTotalPage(int totalPage) { this.totalPage = totalPage;}


        public int getTotalSize() { return totalSize;}


        public void setTotalSize(int totalSize) { this.totalSize = totalSize;}


        public List<RatingFund> getFunds() { return funds;}


        public void setFunds(List<RatingFund> funds) { this.funds = funds;}

    }

}
