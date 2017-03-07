package com.jd.eptid.scheduler.client.processor;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.client.core.ClientContext;
import com.jd.eptid.scheduler.client.core.ConfigItem;
import com.jd.eptid.scheduler.client.core.Task;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.processor.MessageProcessor;
import com.jd.eptid.scheduler.core.response.TaskResponse;
import com.jd.eptid.scheduler.core.utils.GenericUtils;
import com.jd.eptid.scheduler.core.utils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by ClassDan on 2016/10/1.
 */
public class RunTaskMessageProcessor implements MessageProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExecutorService taskExecutor = null;

    public RunTaskMessageProcessor() {
        int taskExecutePoolSize = Configuration.getInteger(ConfigItem.TASK_EXECUTE_POOL_SIZE, 5);
        taskExecutor = Executors.newFixedThreadPool(taskExecutePoolSize, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "task-executor");
            }
        });
        Assert.notNull(taskExecutor);
    }

    @Override
    public void process(Message message, ChannelHandlerContext ctx) {
        Assert.isTrue(message.getType() == MessageType.Task_Run.getCode());
        logger.info("Received task: {}.", message.getContent());

        try {
            TaskConfig taskConfig = JSON.parseObject(message.getContent(), TaskConfig.class);
            String jobName = taskConfig.getJobName();
            Task task = ClientContext.getInstance().getTask(jobName);
            Assert.notNull(task, "Task for job [" + jobName + "] not found.");

            executeTask(task, taskConfig, message, ctx);
        } catch (Exception e) {
            logger.error("Failed to process Task_Run message. {}.", message, e);
            reportResult(message, TaskResponse.buildFailed(e.getMessage()), ctx);
        }
    }

    private void executeTask(final Task task, final TaskConfig taskConfig, final Message message, final ChannelHandlerContext ctx) {
        taskExecutor.submit(new Runnable() {
            @Override
            public void run() {
                TaskResponse response = new TaskResponse();
                response.setStartTime(new Date());
                try {
                    Object taskParam = extractTaskParam(task, taskConfig);
                    task.run(taskParam);
                    response.setSuccess(true);
                } catch (Exception e) {
                    logger.error("Failed to execute task: {}.", taskConfig, e);
                    response.setSuccess(false);
                    response.setMessage(e.getMessage());
                }
                response.setEndTime(new Date());
                reportResult(message, response, ctx);
            }
        });
    }

    private Object extractTaskParam(Task task, TaskConfig taskConfig) {
        Class genericParameterClass = GenericUtils.getGenericParameterClass(task);
        return JsonUtils.parse(taskConfig.getParam(), genericParameterClass);
    }

    private void reportResult(Message message, TaskResponse taskResponse, ChannelHandlerContext ctx) {
        Message response = new Message();
        response.setMessageId(message.getMessageId());
        response.setType(MessageType.Task_Run.getCode());
        response.setContent(JSON.toJSONString(taskResponse));
        ctx.writeAndFlush(response);
    }
}
