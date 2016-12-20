package com.jd.eptid.scheduler.core.domain.task;

import com.jd.eptid.scheduler.core.domain.job.Status;

import java.util.Date;

/**
 * Created by classdan on 16-9-7.
 */
public class ScheduledTask {
    private long id;
    private long jobScheduleId;
    private String data;
    private String clientIp;
    private Status status;
    private String errorMessage;
    private Date createTime;
    private Date startTime;
    private Date endTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getJobScheduleId() {
        return jobScheduleId;
    }

    public void setJobScheduleId(long jobScheduleId) {
        this.jobScheduleId = jobScheduleId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
