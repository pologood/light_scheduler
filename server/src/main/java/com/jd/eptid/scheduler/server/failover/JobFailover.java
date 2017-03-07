package com.jd.eptid.scheduler.server.failover;

import com.jd.eptid.scheduler.core.common.LifeCycle;
import com.jd.eptid.scheduler.core.common.ShutdownHook;
import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.failover.FailoverPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by classdan on 17-1-20.
 */
public class JobFailover implements LifeCycle {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private BlockingQueue<Job> failedJobQueue = new LinkedBlockingQueue<Job>();
    private Map<FailoverPolicy, FailureHandler> handlerMapping = new HashMap<FailoverPolicy, FailureHandler>();
    private Thread failoverThread = null;

    public JobFailover() {
        ShutdownHook.getInstance().addLifeCycleObject(this);
        handlerMapping.put(FailoverPolicy.RETRY, new RetryHandler());
        handlerMapping.put(FailoverPolicy.MANUAL, new ManualHandler());
        handlerMapping.put(FailoverPolicy.ALARM, new AlarmHandler());
    }

    public void fail(Job job) {
        failedJobQueue.add(job);
    }

    @Override
    public void start() {
        failoverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Job failedJob = null;
                    try {
                        failedJob = failedJobQueue.take();
                    } catch (InterruptedException e) {
                        break;
                    }

                    FailoverPolicy policy = failedJob.getFailoverPolicy();
                    if (policy == null || policy == FailoverPolicy.NONE) {
                        continue;
                    }

                    FailureHandler failureHandler = handlerMapping.get(policy);
                    if (failureHandler == null) {
                        logger.error("No FailureHandler found for policy: {}.", policy);
                        continue;
                    }
                    try {
                        failureHandler.handle(failedJob);
                    } catch (Exception e) {
                        logger.error("Failed to handle job failure.", failedJob, e);
                    }
                }
            }
        }, "Job-Failover");
        failoverThread.start();
    }

    @Override
    public void stop() {
        failoverThread.interrupt();
    }

    @Override
    public int order() {
        return 0;
    }

}
