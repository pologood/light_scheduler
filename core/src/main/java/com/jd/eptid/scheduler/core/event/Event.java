package com.jd.eptid.scheduler.core.event;

/**
 * Created by classdan on 16-12-20.
 */
public interface Event {

    Object source();

    long time();

}
