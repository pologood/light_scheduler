package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.job.SplitContext;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.failover.FailoverAction;
import com.jd.eptid.scheduler.core.failover.FailoverStrategy;
import com.jd.eptid.scheduler.core.failover.FailoverSupportedAction;
import com.jd.eptid.scheduler.core.failover.FailureJudger;
import com.jd.eptid.scheduler.core.response.JobSplitResponse;
import com.jd.eptid.scheduler.server.chooser.ClientChooser;
import com.jd.eptid.scheduler.server.core.AppContext;
import com.jd.eptid.scheduler.server.network.ChannelHolder;
import com.jd.eptid.scheduler.server.network.ServerTransport;
import com.jd.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-10-19.
 */
public class TasksReceiver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JobContext jobContext;
    private ServerTransport transport;
    private ClientChooser clientChooser;
    private FailoverStrategy failoverStrategy;

    public TasksReceiver(JobContext jobContext) {
        this.jobContext = jobContext;
        transport = AppContext.getInstance().getServerTransport();
        clientChooser = AppContext.getInstance().getClientChooser();
        failoverStrategy = AppContext.getInstance().getRetryStrategy();
    }

    public JobSplitResponse receive() {
        SplitContext splitContext = new SplitContext();
        splitContext.setJobName(jobContext.getJob().getName());
        splitContext.setSplitTimes(jobContext.getSplitTimes());

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
                return transport.send(ChannelHolder.findChannel(jobClient), message, 5, TimeUnit.SECONDS);
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

    private Client chooseJobClient() {
        logger.info("Choose a job client to split job: {}...", jobContext.getJob());
        Client client = clientChooser.chooseAndOccupy(jobContext.getJob().getName());
        if (client == null) {
            throw new ScheduleException("Failed to choose a job client for job.", JobContextHolder.getContext().getJob());
        }
        logger.info("Job client {} was chosen to split job: {}.", client, jobContext.getJob());
        return client;
    }

}
