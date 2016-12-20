package com.jd.eptid.scheduler.server.job;


import com.jd.eptid.scheduler.core.domain.job.Job;

import java.util.List;

/**
 * Created by ClassDan on 2016/9/18.
 */
public interface JobManager {

    void addJob(Job job);

    void removeJob(String id);

    void disable(String id);

    int count();

    List<Job> getAllJobs();

    List<Job> getAvailableJobs();

}
