package com.jd.eptid.scheduler.core.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ClassDan on 2016/10/9.
 */
public class JobStatistics {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private AtomicInteger totalTasks = new AtomicInteger(0);
    private AtomicInteger successTasks = new AtomicInteger(0);
    private AtomicInteger failedTasks = new AtomicInteger(0);

    public int getTotalTasks() {
        return totalTasks.get();
    }

    public int incrementTotalTasks() {
        return totalTasks.incrementAndGet();
    }

    public int getSuccessTasks() {
        return successTasks.get();
    }

    public int getFailedTasks() {
        return failedTasks.get();
    }

    public synchronized void incrementSuccess() {
        successTasks.incrementAndGet();
        notifyAll();
    }

    public synchronized void incrementFailed() {
        failedTasks.incrementAndGet();
        notifyAll();
    }

    public synchronized void waitForDone() throws InterruptedException {
        int total = 0;
        int done = 0;
        do {
            total = totalTasks.get();
            done = successTasks.get() + failedTasks.get();
            logger.info("total: {}, done: {}.", total, done);

            if (total == done) {
                break;
            }
            wait();
        } while (true);
    }

}
