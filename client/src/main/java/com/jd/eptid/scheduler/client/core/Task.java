package com.jd.eptid.scheduler.client.core;


/**
 * Created by classdan on 16-9-19.
 */
public interface Task<T> {

    String job();

    void run(T param) throws Exception;

}
