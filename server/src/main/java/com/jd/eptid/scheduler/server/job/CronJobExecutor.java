package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.common.ScheduleThreadFactory;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.job.OneShotJob;
import com.jd.eptid.scheduler.core.domain.job.PeriodicJob;
import com.jd.eptid.scheduler.core.utils.TimeUtils;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ClassDan on 2016/9/18.
 */
@Component
public class CronJobExecutor extends AbstractJobExecutor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int ORDER = 3;
    private ScheduledExecutorService scheduledExecutor = null;

    @Override
    protected Future doJob(TrackTask trackTask) {
        Job job = trackTask.getJob();
        long delay = 0;
        if (job.getStartTime() != Job.INSTANT) {
            delay = TimeUtils.getInterval(new Date(), job.getStartTime(), TimeUnit.SECONDS);
        }

        if (job instanceof PeriodicJob) {
            return doPeriodicJob(delay, trackTask);
        } else if (job instanceof OneShotJob) {
            return doOneShotJob(delay, trackTask);
        }
        return null;
    }

    private Future doOneShotJob(long delay, TrackTask trackTask) {
        return scheduledExecutor.schedule(trackTask, delay, TimeUnit.SECONDS);
    }

    private Future doPeriodicJob(long delay, TrackTask trackTask) {
        return scheduledExecutor.scheduleAtFixedRate(trackTask, delay, ((PeriodicJob) trackTask.getJob()).getPeriodicInterval(), TimeUnit.SECONDS);
    }

    @Override
    protected void onJobDone(long jobId, String scheduleId) {
        SubmittedJob submittedJob = submittedJobs.get(jobId);
        if (submittedJob == null) {
            return;
        }

        Job job = submittedJob.getJob();
        if (job instanceof OneShotJob) {
            logger.info("Remove the submittedJob: {}, {}.", jobId, scheduleId);
            removeSubmittedJob(submittedJob);
        } else if (job instanceof PeriodicJob) {
            if (submittedJob.isRemoved()) {
                logger.info("Remove the submittedJob: {}, {}.", jobId, scheduleId);
                removeSubmittedJob(submittedJob);
            } else {
                logger.info("Remove the jobTracker: {}, {}.", jobId, scheduleId);
                submittedJob.removeJobTracker(scheduleId);
            }
        }
    }

    @Override
    public void start() {
        int poolSize = Configuration.getInteger(ConfigItem.BIZ_POOL_SIZE, 5);
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(poolSize, new ScheduleThreadFactory("Cron-Job-Executor-"));
        Assert.notNull(scheduledThreadPoolExecutor);
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        this.scheduledExecutor = scheduledThreadPoolExecutor;
    }

    @Override
    public void stop() {
        logger.info("Stop cron job executor...");
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdownNow();
        }
        logger.info("Stop cron job executor successful.");
    }

    @Override
    public int order() {
        return ORDER;
    }

}
