package com.jd.eptid.scheduler.server.task;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.job.SplitContext;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.event.ClientEvent;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.failover.FailoverAction;
import com.jd.eptid.scheduler.core.failover.FailoverStrategy;
import com.jd.eptid.scheduler.core.failover.FailoverSupportedAction;
import com.jd.eptid.scheduler.core.failover.FailureJudger;
import com.jd.eptid.scheduler.core.listener.EventListener;
import com.jd.eptid.scheduler.core.response.JobSplitResponse;
import com.jd.eptid.scheduler.server.chooser.JobClientChooser;
import com.jd.eptid.scheduler.server.chooser.RandomJobClientChooser;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import com.jd.eptid.scheduler.server.core.ServerContext;
import com.jd.eptid.scheduler.server.job.JobContext;
import com.jd.eptid.scheduler.server.network.ChannelHolder;
import com.jd.eptid.scheduler.server.network.ServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by classdan on 16-10-19.
 */
public class TasksReceiver implements EventListener<ClientEvent> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JobContext jobContext;
    private ServerTransport transport;
    private JobClientChooser jobClientChooser;
    private FailoverStrategy failoverStrategy;
    private Thread currentThread;

    public TasksReceiver(JobContext jobContext) {
        this.jobContext = jobContext;
        transport = ServerContext.getInstance().getServerTransport();
        jobClientChooser = new RandomJobClientChooser(jobContext.getJob().getName());
        failoverStrategy = ServerContext.getInstance().getRetryStrategy();
        currentThread = Thread.currentThread();
    }

    public void init() throws InterruptedException {
        ServerContext.getInstance().getEventBroadcaster().register(ClientEvent.class, this);
        jobClientChooser.init();
        chooseJobClient();
    }

    public JobSplitResponse receive(int splitTimes, List<TaskConfig> previousTasks) {
        SplitContext splitContext = new SplitContext();
        splitContext.setJobName(jobContext.getJob().getName());
        splitContext.setSplitTimes(splitTimes);

        Message message = new Message();
        message.setType(MessageType.Task_Split.getCode());
        message.setContent(JSON.toJSONString(splitContext));
        Message response = sendJobSplitMessage(message);
        return JSON.parseObject(response.getContent(), JobSplitResponse.class);
    }

    private Message sendJobSplitMessage(final Message message) {
        final Client jobClient = jobContext.getJobClient();
        Assert.notNull(jobClient);

        return new FailoverSupportedAction<Message>() {
            @Override
            public Message action() throws Exception {
                return transport.send(ChannelHolder.findChannel(jobClient), message, Configuration.getInteger(ConfigItem.JOB_SPLIT_TIMEOUT, 1), TimeUnit.MINUTES);
            }

            @Override
            public FailoverStrategy<Message> getFailoverStrategy() {
                return failoverStrategy;
            }

            @Override
            public FailureJudger<Message> getFailureJudger() {
                return new FailureJudger<Message>() {
                    @Override
                    public boolean isFailed(Message responseMessage, Throwable e) {
                        if (e != null) {
                            return true;
                        }

                        JobSplitResponse response = JSON.parseObject(responseMessage.getContent(), JobSplitResponse.class);
                        return !response.isSuccess();
                    }
                };
            }

            @Override
            public FailoverAction<Message> getFailoverAction() {
                return new FailoverAction<Message>() {
                    @Override
                    public Message perform() throws Exception {
                        // change a job client
                        jobContext.setJobClient(chooseJobClient());

                        return transport.send(ChannelHolder.findChannel(jobContext.getJobClient()), message, 5, TimeUnit.SECONDS);
                    }
                };
            }
        }.perform();
    }

    private Client chooseJobClient() throws InterruptedException {
        logger.info("Choose a client to split job: {}...", jobContext.getJob());
        Client client = null;
        try {
            client = jobClientChooser.choose(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new ScheduleException("Failed to choose a client to split job.", jobContext.getJob());
        }
        logger.info("client {} was chosen to split job: {}.", client, jobContext.getJob());
        jobContext.setJobClient(client);
        return client;
    }

    public void destroy() {
        ServerContext.getInstance().getEventBroadcaster().unregister(ClientEvent.class, this);
        if (jobClientChooser != null) {
            jobClientChooser.destroy();
        }
    }

    @Override
    public void onEvent(ClientEvent event) {
        Client client = (Client) event.source();
        Assert.notNull(client);
        if (client.equals(jobContext.getJobClient()) && event.getCode() == ClientEvent.Code.REMOVED) {
            logger.warn("Job client {} disconnected...", client);
            if (currentThread.getState() == Thread.State.TIMED_WAITING) {
                currentThread.interrupt();
            }
        }
    }
}
