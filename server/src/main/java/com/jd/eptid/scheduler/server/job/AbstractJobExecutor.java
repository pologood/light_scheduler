package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.common.ShutdownHook;
import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.event.ScheduleEvent;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.listener.EventListener;
import com.jd.eptid.scheduler.server.core.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by classdan on 17-1-18.
 */
public abstract class AbstractJobExecutor implements JobExecutor, EventListener<ScheduleEvent<Job>> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected Map<Long, SubmittedJob> submittedJobs = new ConcurrentHashMap<Long, SubmittedJob>();

    public AbstractJobExecutor() {
        ShutdownHook.getInstance().addLifeCycleObject(this);
        ServerContext.getInstance().getEventBroadcaster().register(ScheduleEvent.class, this);
    }

    @Override
    public void execute(SubmittedJob submittedJob) {
        Job job = submittedJob.getJob();
        Assert.notNull(job.getId());

        SubmittedJob existedSubmittedJob = submittedJobs.get(job.getId());
        if (existedSubmittedJob != null) {
            throw new ScheduleException("Job [" + job.getId() + "] is already submitted.");
        }

        doExecute(submittedJob);
    }

    private void doExecute(SubmittedJob submittedJob) {
        Future future = doJob(new TrackTask(submittedJob));
        submittedJob.setFuture(future);
        submittedJobs.put(submittedJob.getJob().getId(), submittedJob);
    }

    protected abstract Future doJob(TrackTask trackTask);

    @Override
    public void cancel(long jobId) throws InterruptedException {
        SubmittedJob submittedJob = findSubmittedJob(jobId);
        submittedJob.setRemoved(true);
        for (JobTracker jobTracker : submittedJob.getJobTrackers().values()) {
            jobTracker.cancel();
        }

        doCancel(submittedJob);
    }

    private void doCancel(SubmittedJob submittedJob) {
        boolean isCanceled = submittedJob.getFuture().cancel(false);
        if (isCanceled) {
            removeSubmittedJob(submittedJob);
        } else {
            throw new ScheduleException("Failed to cancel the job.", submittedJob.getJob().getId());
        }
    }

    @Override
    public void cancel(long jobId, String scheduleId) throws InterruptedException {
        SubmittedJob submittedJob = findSubmittedJob(jobId);
        if (submittedJob.getJobTracker(scheduleId) != null) {
            submittedJob.getJobTracker(scheduleId).cancel();
        }
    }

    @Override
    public void cancelUntilIdle(long jobId) throws InterruptedException {
        SubmittedJob submittedJob = submittedJobs.get(jobId);
        if (submittedJob == null) {
            return;
        }
        if (!submittedJob.isRunning()) {
            submittedJob.getFuture().cancel(true);
            return;
        }

        submittedJob.setRemoved(true);
        submittedJob.waitForDone();
        doCancel(submittedJob);
    }

    private SubmittedJob findSubmittedJob(long jobId) {
        SubmittedJob submittedJob = submittedJobs.get(jobId);
        if (submittedJob == null) {
            throw new IllegalArgumentException("Job [" + jobId + "] is not submitted.");
        }
        return submittedJob;
    }

    @Override
    public boolean isSubmitted(long jobId) {
        return submittedJobs.get(jobId) != null;
    }

    @Override
    public Map<Long, SubmittedJob> getAllSubmittedJobs() {
        return submittedJobs;
    }

    @Override
    public void remove(long jobId) {
        SubmittedJob submittedJob = submittedJobs.get(jobId);
        if (submittedJob == null) {
            return;
        }

        submittedJob.setRemoved(true);
        doCancel(submittedJob);
    }

    protected void removeSubmittedJob(SubmittedJob submittedJob) {
        submittedJob.removeAllJobTrackers();
        submittedJobs.remove(submittedJob.getJob().getId());
    }

    protected class TrackTask implements Runnable {
        private SubmittedJob submittedJob;

        TrackTask(SubmittedJob submittedJob) {
            this.submittedJob = submittedJob;
        }

        Job getJob() {
            return this.submittedJob.getJob();
        }

        @Override
        public void run() {
            Job job = submittedJob.getJob();
            String scheduleId = UUID.randomUUID().toString();
            logger.info("Start to execute job: {}. scheduleId: {}.", job, scheduleId);
            try {
                checkMutexCondition(job);

                JobContext context = new JobContext();
                context.setScheduleId(scheduleId);
                context.setJob(job);
                context.setRetry(submittedJob.isRetry());

                JobTracker jobTracker = new JobTracker(context);
                submittedJob.addJobTracker(scheduleId, jobTracker);
                jobTracker.track();
            } catch (Exception e) {
                logger.error("Failed to execute job: {}.", job.getId(), e);
                ServerContext.getInstance().getJobFailover().fail(job);
            }
        }
    }

    private void checkMutexCondition(Job job) throws InterruptedException, TimeoutException {
        List<Long> mutexJobIds = job.getMutexJobIds();
        for (Long jobId : mutexJobIds) {
            SubmittedJob submittedJob = submittedJobs.get(jobId);
            if (submittedJob != null) {
                submittedJob.waitForDone(10, TimeUnit.MINUTES);
            }
        }
    }

    @Override
    public void onEvent(ScheduleEvent<Job> event) {
        Job job = event.source();
        String scheduleId = event.getScheduleId();
        logger.info("ScheduleEvent: {}, {}.", job, scheduleId);
        SubmittedJob submittedJob = submittedJobs.get(job.getId());
        if (submittedJob == null) {
            logger.warn("SubmittedJob for job[" + job.getId() + "] not found.");
            return;
        }

        switch (event.getCode()) {
            case DONE:
                onJobDone(job.getId(), scheduleId);
                break;
            default:
                break;
        }
    }

    protected abstract void onJobDone(long jobId, String scheduleId);

}
