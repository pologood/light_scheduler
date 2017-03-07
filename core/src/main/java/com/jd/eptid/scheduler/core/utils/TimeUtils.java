package com.jd.eptid.scheduler.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-11-7.
 */
public class TimeUtils {
    private static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static void sleep(int num, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(num);
        } catch (InterruptedException e) {
            //Ignore
        }
    }

    public static long getInterval(Date time1, Date time2, TimeUnit timeUnit) {
        if (time2.before(time1)) {
            return 0L;
        }

        long ms = time2.getTime() - time1.getTime();
        return timeUnit.convert(ms, TimeUnit.MILLISECONDS);
    }

    public static Date futureDate(Date baseDate, long interval, TimeUnit timeUnit) {
        long baseMs = baseDate.getTime();
        long intervalMs = timeUnit.toMillis(interval);
        return new Date(baseMs + intervalMs);
    }

    public static Date parseLongDate(String dateString) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(LONG_DATE_FORMAT);
        return dateFormatter.parse(dateString);
    }

}
