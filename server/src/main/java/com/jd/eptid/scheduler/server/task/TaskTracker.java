package com.jd.eptid.scheduler.server.task;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.task.ScheduledTask;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.domain.task.TaskStatus;
import com.jd.eptid.scheduler.core.event.ClientEvent;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.listener.EventListener;
import com.jd.eptid.scheduler.core.response.TaskResponse;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import com.jd.eptid.scheduler.server.core.ServerContext;
import com.jd.eptid.scheduler.server.dao.ScheduledTaskDao;
import com.jd.eptid.scheduler.server.job.JobContext;
import com.jd.eptid.scheduler.server.network.ChannelHolder;
import com.jd.eptid.scheduler.server.network.ServerTransport;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-9-9.
 */
public class TaskTracker implements EventListener<ClientEvent> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServerTransport transport;
    private ScheduledTaskDao scheduledTaskDao;
    private JobContext jobContext;
    private String scheduleId;
    private TaskConfig taskConfig;
    private Client taskClient;
    private ScheduledTask scheduledTask;
    private Thread currentThread;

    public TaskTracker(JobContext jobContext, String scheduleId, Client taskClient, TaskConfig taskConfig) {
        this.jobContext = jobContext;
        this.scheduleId = scheduleId;
        this.taskClient = taskClient;
        this.taskConfig = taskConfig;
        transport = ServerContext.getInstance().getServerTransport();
        scheduledTaskDao = ServerContext.getInstance().getScheduledTaskDao();
        ServerContext.getInstance().getEventBroadcaster().register(ClientEvent.class, this);
        currentThread = Thread.currentThread();
    }

    public void track() throws Exception {
        try {
            prepare();

            runTask();
        } catch (Exception e) {
            updateStatus(TaskStatus.FAILED, e.getMessage());
            throw e;
        } finally {
            ServerContext.getInstance().getEventBroadcaster().unregister(ClientEvent.class, this);
        }
    }

    private void prepare() {
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.setJobScheduleId(jobContext.getStoreId());
        scheduledTask.setScheduleId(scheduleId);
        if (taskConfig.getParam() != null) {
            scheduledTask.setData(JSON.toJSONString(taskConfig.getParam()));
        }
        scheduledTask.setClientIp(taskClient.getIp());
        scheduledTask.setStatus(TaskStatus.RUNNING);
        int affected = scheduledTaskDao.add(scheduledTask);
        if (affected <= 0) {
            throw new ScheduleException("Failed to add scheduled task record. {}.", taskConfig);
        }
        this.scheduledTask = scheduledTask;
    }

    private void updateStatus(TaskStatus newStatus, String message) {
        int affected = 0;
        switch (newStatus) {
            case RUNNING:
                affected = scheduledTaskDao.start(scheduledTask.getId(), scheduledTask.getClientIp());
                break;
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
        if (taskResponse == null) {
            throw new ScheduleException("Failed to send task to client.", taskClient, taskConfig);
        }

        if (taskResponse.isSuccess()) {   // task execute success
            logger.info("Task {} execute successful. Task client: {}.", taskConfig, taskClient);
            updateStatus(TaskStatus.SUCCESS, null);
        } else {    // task execute failed
            logger.error("Task {} execute failed. Task client: {}. Cause: {}.", taskConfig, taskClient, taskResponse.getMessage());
            throw new ScheduleException(taskResponse.getMessage());
        }
    }

    private TaskResponse sendTask() throws Exception {
        Message message = packetRequest();

        Channel clientChannel = ChannelHolder.findChannel(taskClient);
        if (clientChannel == null) {
            throw new ScheduleException("Channel not exists, the client may has disconnect from the server.");
        }

        try {
            Message response = transport.send(clientChannel, message, Configuration.getInteger(ConfigItem.TASK_RUN_TIMEOUT, 5), TimeUnit.MINUTES);
            return JSON.parseObject(response.getContent(), TaskResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    private Message packetRequest() {
        Message message = new Message();
        message.setType(MessageType.Task_Run.getCode());
        message.setContent(JSON.toJSONString(taskConfig));
        return message;
    }

    @Override
    public void onEvent(ClientEvent event) {
        Client client = (Client) event.source();
        Assert.notNull(client);
        if (client.equals(taskClient) && event.getCode() == ClientEvent.Code.REMOVED) {
            if (currentThread.getState() == Thread.State.TIMED_WAITING) {
                currentThread.interrupt();
            }
        }
    }
}
