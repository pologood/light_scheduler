package com.jd.eptid.scheduler.core.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-11-7.
 */
public class MiscUtils {

    public static void sleep(int num, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(num);
        } catch (InterruptedException e) {
            //Ignore
        }
    }

}
