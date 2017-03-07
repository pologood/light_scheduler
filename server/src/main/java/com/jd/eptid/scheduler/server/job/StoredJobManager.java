package com.jd.eptid.scheduler.server.job;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jd.eptid.scheduler.core.domain.job.PeriodicJob;
import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.job.OneShotJob;
import com.jd.eptid.scheduler.core.event.JobEvent;
import com.jd.eptid.scheduler.core.failover.FailoverPolicy;
import com.jd.eptid.scheduler.server.core.ServerContext;
import com.jd.eptid.scheduler.server.dao.JobDao;
import com.jd.eptid.scheduler.server.po.JobEntity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ClassDan on 2016/9/18.
 */
@Component
public class StoredJobManager implements JobManager {
    @Resource
    private JobDao jobDao;

    @Override
    public long addJob(Job job) {
        JobEntity jobEntity = convert(job);
        jobDao.add(jobEntity);
        ServerContext.getInstance().getEventBroadcaster().publish(new JobEvent(job, JobEvent.Code.NEW));
        return jobEntity.getId();
    }

    @Override
    public void update(long id, Job newJob) {
        Job job = findJob(id);
        if (job == null) {
            throw new IllegalArgumentException("Job [" + id + "] not found.");
        }

        int effected = jobDao.update(convert(newJob));
        if (effected > 0) {
            ServerContext.getInstance().getEventBroadcaster().publish(new JobEvent(newJob, JobEvent.Code.UPDATE));
        }
    }

    private JobEntity convert(Job job) {
        JobEntity jobEntity = new JobEntity();
        jobEntity.setId(job.getId());
        jobEntity.setName(job.getName());
        jobEntity.setDescription(job.getDescription());
        if (CollectionUtils.isNotEmpty(job.getMutexJobIds())) {
            jobEntity.setMutexJobs(JSON.toJSONString(job.getMutexJobIds()));
        }
        if (job.getFailoverPolicy() != null) {
            jobEntity.setFailoverPolicy(job.getFailoverPolicy().getCode());
        }
        if (job instanceof PeriodicJob) {
            PeriodicJob periodicJob = (PeriodicJob) job;
            jobEntity.setStartTime(periodicJob.getStartTime());
            jobEntity.setExecuteInterval(periodicJob.getPeriodicInterval());
            jobEntity.setConcurrentExecution(periodicJob.isAllowConcurrent());
        }
        return jobEntity;
    }

    @Override
    public void removeJob(long id) {
        Job job = findJob(id);
        if (job == null) {
            throw new IllegalArgumentException("Job [" + id + "] not found.");
        }

        int effected = jobDao.delete(id);
        if (effected > 0) {
            ServerContext.getInstance().getEventBroadcaster().publish(new JobEvent(job, JobEvent.Code.REMOVE));
        }
    }

    @Override
    public void disable(long id) {
        updateStatus(id, false);
    }

    @Override
    public void enable(long id) {
        updateStatus(id, true);
    }

    private void updateStatus(long id, boolean isEnable) {
        Job job = findJob(id);
        if (job == null) {
            throw new IllegalArgumentException("Job [" + id + "] not found.");
        }
        boolean check = isEnable ? job.isAvailable() : !job.isAvailable();
        if (check) {
            throw new IllegalArgumentException("Job [" + id + "] is already " + (isEnable ? "enabled" : "disabled") + ".");
        }

        int effected = jobDao.updateStatus(id, isEnable ? 1 : 0);
        if (effected > 0) {
            ServerContext.getInstance().getEventBroadcaster().publish(new JobEvent(job, isEnable ? JobEvent.Code.ENABLE : JobEvent.Code.DISABLE));
        }
    }

    @Override
    public int count() {
        return jobDao.count();
    }

    @Override
    public Job findJob(long id) {
        JobEntity jobEntity = jobDao.getJobById(id);
        return convert(jobEntity);
    }

    @Override
    public List<Job> getAllJobs() {
        List<JobEntity> jobEntities = jobDao.getAllJobs();
        List<Job> jobs = new ArrayList<Job>();
        for (JobEntity entity : jobEntities) {
            jobs.add(convert(entity));
        }
        return jobs;
    }

    private Job convert(JobEntity entity) {
        Job job = null;
        if (entity.getExecuteInterval() != null) {
            PeriodicJob periodicJob = new PeriodicJob();
            periodicJob.setStartTime(entity.getStartTime());
            periodicJob.setPeriodicInterval(entity.getExecuteInterval());
            periodicJob.setAllowConcurrent(entity.isConcurrentExecution());
            job = periodicJob;
        } else {
            OneShotJob oneShotJob = new OneShotJob();
            job = oneShotJob;
        }
        job.setId(entity.getId());
        job.setName(entity.getName());
        job.setDescription(entity.getDescription());
        if (!StringUtils.isBlank(entity.getMutexJobs())) {
            job.setMutexJobIds(JSON.parseObject(entity.getMutexJobs(), new TypeReference<LinkedList<Long>>() {
            }));
        }
        if (entity.getFailoverPolicy() != null) {
            job.setFailoverPolicy(FailoverPolicy.getFailoverPolicy(entity.getFailoverPolicy()));
        }
        job.setCreateTime(entity.getCreateTime());
        job.setUpdateTime(entity.getUpdateTime());
        job.setAvailable(entity.isAvailable());
        return job;
    }

    @Override
    public List<Job> getAvailableJobs() {
        List<JobEntity> jobEntities = jobDao.getAvailableJobs();
        List<Job> jobs = new ArrayList<Job>();
        for (JobEntity entity : jobEntities) {
            jobs.add(convert(entity));
        }
        return jobs;
    }
}
