package com.jd.eptid.scheduler.core.failover;

/**
 * Created by classdan on 16-10-19.
 */
public interface FailureJudger<T> {

    boolean isFailed(T returnValue, Throwable e);

}
