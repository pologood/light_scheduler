package com.jd.eptid.scheduler.client.test.network;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.client.core.ClientContext;
import com.jd.eptid.scheduler.client.core.Job;
import com.jd.eptid.scheduler.client.core.SplitResult;
import com.jd.eptid.scheduler.client.network.ClientTransport;
import com.jd.eptid.scheduler.client.network.WorkerClientTransport;
import com.jd.eptid.scheduler.client.test.job.TestJob;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.domain.node.Server;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.response.JobSplitResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-9-30.
 */
public class ClientTransportTest {
    private ClientTransport clientTransport;

    @Before
    public void init() {
        clientTransport = new WorkerClientTransport();
        Server server = new Server();
        server.setIp("127.0.0.1");
        server.setPort(9188);
        ClientContext.getInstance().setMasterNode(server);
        clientTransport.start();
        clientTransport.connect();
    }

    @Test
    public void testStandby() throws InterruptedException {
        TimeUnit.MINUTES.sleep(3);
    }

    @Test
    public void testSend() throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);

        TestJob job = new TestJob();
        SplitResult splitResult = job.split(1);

        JobSplitResponse response = new JobSplitResponse();
        response.setSuccess(true);
        response.setJobName(job.name());
        response.setLast(splitResult.isLast());
        response.setTaskConfigs(packTaskConfig(job, splitResult));
        reportResult(response);

        TimeUnit.MINUTES.sleep(3);
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

    private void reportResult(JobSplitResponse response) {
        Message responseMessage = new Message();
        responseMessage.setType(MessageType.Task_Split.getCode());
        responseMessage.setContent(JSON.toJSONString(response));
        clientTransport.send(responseMessage);
    }

}
