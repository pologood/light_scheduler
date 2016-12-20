package com.jd.eptid.scheduler.server.job;


import com.jd.eptid.scheduler.core.domain.task.TaskConfig;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

/**
 * Created by classdan on 16-9-29.
 */
public class ExecuteTaskQueue {
    private BlockingQueue<TaskConfig> taskQueue = null;
    private Semaphore schedulable = null;

    public ExecuteTaskQueue(int candidate) {
        taskQueue = new LinkedBlockingDeque<TaskConfig>(candidate * 2);
        schedulable = new Semaphore(candidate);
    }

    public TaskConfig take() throws InterruptedException {
        return taskQueue.take();
    }

    public void releaseOne() {
        schedulable.release();
    }

    public void putIfPresent(TaskConfig taskConfig) throws InterruptedException {
        schedulable.acquire();
        taskQueue.put(taskConfig);
    }

    public void put(TaskConfig taskConfig) throws InterruptedException {
        taskQueue.put(taskConfig);
    }

    public boolean isEmpty() {
        return taskQueue.isEmpty();
    }

}
