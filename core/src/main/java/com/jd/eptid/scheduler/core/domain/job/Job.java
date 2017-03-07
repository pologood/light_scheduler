package com.jd.eptid.scheduler.core.domain.job;

import com.jd.eptid.scheduler.core.failover.FailoverPolicy;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by classdan on 16-9-7.
 */
public abstract class Job {
    public static final Date INSTANT = null;
    private Long id;
    private String name;
    private String description;
    private List<Long> mutexJobIds = new LinkedList<Long>();
    private FailoverPolicy failoverPolicy = FailoverPolicy.NONE;
    private Date startTime = INSTANT;
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

    public List<Long> getMutexJobIds() {
        return mutexJobIds;
    }

    public void setMutexJobIds(List<Long> mutexJobIds) {
        this.mutexJobIds = mutexJobIds;
    }

    public FailoverPolicy getFailoverPolicy() {
        return failoverPolicy;
    }

    public void setFailoverPolicy(FailoverPolicy failoverPolicy) {
        this.failoverPolicy = failoverPolicy;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        return name.equals(job.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
