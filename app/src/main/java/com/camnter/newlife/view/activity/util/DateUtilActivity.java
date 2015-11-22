package com.camnter.newlife.view.activity.util;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.camnter.newlife.R;
import com.camnter.newlife.utils.DateUtil;

import java.util.Date;


/**
 * Description：DateUtilActivity
 * Created by：CaMnter
 * Time：2015-10-14 15:47
 */
public class DateUtilActivity extends AppCompatActivity {
    TextView string2DateTv;
    TextView date2StringTv;
    TextView getYearMonthDayTv;
    TextView getTimestampStringTv;
    TextView date2yyyyMMddTv;
    TextView date2MMddWeekTv;
    TextView date2yyyyMMddWeekTv;
    TextView time24To12Tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_date_util);

        this.string2DateTv = (TextView) this.findViewById(R.id.string2Date_tv);
        this.date2StringTv = (TextView) this.findViewById(R.id.date2String_tv);
        this.getYearMonthDayTv = (TextView) this.findViewById(R.id.getYearMonthDay_tv);
        this.getTimestampStringTv = (TextView) this.findViewById(R.id.getTimestampString_tv);
        this.date2yyyyMMddTv = (TextView) this.findViewById(R.id.date2yyyyMMdd_tv);
        this.date2MMddWeekTv = (TextView) this.findViewById(R.id.date2MMddWeek_tv);
        this.date2yyyyMMddWeekTv = (TextView) this.findViewById(R.id.date2yyyyMMddWeek_tv);
        this.time24To12Tv = (TextView) this.findViewById(R.id.time24To12_tv);
        this.initData();
    }

    private void initData(){
        long oldTime = System.currentTimeMillis() - 1200000;
        Date date = new Date(oldTime);
        this.string2DateTv.setText(DateUtil.string2Date(date.toString(), "yyyy-MM-dd").toString());
        this.date2StringTv.setText(DateUtil.date2String(oldTime, "yyyy-MM-dd HH:mm:ss"));
        this.getYearMonthDayTv.setText(new Date(DateUtil.getYearMonthDay(oldTime)).toString());
        this.date2yyyyMMddTv.setText(DateUtil.date2yyyyMMdd(date));
        this.date2MMddWeekTv.setText(DateUtil.date2MMddWeek(date));
        this.date2yyyyMMddWeekTv.setText(DateUtil.date2yyyyMMddWeek(date));
        this.time24To12Tv.setText(DateUtil.time24To12("16:26"));
        this.getTimestampStringTv.setText(DateUtil.getTimestampString(date));
    }

}
