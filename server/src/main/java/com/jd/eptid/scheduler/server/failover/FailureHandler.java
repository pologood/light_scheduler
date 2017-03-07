package com.jd.eptid.scheduler.server.failover;

import com.jd.eptid.scheduler.core.domain.job.Job;

/**
 * Created by classdan on 17-1-20.
 */
public interface FailureHandler {

    void handle(Job job);

}
