package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.job.CronJob;
import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.job.ScheduledJob;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by ClassDan on 2016/9/18.
 */
@Component
public class CronJobExecutor implements JobExecutor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ScheduledExecutorService scheduledExecutor = null;
    private List<ScheduledFuture> scheduledFutures = new LinkedList();
    @Resource
    private ScheduledJobDao scheduledJobDao;

    @Override
    public boolean supports(Job job) {
        return job instanceof CronJob;
    }

    @Override
    public Future execute(Job job) {
        logger.info("Prepare to execute job: {}.", job);
        Assert.notNull(job.getId());

        CronJob cronJob = (CronJob) job;
        ScheduledJob scheduledJob = scheduledJobDao.findRunningJobByJobId(cronJob.getId());
        if (scheduledJob != null && !cronJob.allowMultipleExecution()) {
            throw new ScheduleException("Waiting for previous job to complete.", job, scheduledJob);
        }

        return doExecute(job);
    }

    private ScheduledFuture doExecute(final Job job) {
        ScheduledFuture future = scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    JobContext context = new JobContext();
                    context.setJob(job);
                    JobContextHolder.setContext(context);

                    JobTracker jobTracker = new JobTracker(context);
                    jobTracker.start();
                } catch (Exception e) {
                    logger.error("Failed to execute job: {}.", job, e);
                }
            }
        }, 0, ((CronJob) job).getPeriodicInterval(), TimeUnit.SECONDS);
        scheduledFutures.add(future);
        return future;
    }

    @Override
    public void start() {
        int poolSize = Configuration.getInteger(ConfigItem.BIZ_POOL_SIZE, 5);
        scheduledExecutor = Executors.newScheduledThreadPool(poolSize, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "cron-job-executor");
            }
        });
        Assert.notNull(scheduledExecutor);
    }

    @Override
    public void stop() {
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
        }
    }

}
