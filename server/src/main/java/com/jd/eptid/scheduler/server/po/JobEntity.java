package com.jd.eptid.scheduler.server.po;

import java.util.Date;

/**
 * Created by classdan on 17-1-3.
 */
public class JobEntity {
    private Long id;
    private String name;
    private String description;
    private Date startTime;
    private Integer executeInterval;
    private boolean concurrentExecution;
    private String mutexJobs;
    private Integer failoverPolicy;
    private Date createTime;
    private Date updateTime;
    private boolean available;

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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Integer getExecuteInterval() {
        return executeInterval;
    }

    public void setExecuteInterval(Integer executeInterval) {
        this.executeInterval = executeInterval;
    }

    public boolean isConcurrentExecution() {
        return concurrentExecution;
    }

    public void setConcurrentExecution(boolean concurrentExecution) {
        this.concurrentExecution = concurrentExecution;
    }

    public String getMutexJobs() {
        return mutexJobs;
    }

    public void setMutexJobs(String mutexJobs) {
        this.mutexJobs = mutexJobs;
    }

    public Integer getFailoverPolicy() {
        return failoverPolicy;
    }

    public void setFailoverPolicy(Integer failoverPolicy) {
        this.failoverPolicy = failoverPolicy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
