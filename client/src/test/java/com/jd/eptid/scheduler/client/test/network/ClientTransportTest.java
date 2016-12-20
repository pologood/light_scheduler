package com.jd.eptid.scheduler.client.test.network;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.client.core.Job;
import com.jd.eptid.scheduler.client.core.SplitResult;
import com.jd.eptid.scheduler.client.network.ClientTransport;
import com.jd.eptid.scheduler.client.network.WorkerClientTransport;
import com.jd.eptid.scheduler.client.test.job.TestJob;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.response.JobSplitResponse;
import com.jd.eptid.scheduler.core.utils.GenericUtils;
import com.jd.eptid.scheduler.core.utils.JsonUtils;
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
        this.clientTransport = new WorkerClientTransport();
    }

    @Test
    public void testStart() throws InterruptedException {
        clientTransport.start();

        TimeUnit.MINUTES.sleep(3);
    }

    @Test
    public void testSend() {

    }

    @Test
    public void testGeneric() {
        TestJob testJob = new TestJob();
        Class type = GenericUtils.getGenericParameterClass(testJob);
        SplitResult result = testJob.split(1);

        JobSplitResponse response = new JobSplitResponse();
        response.setJobName("testJob");
        response.setLast(result.isLast());
        response.setTaskConfigs(packTaskConfig(testJob, result));
        String json = JSON.toJSONString(response);
        System.out.println(json);

        JobSplitResponse responseParsed = JSON.parseObject(json, JobSplitResponse.class);
        List<TaskConfig> taskConfigs = responseParsed.getTaskConfigs();
        List<Object> params = new ArrayList();
        for (TaskConfig taskConfig : taskConfigs) {
            params.add(JsonUtils.parse(taskConfig.getParam(), type));
        }
        System.out.println(params);

        /*List<TaskConfig<String>> taskConfigs = new ArrayList();
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.setNum(1);
        taskConfig.setJobName("aaaa");
        taskConfig.setParam(new TaskParam("param1"));
        taskConfigs.add(taskConfig);
        taskConfig = new TaskConfig();
        taskConfig.setNum(2);
        taskConfig.setJobName("bbbb");
        taskConfig.setParam(new TaskParam("param2"));
        taskConfigs.add(taskConfig);

        Message message = new Message();
        message.setType(MessageType.Task_Split.getCode());
        message.setContent(JSON.toJSONString(taskConfigs));
        String json = JSON.toJSONString(message);
        System.out.println(json);

        Message msgParsed = JSON.parseObject(json, Message.class);
        List<TaskConfig<String>> data = JSON.parseObject(msgParsed.getContent(), new TypeReference<List<TaskConfig<String>>>(){});
        System.out.println(data);*/
    }

    private List<TaskConfig> packTaskConfig(Job job, SplitResult splitResult) {
        Class paramType = GenericUtils.getGenericParameterClass(job);
        List<TaskConfig> taskConfigs = new ArrayList();
        int i = 1;
        for (Object param : splitResult.getTaskParams()) {
            TaskConfig taskConfig = new TaskConfig();
            taskConfig.setNum(i++);
            taskConfig.setParam(param);
            taskConfigs.add(taskConfig);
        }
        return taskConfigs;
    }

}
