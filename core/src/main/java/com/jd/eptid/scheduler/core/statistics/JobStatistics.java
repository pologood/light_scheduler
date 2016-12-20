package com.jd.eptid.scheduler.core.statistics;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ClassDan on 2016/10/9.
 */
public class JobStatistics {
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

    public int incrementSuccess() {
        return successTasks.incrementAndGet();
    }

    public int incrementFailed() {
        return failedTasks.incrementAndGet();
    }
}
