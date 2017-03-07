package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.common.LifeCycle;
import com.jd.eptid.scheduler.core.domain.job.Job;

import java.util.Map;

/**
 * Created by classdan on 16-9-7.
 */
public interface JobScheduler extends LifeCycle {

    /**
     * Submit a job.
     *
     * @param job a job
     */
    void submit(Job job);

    /**
     * Re-submit a job. Only provide for internal service. Usually used to submit a retry job.
     *
     * @param job a job
     */
    void resubmit(Job job);

    /**
     * Cancel a job.
     *
     * @param jobId job id
     * @throws com.jd.eptid.scheduler.core.exception.ScheduleException cancel failed
     */
    void cancel(long jobId);

    /**
     * Suspend a job.
     *
     * @param jobId job id
     */
    void suspend(long jobId);

    /**
     * Remove a job from the schedule queue.
     * When the job is running, it will wait for the job to complete.
     *
     * @param jobId job id
     * @throws com.jd.eptid.scheduler.core.exception.ScheduleException remove failed
     */
    void remove(long jobId);

    /**
     * Generate a snapshot to report all job's schedule status.
     *
     * @return the mapping of job id and submittedJob
     */
    Map<Long, SubmittedJob> snapshot();

    /**
     * Get submitted job object of the specify job.
     *
     * @param jobId job id
     * @return the submitted job object
     */
    SubmittedJob getSubmittedJob(long jobId);

}
