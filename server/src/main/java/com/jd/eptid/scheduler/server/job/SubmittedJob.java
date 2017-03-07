package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.domain.job.Job;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A dynamic view of scheduled job
 * Created by classdan on 17-1-10.
 */
public class SubmittedJob {
    private Job job;
    private boolean isRetry = false;
    private boolean isRemoved = false;
    private Future future;
    /**
     * Record jobTrackers for each scheduling. For a normal job, usually has only one jobTracker, but for a cron job, it may has many jobTrackers.
     * scheduleId --> jobTracker
     */
    private Map<String, JobTracker> jobTrackers = new HashMap<String, JobTracker>();

    public SubmittedJob(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    public Future getFuture() {
        return future;
    }

    public void setFuture(Future future) {
        this.future = future;
    }

    public Map<String, JobTracker> getJobTrackers() {
        return jobTrackers;
    }

    public JobTracker getJobTracker(String scheduleId) {
        return jobTrackers.get(scheduleId);
    }

    public synchronized void addJobTracker(String scheduleId, JobTracker jobTracker) {
        jobTrackers.put(scheduleId, jobTracker);
    }

    public synchronized void removeJobTracker(String scheduleId) {
        if (jobTrackers.containsKey(scheduleId)) {
            jobTrackers.remove(scheduleId);
            notifyAll();
        }
    }

    public synchronized void removeAllJobTrackers() {
        jobTrackers.clear();
        notifyAll();
    }

    public JobContext getJobContext(String scheduleId) {
        JobTracker jobTracker = jobTrackers.get(scheduleId);
        if (jobTracker == null) {
            return null;
        }
        return jobTracker.getJobContext();
    }

    public synchronized boolean isRunning() {
        return MapUtils.isNotEmpty(jobTrackers);
    }

    public synchronized void waitForDone() throws InterruptedException {
        while (!jobTrackers.isEmpty()) {
            wait();
        }
    }

    public synchronized void waitForDone(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        boolean hasWait = false;
        while (!jobTrackers.isEmpty()) {
            if (hasWait) {
                throw new TimeoutException("Wait for job [" + job.getId() + "] done timeout.");
            }
            wait(timeUnit.toMillis(timeout));
            hasWait = true;
        }
    }

}
