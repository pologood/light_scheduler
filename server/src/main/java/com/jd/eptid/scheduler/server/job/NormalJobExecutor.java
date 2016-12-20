package com.jd.eptid.scheduler.server.job;


import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.job.RealTimeJob;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * Created by classdan on 16-9-19.
 */
@Component
public class NormalJobExecutor implements JobExecutor {
    @Override
    public boolean supports(Job job) {
        return job instanceof RealTimeJob;
    }

    @Override
    public Future execute(Job job) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
