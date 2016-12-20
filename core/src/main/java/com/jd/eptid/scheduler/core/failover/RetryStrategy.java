package com.jd.eptid.scheduler.core.failover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by classdan on 16-9-23.
 */
public class RetryStrategy<T> implements FailoverStrategy<T> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private int maxRetryTimes = 3;

    @Override
    public T handle(FailureJudger<T> failureJudger, FailoverAction<T> action) {
        int retryTimes = 0;
        while (retryTimes++ < maxRetryTimes) {
            logger.info("Retry times: {}.", retryTimes);

            T returnValue = null;
            Throwable ex = null;
            try {
                returnValue = action.perform();
            } catch (Throwable e) {
                logger.error("Failed to retry. {}", e.getMessage());
                ex = e;
            }

            if (failureJudger.isFailed(returnValue, ex)) {
                continue;
            }
            return returnValue;
        }
        throw new RuntimeException("Retry times exceed the limits: " + maxRetryTimes);
    }

}
