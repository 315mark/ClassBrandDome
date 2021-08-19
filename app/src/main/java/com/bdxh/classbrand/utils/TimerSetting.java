package com.bdxh.classbrand.utils;

import android.text.format.Time;

public class TimerSetting {

    /***
     *
     * @param beginHour 开始小时       比如   23
     * @param beginMin  开始小时的分钟  比如  00
     * @param endHour   结束小时        比如  5
     * @param endMin    结束小时的分钟   比如 00
     * @return         true表示范围内   否则false
     */
    public static boolean atTheCurrentTime(int beginHour, int beginMin, int endHour, int endMin) {
        boolean result = false;
        final long aDayInMillis = 1000 * 60 * 60 * 24;
        final long currentTimeMillis = System.currentTimeMillis();
        Time now = new Time();
        now.set(currentTimeMillis);
        Time startTime = new Time();
        startTime.set(currentTimeMillis);
        startTime.hour = beginHour;
        startTime.minute = beginMin;
        Time endTime = new Time();
        endTime.set(currentTimeMillis);
        endTime.hour = endHour;
        endTime.minute = endMin;
        /**跨天的特殊情况(比如23:00-2:00)*/
        if (!startTime.before(endTime)) {
            startTime.set(startTime.toMillis(true) - aDayInMillis);
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
            Time startTimeInThisDay = new Time();
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis);
            if (!now.before(startTimeInThisDay)) {
                result = true;
            }
        } else {
            /**普通情况(比如5:00-10:00)*/
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
        }
        return result;
    }
}
