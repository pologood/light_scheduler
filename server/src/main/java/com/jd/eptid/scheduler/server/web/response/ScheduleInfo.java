package com.jd.eptid.scheduler.server.web.response;

import com.jd.eptid.scheduler.core.domain.task.TaskClient;

import java.util.Collection;

/**
 * Created by classdan on 17-2-7.
 */
public class ScheduleInfo {
    private String jobClient;
    private Collection<TaskClient> taskClients;

    public String getJobClient() {
        return jobClient;
    }

    public void setJobClient(String jobClient) {
        this.jobClient = jobClient;
    }

    public Collection<TaskClient> getTaskClients() {
        return taskClients;
    }

    public void setTaskClients(Collection<TaskClient> taskClients) {
        this.taskClients = taskClients;
    }
}
