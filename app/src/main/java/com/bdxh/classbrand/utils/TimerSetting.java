package com.bdxh.classbrand.utils;

import java.util.Calendar;
import java.util.Date;

public class TimerSetting {

    public static boolean isEffectiveDate(Date startTime, Date endTime){
        final long tenMin = 1000 * 60 * 5;
        //提前10分钟将
        final long currentTimeMillis = (System.currentTimeMillis() - tenMin);
        Date nowTime = new Date();
        nowTime.setTime(currentTimeMillis);

        if ( nowTime.getTime()== startTime.getTime() || nowTime.getTime()== endTime.getTime()){
            return true;
        }
        Calendar now = Calendar.getInstance();
        now.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        return now.after(begin) && now.before(end);
    }


}
