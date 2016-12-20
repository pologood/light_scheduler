package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.server.core.LifeCycle;

import java.util.concurrent.Future;

/**
 * Created by classdan on 16-9-21.
 */
public interface JobExecutor extends LifeCycle {

    boolean supports(Job job);

    Future execute(Job job);

}
