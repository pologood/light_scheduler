package com.jd.eptid.scheduler.server.failover;

import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.utils.TimeUtils;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import com.jd.eptid.scheduler.server.core.ServerContext;
import com.jd.eptid.scheduler.server.job.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 17-1-20.
 */
public class RetryHandler implements FailureHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<Long, RetryContext> retryContexts = new HashMap<Long, RetryContext>();
    private JobScheduler jobScheduler;

    public RetryHandler() {
        jobScheduler = ServerContext.getInstance().getJobScheduler();
    }

    @Override
    public void handle(Job job) {
        RetryContext retryContext = null;
        synchronized (retryContexts) {
            retryContext = retryContexts.get(job.getId());
            if (retryContext == null) {
                retryContext = new RetryContext(job);
                retryContexts.put(job.getId(), retryContext);
            } else {
                int maxRetryTimes = Configuration.getInteger(ConfigItem.JOB_RETRY_TIMES, 3);
                if (retryContext.retryTimes > maxRetryTimes) {
                    retryContexts.remove(job.getId());
                    throw new ScheduleException("Job [" + job.getId() + "] has failed more than " + maxRetryTimes + " times, abort this job.");
                }
                ++retryContext.retryTimes;
            }
        }

        int retryDelay = getRetryDelay(retryContext);
        logger.info("Job [{}] will retry after {} seconds.", job.getId(), retryDelay);
        TimeUtils.sleep(retryDelay, TimeUnit.SECONDS);
        // re-submit this job
        jobScheduler.resubmit(job);
    }

    private int getRetryDelay(RetryContext retryContext) {
        int retryTimes = retryContext.retryTimes;
        int maxDelay = 300;
        int minDelay = 10;
        Random random = new Random();
        int delay = minDelay * retryTimes + random.nextInt(minDelay);
        return Math.min(delay, maxDelay);
    }

    private class RetryContext {
        private Job job;
        private int retryTimes;

        public RetryContext(Job job) {
            this.job = job;
            this.retryTimes = 1;
        }
    }
}
