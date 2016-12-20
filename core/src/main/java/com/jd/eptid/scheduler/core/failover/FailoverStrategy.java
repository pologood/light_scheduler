package com.jd.eptid.scheduler.core.failover;

/**
 * Created by classdan on 16-9-23.
 */
public interface FailoverStrategy<T> {

    T handle(FailureJudger<T> failureJudger, FailoverAction<T> action);

}
