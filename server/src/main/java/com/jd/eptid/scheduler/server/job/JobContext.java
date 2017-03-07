package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.task.TaskClient;
import com.jd.eptid.scheduler.core.statistics.JobStatistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by classdan on 16-9-22.
 */
public class JobContext {
    private String scheduleId;
    private Job job;
    private boolean isRetry;
    private Client jobClient;
    private long storeId;
    private int splitTimes = 0;
    private ConcurrentMap<String, TaskClient> taskClients = new ConcurrentHashMap<String, TaskClient>();
    private volatile CountDownLatch allTaskFinishedLatch = new CountDownLatch(1);
    private volatile JobStatistics jobStatistics = new JobStatistics();
    private volatile boolean enableMonitorLog = false;

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public Client getJobClient() {
        return jobClient;
    }

    public void setJobClient(Client jobClient) {
        this.jobClient = jobClient;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public Map<String, TaskClient> getTaskClients() {
        return taskClients;
    }

    public void addTaskClient(String scheduleId, TaskClient taskClient) {
        taskClients.put(scheduleId, taskClient);
    }

    public void removeTaskClient(String scheduleId) {
        taskClients.remove(scheduleId);
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

    public void enableMonitorLog() {
        this.enableMonitorLog = true;
    }

    public boolean isEnableMonitorLog() {
        return enableMonitorLog;
    }
}
