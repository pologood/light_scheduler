package com.jd.eptid.scheduler.test.job;

import com.jd.eptid.scheduler.core.domain.job.OneShotJob;
import com.jd.eptid.scheduler.core.domain.job.PeriodicJob;
import com.jd.eptid.scheduler.core.event.JobEvent;
import com.jd.eptid.scheduler.core.failover.FailoverPolicy;
import com.jd.eptid.scheduler.core.utils.TimeUtils;
import com.jd.eptid.scheduler.server.core.ServerContext;
import com.jd.eptid.scheduler.server.job.JobScheduler;
import com.jd.eptid.scheduler.test.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by ClassDan on 2016/10/8.
 */
public class JobSchedulerTest extends BaseTest {
    @Resource
    private JobScheduler jobScheduler;

    @Test
    public void testSubmit() throws InterruptedException {
        PeriodicJob job = new PeriodicJob();
        job.setId(1L);
        job.setName("testJob");
        job.setDescription("It's a test job.");
        job.setPeriodicInterval(300);
        job.setAllowConcurrent(false);
        jobScheduler.submit(job);

        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testSubmit2() throws InterruptedException {
        PeriodicJob shortJob = new PeriodicJob();
        shortJob.setId(2L);
        shortJob.setName("shortJob");
        shortJob.setDescription("It's a short job.");
        shortJob.setPeriodicInterval(300);
        shortJob.setAllowConcurrent(false);
        jobScheduler.submit(shortJob);

        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testMutexJobSubmit() throws InterruptedException {
        TimeUnit.SECONDS.sleep(30);

        PeriodicJob job = new PeriodicJob();
        job.setId(1L);
        job.setName("testJob");
        job.setDescription("It's a test job.");
        job.setPeriodicInterval(300);
        job.setAllowConcurrent(false);
        jobScheduler.submit(job);

        TimeUnit.SECONDS.sleep(15);

        PeriodicJob shortJob = new PeriodicJob();
        shortJob.setId(2L);
        shortJob.setName("shortJob");
        shortJob.setDescription("It's a short job.");
        shortJob.setPeriodicInterval(60);
        shortJob.setAllowConcurrent(false);
        shortJob.setMutexJobIds(Arrays.asList(1L));
        jobScheduler.submit(shortJob);

        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testJobUpdated() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);

        PeriodicJob shortJob = new PeriodicJob();
        shortJob.setId(2L);
        shortJob.setName("shortJob");
        shortJob.setDescription("It's a short job.");
        shortJob.setPeriodicInterval(300);
        shortJob.setAllowConcurrent(false);
        jobScheduler.submit(shortJob);

        TimeUnit.SECONDS.sleep(15);
        shortJob.setPeriodicInterval(600);
        ServerContext.getInstance().getEventBroadcaster().publish(new JobEvent(shortJob, JobEvent.Code.UPDATE));

        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testNormalJobSubmit() throws InterruptedException {
        OneShotJob oneShotJob = new OneShotJob();
        oneShotJob.setId(293L);
        oneShotJob.setName("shortJob");
        oneShotJob.setDescription("It's a one-time job.");
        oneShotJob.setFailoverPolicy(FailoverPolicy.RETRY);
        jobScheduler.submit(oneShotJob);

        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testMayErrorJobSubmit() throws InterruptedException {
        PeriodicJob mayErrorJob = new PeriodicJob();
        mayErrorJob.setId(404L);
        mayErrorJob.setName("errorExistJob");
        mayErrorJob.setDescription("It's a error existed job.");
        mayErrorJob.setPeriodicInterval(300);
        mayErrorJob.setAllowConcurrent(false);
        jobScheduler.submit(mayErrorJob);

        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testOneShotJobSubmit() throws ParseException, InterruptedException {
        OneShotJob oneShotJob = new OneShotJob();
        oneShotJob.setId(222L);
        oneShotJob.setName("oneShotJob");
        oneShotJob.setDescription("It is a one-shot job.");
        oneShotJob.setStartTime(TimeUtils.parseLongDate("2017-02-06 17:49:00"));
        jobScheduler.submit(oneShotJob);

        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void testLongJobSubmit() throws InterruptedException {
        PeriodicJob job = new PeriodicJob();
        job.setId(204L);
        job.setName("longJob");
        job.setDescription("It's a long job.");
        job.setPeriodicInterval(300);
        job.setAllowConcurrent(false);
        jobScheduler.submit(job);

        TimeUnit.DAYS.sleep(1);
    }

}
