package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.statistics.JobStatistics;
import io.netty.util.internal.ConcurrentSet;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by classdan on 16-9-22.
 */
public class JobContext {
    private Job job;
    private Client jobClient;
    private long scheduleId;
    private int splitTimes = 0;
    private ConcurrentSet<Client> taskClients = new ConcurrentSet<Client>();
    private volatile CountDownLatch allTaskFinishedLatch = new CountDownLatch(1);
    private volatile JobStatistics jobStatistics = new JobStatistics();

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Client getJobClient() {
        return jobClient;
    }

    public void setJobClient(Client jobClient) {
        this.jobClient = jobClient;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Set<Client> getTaskClients() {
        return taskClients;
    }

    public void addTaskClient(Client taskClient) {
        taskClients.add(taskClient);
    }

    public void removeTaskClient(Client taskClient) {
        taskClients.remove(taskClient);
    }

    public int getSplitTimes() {
        return splitTimes;
    }

    public void incrSplitTimes() {
        ++splitTimes;
    }

    public CountDownLatch getAllTaskFinishedLatch() {
        return allTaskFinishedLatch;
    }

    public JobStatistics getJobStatistics() {
        return jobStatistics;
    }
}
