package com.jd.eptid.scheduler.core.domain.task;

import com.jd.eptid.scheduler.core.domain.node.Client;

public class TaskClient {
    private String scheduleId;
    private TaskConfig taskConfig;
    private Client client;

    public TaskClient(String scheduleId, TaskConfig taskConfig, Client client) {
        this.scheduleId = scheduleId;
        this.taskConfig = taskConfig;
        this.client = client;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public TaskConfig getTaskConfig() {
        return taskConfig;
    }

    public void setTaskConfig(TaskConfig taskConfig) {
        this.taskConfig = taskConfig;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}