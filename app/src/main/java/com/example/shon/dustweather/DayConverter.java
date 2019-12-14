package com.example.shon.dustweather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DayConverter {
    private static SimpleDateFormat simpleDateFormat;

    public static String getTime(long sec){
        Date time = new Date(sec * 1000);
        simpleDateFormat = new SimpleDateFormat("hh:mm a");
        String timeSt = simpleDateFormat.format(time);
        return timeSt;
    }

    public  static String getDate(long sec){
        Date date = new Date(sec * 1000);
        //한국 요일
        simpleDateFormat = new SimpleDateFormat("EEE", Locale.KOREAN);
        String dateString = simpleDateFormat.format(date);
        return dateString;
    }
}
