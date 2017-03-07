package com.jd.eptid.scheduler.core.common;

/**
 * Created by ClassDan on 2016/9/18.
 */
public interface LifeCycle {

    void start();

    void stop();

    int order();

}
