package com.jd.eptid.scheduler.server.job;


import com.jd.eptid.scheduler.core.domain.job.Job;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Created by ClassDan on 2016/9/18.
 */
@Component
public class StoredJobManager implements JobManager {
    @Override
    public void addJob(Job job) {

    }

    @Override
    public void removeJob(String id) {

    }

    @Override
    public void disable(String id) {

    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public List<Job> getAllJobs() {
        return Collections.emptyList();
    }

    @Override
    public List<Job> getAvailableJobs() {
        return Collections.emptyList();
    }
}
