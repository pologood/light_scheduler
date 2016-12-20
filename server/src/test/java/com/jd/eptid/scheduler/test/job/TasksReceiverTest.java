package com.jd.eptid.scheduler.test.job;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.job.CronJob;
import com.jd.eptid.scheduler.core.response.JobSplitResponse;
import com.jd.eptid.scheduler.server.job.JobContext;
import com.jd.eptid.scheduler.server.job.TasksReceiver;
import com.jd.eptid.scheduler.test.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by classdan on 16-10-20.
 */
public class TasksReceiverTest extends BaseTest {
    private TasksReceiver tasksReceiver;

    @Before
    public void init() {
        JobContext jobContext = new JobContext();
        CronJob cronJob = new CronJob();
        cronJob.setName("testJob");
        jobContext.setJob(cronJob);
        jobContext.setJobClient(new Client("127.0.0.1", 2222, new HashSet<String>(Arrays.asList("testJob"))));
        tasksReceiver = new TasksReceiver(jobContext);
    }

    @Test
    public void testReceive() throws Exception {
        JobSplitResponse jobSplitResponse = tasksReceiver.receive();
        Assert.assertNotNull(jobSplitResponse);
    }

}
