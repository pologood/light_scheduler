package com.jd.eptid.scheduler.server.job;


import com.jd.eptid.scheduler.core.common.ScheduleThreadFactory;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.job.OneShotJob;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by classdan on 16-9-19.
 */
@Deprecated
public class NormalJobExecutor extends AbstractJobExecutor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int ORDER = 3;
    private ExecutorService executorService;

    @Override
    protected Future doJob(TrackTask trackTask) {
        return executorService.submit(trackTask);
    }

    @Override
    protected void onJobDone(long jobId, String scheduleId) {
        submittedJobs.remove(jobId);
    }

    @Override
    public void start() {
        int poolSize = Configuration.getInteger(ConfigItem.BIZ_POOL_SIZE, 5);
        executorService = Executors.newFixedThreadPool(poolSize, new ScheduleThreadFactory("Normal-Job-Executor-"));
        Assert.notNull(executorService);
    }

    @Override
    public void stop() {
        logger.info("Stop normal job executor...");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        logger.info("Stop normal job executor successful.");
    }

    @Override
    public int order() {
        return ORDER;
    }

}
