package com.jd.eptid.scheduler.client.core;


/**
 * Created by classdan on 16-9-22.
 */
public interface Job<T> {

    String name();

    SplitResult<T> split(int splitTimes);

}
