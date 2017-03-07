package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.common.LifeCycle;

import java.util.Map;

/**
 * Created by classdan on 16-9-21.
 */
public interface JobExecutor extends LifeCycle {

    void execute(SubmittedJob job);

    void cancel(long jobId) throws InterruptedException;

    void cancel(long jobId, String scheduleId) throws InterruptedException;

    void cancelUntilIdle(long jobId) throws InterruptedException;

    void remove(long jobId);

    boolean isSubmitted(long jobId);

    Map<Long, SubmittedJob> getAllSubmittedJobs();
}
