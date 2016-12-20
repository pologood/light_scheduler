package com.jd.eptid.scheduler.server.task;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.core.domain.job.Status;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.task.ScheduledTask;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.response.TaskResponse;
import com.jd.eptid.scheduler.server.core.AppContext;
import com.jd.eptid.scheduler.server.dao.ScheduledTaskDao;
import com.jd.eptid.scheduler.server.job.JobContext;
import com.jd.eptid.scheduler.server.network.ChannelHolder;
import com.jd.eptid.scheduler.server.network.ServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-9-9.
 */
public class TaskTracker {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServerTransport transport;
    private ScheduledTaskDao scheduledTaskDao;
    private JobContext jobContext;
    private TaskConfig taskConfig;
    private Client taskClient;
    private ScheduledTask scheduledTask;

    public TaskTracker(JobContext jobContext, Client taskClient, TaskConfig taskConfig) {
        this.jobContext = jobContext;
        this.taskClient = taskClient;
        this.taskConfig = taskConfig;
        transport = AppContext.getInstance().getServerTransport();
        scheduledTaskDao = AppContext.getInstance().getScheduledTaskDao();
    }

    public void track() throws Exception {
        try {
            prepare();

            runTask();
        } catch (Exception e) {
            logger.error("Failed to track task. job:{}, task:{}.", jobContext.getJob(), taskConfig, e);
            updateStatus(Status.FAILED, e.getMessage());
            throw e;
        }
    }

    private void prepare() {
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.setJobScheduleId(jobContext.getScheduleId());
        if (taskConfig.getParam() != null) {
            scheduledTask.setData(JSON.toJSONString(taskConfig.getParam()));
        }
        scheduledTask.setClientIp(taskClient.getIp());
        scheduledTask.setStatus(Status.RUNNING);
        int affected = scheduledTaskDao.add(scheduledTask);
        if (affected <= 0) {
            throw new ScheduleException("Failed to add scheduled task record. {}.", taskConfig);
        }
        this.scheduledTask = scheduledTask;
    }

    private void updateStatus(Status newStatus, String message) {
        int affected = 0;
        switch (newStatus) {
            case RUNNING:
                affected = scheduledTaskDao.start(scheduledTask.getId(), scheduledTask.getClientIp());
                break;
            case PARTIAL_SUCCESS:
            case SUCCESS:
                affected = scheduledTaskDao.success(scheduledTask.getId());
                break;
            case FAILED:
                affected = scheduledTaskDao.fail(scheduledTask.getId(), message);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported status: " + newStatus);
        }

        if (affected <= 0) {
            throw new IllegalStateException("Failed to update status of scheduled task: " + scheduledTask.getId() + ", new status: " + newStatus);
        }
    }

    private void runTask() throws Exception {
        TaskResponse taskResponse = sendTask();
        if (taskResponse.isSuccess()) {   // task execute success
            logger.info("Task {} execute successful. Task client: {}.", taskConfig, taskClient);
            updateStatus(Status.SUCCESS, null);
        } else {    // task execute failed
            logger.error("Task {} execute failed. Task client: {}. Cause: {}.", taskConfig, taskClient, taskResponse.getMessage());
            throw new ScheduleException(taskResponse.getMessage());
        }
    }

    private TaskResponse sendTask() throws Exception {
        Message message = new Message();
        message.setType(MessageType.Task_Run.getCode());
        message.setContent(JSON.toJSONString(taskConfig));

        Message response = null;
        try {
            response = transport.send(ChannelHolder.findChannel(taskClient), message, 3, TimeUnit.MINUTES);
        } catch (Exception e) {
            logger.error("Failed to send task to task client: {}, splitContext: {}.", taskClient, taskConfig);
            throw e;
        }

        return JSON.parseObject(response.getContent(), TaskResponse.class);
    }
}
