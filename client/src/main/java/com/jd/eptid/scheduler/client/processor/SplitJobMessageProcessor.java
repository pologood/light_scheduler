package com.jd.eptid.scheduler.client.processor;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.client.core.AppContext;
import com.jd.eptid.scheduler.client.core.ConfigItem;
import com.jd.eptid.scheduler.client.core.Job;
import com.jd.eptid.scheduler.client.core.SplitResult;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.job.SplitContext;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.processor.MessageProcessor;
import com.jd.eptid.scheduler.core.response.JobSplitResponse;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by ClassDan on 2016/10/1.
 */
public class SplitJobMessageProcessor implements MessageProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExecutorService splitExecutor = null;

    public SplitJobMessageProcessor() {
        int jobSplitPoolSize = Configuration.getInteger(ConfigItem.JOB_SPLIT_POOL_SIZE, 5);
        splitExecutor = Executors.newFixedThreadPool(jobSplitPoolSize, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "job-split-executor");
            }
        });
        Assert.notNull(splitExecutor);
    }

    @Override
    public void process(Message message, ChannelHandlerContext ctx) {
        Assert.isTrue(message.getType() == MessageType.Task_Split.getCode());

        try {
            SplitContext splitContext = JSON.parseObject(message.getContent(), SplitContext.class);
            String jobName = splitContext.getJobName();
            Assert.hasText(jobName);
            Job job = AppContext.getInstance().getJob(jobName);
            Assert.notNull(job, "Job for job [" + jobName + "] not found.");

            executeJobSplit(job, splitContext, message, ctx);
        } catch (Exception e) {
            logger.error("Failed to process the message: {}.", message, e);
            reportResult(message, ctx, JobSplitResponse.buildFailed(e.getMessage()));
        }
    }

    private void executeJobSplit(final Job job, final SplitContext splitContext, final Message message, final ChannelHandlerContext ctx) {
        splitExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    SplitResult splitResult = job.split(splitContext.getSplitTimes());
                    Assert.notNull(splitResult.getTaskParams());

                    JobSplitResponse response = new JobSplitResponse();
                    response.setSuccess(true);
                    response.setJobName(job.name());
                    response.setLast(splitResult.isLast());
                    response.setTaskConfigs(packTaskConfig(job, splitResult));
                    reportResult(message, ctx, response);
                } catch (Exception e) {
                    logger.error("Failed to execute job split: {}.", message, e);
                    reportResult(message, ctx, JobSplitResponse.buildFailed(e.getMessage()));
                }
            }
        });
    }

    private List<TaskConfig> packTaskConfig(Job job, SplitResult splitResult) {
        List<TaskConfig> taskConfigs = new ArrayList();
        int i = 1;
        for (Object param : splitResult.getTaskParams()) {
            TaskConfig taskConfig = new TaskConfig();
            taskConfig.setJobName(job.name());
            taskConfig.setNum(i++);
            taskConfig.setParam(param);
            taskConfigs.add(taskConfig);
        }
        return taskConfigs;
    }

    private void reportResult(Message message, ChannelHandlerContext ctx, JobSplitResponse response) {
        Message responseMessage = new Message();
        responseMessage.setMessageId(message.getMessageId());
        responseMessage.setType(MessageType.Task_Split.getCode());
        responseMessage.setContent(JSON.toJSONString(response));
        ctx.writeAndFlush(responseMessage);
    }

}
