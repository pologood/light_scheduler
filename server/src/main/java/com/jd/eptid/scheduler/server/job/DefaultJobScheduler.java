package com.jd.eptid.scheduler.server.job;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.core.common.ShutdownHook;
import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.job.OneShotJob;
import com.jd.eptid.scheduler.core.domain.job.PeriodicJob;
import com.jd.eptid.scheduler.core.domain.job.ScheduledJob;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.event.Event;
import com.jd.eptid.scheduler.core.event.JobEvent;
import com.jd.eptid.scheduler.core.event.MasterChangedEvent;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.listener.EventListener;
import com.jd.eptid.scheduler.core.utils.TimeUtils;
import com.jd.eptid.scheduler.server.core.ServerContext;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-9-21.
 */
@Component
public class DefaultJobScheduler implements JobScheduler, EventListener<Event> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int ORDER = 1;
    @Resource
    private JobManager jobManager;
    @Resource
    private ScheduledJobDao scheduledJobDao;
    @Resource
    private JobExecutor jobExecutor;
    private CountDownLatch startedLatch = new CountDownLatch(1);

    public DefaultJobScheduler() {
        ServerContext.getInstance().getEventBroadcaster().register(MasterChangedEvent.class, this);
        ServerContext.getInstance().getEventBroadcaster().register(JobEvent.class, this);
        ShutdownHook.getInstance().addLifeCycleObject(this);
    }

    @Override
    public void submit(Job job) {
        doSubmit(job, false);
    }

    @Override
    public void resubmit(Job job) {
        doSubmit(job, true);
    }

    private void doSubmit(Job job, boolean isRetry) {
        checkStatus();

        logger.info("Submit job: {}...", job);
        Assert.notNull(job.getId());

        SubmittedJob submittedJob = new SubmittedJob(job);
        if (isRetry) {
            submittedJob.setRetry(true);
        }
        jobExecutor.execute(submittedJob);
    }

    @Override
    public void cancel(long jobId) {
        checkStatus();

        logger.info("Apply cancel job: {}...", jobId);
        try {
            jobExecutor.cancel(jobId);
        } catch (InterruptedException e) {
            logger.error("Failed to cancel the job: {}.", jobId);
            Throwables.propagate(e);
        }
    }

    @Override
    public void suspend(long jobId) {
        checkStatus();
        logger.info("Apply suspend job: {}.", jobId);
    }

    @Override
    public void remove(long jobId) {
        checkStatus();

        logger.info("Apply remove job: {}...", jobId);
        jobExecutor.remove(jobId);
    }

    @Override
    public Map<Long, SubmittedJob> snapshot() {
        return MapUtils.unmodifiableMap(jobExecutor.getAllSubmittedJobs());
    }

    @Override
    public SubmittedJob getSubmittedJob(long jobId) {
        SubmittedJob submittedJob = jobExecutor.getAllSubmittedJobs().get(jobId);
        if (submittedJob == null) {
            throw new ScheduleException("Job " + jobId + " is not been submitted yet.", jobId);
        }
        return submittedJob;
    }

    private void checkStatus() {
        try {
            startedLatch.await();
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    @Override
    public void start() {
        logger.info("Start scheduler...");
        jobExecutor.start();
        ServerContext.getInstance().getJobFailover().start();
        logger.info("Start scheduler successful.");
    }

    @Override
    public void stop() {
        logger.info("Stop scheduler...");
        jobExecutor.stop();
        ServerContext.getInstance().getJobFailover().stop();
        logger.info("Stop scheduler successful.");
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MasterChangedEvent) {
            onMasterChange((MasterChangedEvent) event);
        } else if (event instanceof JobEvent) {
            onJobEvent((JobEvent) event);
        }
    }

    private void onMasterChange(MasterChangedEvent event) {
        Node masterNode = event.getMasterNode();
        if (ServerContext.getInstance().thisNode().equals(masterNode)) { //Only master should start scheduler when system bootstrapped
            logger.info("This node has been elected as a master. Start scheduler...");
            start();
            startedLatch.countDown();
            scheduleExistJobs();
        } else {
            stop();
            startedLatch = new CountDownLatch(1);
        }
    }

    private void scheduleExistJobs() {
        try {
            List<Job> jobs = jobManager.getAvailableJobs();
            for (Job job : jobs) {
                scheduleExistJob(job);
            }
        } catch (Exception e) {
            logger.error("Failed to schedule exist jobs.", e);
            Throwables.propagate(e);
        }
    }

    private void scheduleExistJob(Job job) {
        ScheduledJob lastSchedule = scheduledJobDao.findLastScheduleByJobId(job.getId());
        if (lastSchedule == null) {
            this.submit(job);
            return;
        }

        switch (lastSchedule.getStatus()) {
            case WAITING:
                handleWaitingCase(job, lastSchedule);
                break;
            case RUNNING:
                handleRunningCase(job, lastSchedule);
                break;
            case SUCCESS:
            case PARTIAL_SUCCESS:
            case FAILED:
            case FORCE_STOP:
            case CANCELED:
                handleDoneCase(job, lastSchedule);
                break;
        }
    }

    private void handleWaitingCase(Job job, ScheduledJob lastSchedule) {
        scheduledJobDao.cancel(lastSchedule.getId());
        this.submit(job);
    }

    private void handleRunningCase(Job job, ScheduledJob lastSchedule) {
        // TODO strategy
    }

    private void handleDoneCase(Job job, ScheduledJob lastSchedule) {
        if (job instanceof OneShotJob) {
            return;
        } else if (job instanceof PeriodicJob) {
            PeriodicJob periodicJob = (PeriodicJob) job;
            Date startTime = lastSchedule.getStartTime();
            long elapse = TimeUtils.getInterval(startTime, new Date(), TimeUnit.SECONDS);
            if (elapse > periodicJob.getPeriodicInterval()) {
                periodicJob.setStartTime(Job.INSTANT);
            } else {
                periodicJob.setStartTime(TimeUtils.futureDate(startTime, periodicJob.getPeriodicInterval(), TimeUnit.SECONDS));
            }
            this.submit(job);
        }
    }

    private void determineStartTimeForPeriodicJob(PeriodicJob periodicJob, ScheduledJob lastSchedule) {
        Date startTime = lastSchedule.getStartTime();
        long elapse = TimeUtils.getInterval(startTime, new Date(), TimeUnit.SECONDS);
        if (elapse > periodicJob.getPeriodicInterval()) {
            periodicJob.setStartTime(Job.INSTANT);
        } else {
            periodicJob.setStartTime(TimeUtils.futureDate(startTime, periodicJob.getPeriodicInterval(), TimeUnit.SECONDS));
        }
    }

    private void onJobEvent(JobEvent event) {
        Job job = (Job) event.source();
        switch (event.getCode()) {
            case ENABLE:
                handleJobEnabled(job);
                break;
            case UPDATE:
                handleJobUpdated(job);
                break;
            case DISABLE:
            case REMOVE:
                handleJobRemoved(job);
                break;
            default:
                break;
        }
    }

    private void handleJobUpdated(Job job) {
        if (!jobExecutor.isSubmitted(job.getId())) {    //not submitted yet
            return;
        }

        logger.info("Job [{}] is updated, try to re-submit it...", job.getId());
        try {
            jobExecutor.cancelUntilIdle(job.getId());
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }

        ScheduledJob lastSchedule = scheduledJobDao.findLastScheduleByJobId(job.getId());
        if (lastSchedule == null) { //not been executed yet
            submit(job);
        } else {    // has been executed
            if (job instanceof OneShotJob) {
                logger.info("One-shot job [{}] has been executed. Ignore its update event.");
                return;
            } else if (job instanceof PeriodicJob) {
                determineStartTimeForPeriodicJob((PeriodicJob) job, lastSchedule);
                submit(job);
            }
        }
    }

    private void handleJobEnabled(Job job) {
        logger.info("Job [{}] is enabled, try to submit it...", job.getId());
        submit(job);
    }

    private void handleJobRemoved(Job job) {
        logger.info("Job [{}] is removed, try to remove it...", job.getId());
        remove(job.getId());
    }

}
