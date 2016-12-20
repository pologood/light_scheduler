package com.jd.eptid.scheduler.core.response;

import com.jd.eptid.scheduler.core.domain.task.TaskConfig;

import java.util.List;

/**
 * Created by classdan on 16-10-17.
 */
public class JobSplitResponse {
    private boolean success;
    private String message;
    private String jobName;
    private boolean isLast;
    private List<TaskConfig> taskConfigs;

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

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public List<TaskConfig> getTaskConfigs() {
        return taskConfigs;
    }

    public void setTaskConfigs(List<TaskConfig> taskConfigs) {
        this.taskConfigs = taskConfigs;
    }

    public static JobSplitResponse buildFailed(String errorMessage) {
        JobSplitResponse response = new JobSplitResponse();
        response.setSuccess(false);
        response.setMessage(errorMessage);
        return response;
    }

}
