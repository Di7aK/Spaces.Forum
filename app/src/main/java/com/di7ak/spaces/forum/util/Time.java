package com.di7ak.spaces.forum.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Time {
    
    public static String toString(long time) {
        Date current = new Date();
        Date date = new Date(time);
        String sDate;
        if(date.getDay() != current.getDay() ||
            date.getMonth() != current.getMonth()) {
             sDate = new SimpleDateFormat("dd.MM HH:mm").format(time);
        } else sDate = new SimpleDateFormat("HH:mm").format(time);
        return sDate;
    }
}
