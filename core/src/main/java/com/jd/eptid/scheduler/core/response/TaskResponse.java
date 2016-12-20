package com.jd.eptid.scheduler.core.response;

import java.util.Date;

/**
 * Created by classdan on 16-9-28.
 */
public class TaskResponse {
    private boolean success;
    private String message;
    private Date startTime;
    private Date endTime;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public static TaskResponse buildFailed(String errorMessage) {
        TaskResponse response = new TaskResponse();
        response.setSuccess(false);
        response.setMessage(errorMessage);
        return response;
    }

}
