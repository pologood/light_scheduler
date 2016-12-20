package com.jd.eptid.scheduler.core.failover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by classdan on 16-10-20.
 */
public abstract class FailoverSupportedAction<T> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public T perform() {
        T returnValue = null;
        Throwable ex = null;
        try {
            returnValue = action();
        } catch (Throwable e) {
            logger.error("Failed to perform the action.", e);
            ex = e;
        }

        if (getFailureJudger().isFailed(returnValue, ex)) {
            return getFailoverStrategy().handle(getFailureJudger(), getFailoverAction());
        }
        return returnValue;
    }

    public abstract T action() throws Exception;

    public abstract FailoverStrategy<T> getFailoverStrategy();

    public abstract FailureJudger<T> getFailureJudger();

    public abstract FailoverAction<T> getFailoverAction();

}
