package com.jd.eptid.scheduler.test.job;

import com.jd.eptid.scheduler.core.domain.job.CronJob;
import com.jd.eptid.scheduler.server.job.JobScheduler;
import com.jd.eptid.scheduler.test.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by ClassDan on 2016/10/8.
 */
public class JobSchedulerTest extends BaseTest {
    @Resource
    private JobScheduler jobScheduler;

    @Test
    public void testSubmit() throws InterruptedException {
        CronJob job = new CronJob();
        job.setId(1);
        job.setName("testJob");
        job.setDescription("It's a test job.");
        job.setPeriodicInterval(60);
        job.setMultipleExecution(false);
        jobScheduler.submit(job);

        TimeUnit.HOURS.sleep(1);
    }

}
