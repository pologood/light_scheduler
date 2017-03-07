package com.jd.eptid.scheduler.server.web.request;

import java.util.Date;

/**
 * Created by classdan on 17-1-3.
 */
public class JobSaveRequest {
    private Long id;
    private String name;
    private String description;
    private boolean cron;
    private Date startTime;
    private int interval;
    private Long[] mutexJobIds;
    private String failureStrategy;
    private boolean allowConcurrent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCron() {
        return cron;
    }

    public void setCron(boolean cron) {
        this.cron = cron;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public Long[] getMutexJobIds() {
        return mutexJobIds;
    }

    public void setMutexJobIds(Long[] mutexJobIds) {
        this.mutexJobIds = mutexJobIds;
    }

    public String getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(String failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public boolean isAllowConcurrent() {
        return allowConcurrent;
    }

    public void setAllowConcurrent(boolean allowConcurrent) {
        this.allowConcurrent = allowConcurrent;
    }
}
