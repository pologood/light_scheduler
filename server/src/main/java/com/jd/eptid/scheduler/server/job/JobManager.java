package com.jd.eptid.scheduler.server.job;


import com.jd.eptid.scheduler.core.domain.job.Job;

import java.util.List;

/**
 * Created by ClassDan on 2016/9/18.
 */
public interface JobManager {

    long addJob(Job job);

    void update(long id, Job newJob);

    void removeJob(long id);

    void disable(long id);

    void enable(long id);

    int count();

    Job findJob(long id);

    List<Job> getAllJobs();

    List<Job> getAvailableJobs();

}
