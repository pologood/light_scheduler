package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.server.core.LifeCycle;

/**
 * Created by classdan on 16-9-7.
 */
public interface JobScheduler extends LifeCycle {

    void submit(Job job);

    void cancel(long submittedJobId);

    void suspend(long submittedJobId);

}
