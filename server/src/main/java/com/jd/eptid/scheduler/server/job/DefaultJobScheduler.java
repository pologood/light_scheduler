package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by classdan on 16-9-21.
 */
@Component
public class DefaultJobScheduler implements JobScheduler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource(name = "jobExecutors")
    private List<JobExecutor> jobExecutors = new ArrayList();

    @Override
    public void submit(Job job) {
        logger.info("Job {} submitted.", job);
        Assert.notNull(job.getId());

        JobExecutor jobExecutor = findJobExecutor(job);
        if (jobExecutor == null) {
            throw new ScheduleException("No jobScheduler found for job: " + job);
        }

        jobExecutor.execute(job);
    }

    @Override
    public void cancel(long submittedJobId) {
        logger.info("Apply cancel job: {}.", submittedJobId);
    }

    @Override
    public void suspend(long submittedJobId) {
        logger.info("Apply suspend job: {}.", submittedJobId);
    }

    private JobExecutor findJobExecutor(Job job) {
        for (JobExecutor jobExecutor : jobExecutors) {
            if (jobExecutor.supports(job)) {
                return jobExecutor;
            }
        }
        return null;
    }

    @Override
    public void start() {
        for (JobExecutor jobExecutor : jobExecutors) {
            jobExecutor.start();
        }
    }

    @Override
    public void stop() {
        for (JobExecutor jobExecutor : jobExecutors) {
            jobExecutor.stop();
        }
    }

}
