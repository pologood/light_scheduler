package com.jd.eptid.scheduler.test.job;

import com.jd.eptid.scheduler.core.domain.job.PeriodicJob;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.response.JobSplitResponse;
import com.jd.eptid.scheduler.server.job.JobContext;
import com.jd.eptid.scheduler.server.task.TasksReceiver;
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
    private JobContext jobContext;
    private TasksReceiver tasksReceiver;

    @Before
    public void init() {
        jobContext = new JobContext();
        PeriodicJob periodicJob = new PeriodicJob();
        periodicJob.setName("testJob");
        jobContext.setJob(periodicJob);
        jobContext.setJobClient(new Client("127.0.0.1", 2222, new HashSet<String>(Arrays.asList("testJob"))));
        tasksReceiver = new TasksReceiver(jobContext);
    }

    @Test
    public void testReceive() throws Exception {
        JobSplitResponse jobSplitResponse = tasksReceiver.receive(jobContext.getSplitTimes(), null);
        Assert.assertNotNull(jobSplitResponse);
    }

}
