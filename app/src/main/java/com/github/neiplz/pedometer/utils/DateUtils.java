package com.github.neiplz.pedometer.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class DateUtils {

    private final static ThreadLocal<SimpleDateFormat> STANDARD_DATE_FORMATER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };


    public static String getCurrentTimeString() {
        Calendar calendar = Calendar.getInstance();
        return STANDARD_DATE_FORMATER.get().format(calendar.getTime());
    }

    /**
     * @return milliseconds since 1.1.1970 for today 0:00:00
     */
    public static long getToday() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }


}
