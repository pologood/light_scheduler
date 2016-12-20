package com.jd.eptid.scheduler.server.task;

import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.server.chooser.ClientChooser;
import com.jd.eptid.scheduler.server.core.AppContext;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.eptid.scheduler.server.job.ExecuteTaskQueue;
import com.jd.eptid.scheduler.server.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created by classdan on 16-9-29.
 */
public class TaskScheduler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ClientManager clientManager;
    private ClientChooser clientChooser;
    private JobContext jobContext;
    private ExecutorService taskExecutors = null;
    private ExecuteTaskQueue taskQueue = null;
    private BlockingQueue<Future> taskFutures = new LinkedBlockingDeque<Future>();
    private volatile boolean jobSplitDone = false;
    private Thread taskScheduleThread = null;
    private Thread taskMonitorThread = null;

    public TaskScheduler(JobContext jobContext) {
        this.jobContext = jobContext;
        clientManager = AppContext.getInstance().getClientManager();
        clientChooser = AppContext.getInstance().getClientChooser();
    }

    public void init() {
        int schedulableClients = clientManager.sizeOfSchedulableClients(jobContext.getJob().getName());
        if (schedulableClients <= 0) {
            throw new ScheduleException("No enough clients to run task.");
        }

        taskQueue = new ExecuteTaskQueue(schedulableClients);
        taskExecutors = Executors.newFixedThreadPool(schedulableClients, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "TaskTracker-" + jobContext.getJob().getName());
            }
        });

        taskMonitorThread = new Thread(new TaskMonitor(), "TaskMonitor-" + jobContext.getJob().getName());
        taskScheduleThread = new Thread(new Scheduler(), "TaskScheduler-" + jobContext.getJob().getName());
    }

    public void start() {
        taskMonitorThread.start();
        taskScheduleThread.start();
    }

    private void clean() throws InterruptedException {
        taskExecutors.shutdown();
        taskScheduleThread.join();
    }

    class Scheduler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    TaskConfig taskConfig = taskQueue.take();

                    Client taskClient = chooseTaskClient(taskConfig);
                    Future taskFuture = taskExecutors.submit(new TaskRunner(taskClient, taskConfig));
                    taskFutures.add(taskFuture);
                } catch (InterruptedException e) {
                    logger.debug("Task schedule is canceled.", e);
                    break;
                }
            }
        }

        private Client chooseTaskClient(TaskConfig taskConfig) {
            logger.info("Choose a task client to run task: {}...", taskConfig);
            Client taskClient = clientChooser.chooseAndOccupy(jobContext.getJob().getName());
            if (taskClient == null) {
                throw new ScheduleException("Failed to chooseAndOccupy a task client for task:" + taskConfig);
            }
            logger.info("Task client {} was chosen to run task: {}.", taskClient, taskConfig);
            return taskClient;
        }

    }

    class TaskRunner implements Runnable {
        private static final int maxTaskRetryTimes = 3;
        private Client taskClient;
        private TaskConfig taskConfig;

        TaskRunner(Client taskClient, TaskConfig taskConfig) {
            this.taskClient = taskClient;
            this.taskConfig = taskConfig;
        }

        @Override
        public void run() {
            TaskTracker taskTracker = new TaskTracker(jobContext, taskClient, taskConfig);
            try {
                taskTracker.track();
                jobContext.getJobStatistics().incrementSuccess();
            } catch (Exception e) {
                handleFailedCase(taskConfig);
            } finally {
                taskQueue.releaseOne();
                clientChooser.release(jobContext.getJob().getName(), taskClient);
            }
        }

        private void handleFailedCase(TaskConfig taskConfig) {
            if (taskConfig.getRetryTimes() < maxTaskRetryTimes) {
                logger.warn("Retry execute task: {}. Retry times: {}.", taskConfig, taskConfig.getRetryTimes());
                taskConfig.setRetryTimes(taskConfig.getRetryTimes() + 1);
                try {
                    taskQueue.put(taskConfig);
                } catch (InterruptedException e) {
                    logger.error("Task re-schedule is canceled.", e);
                }
            } else {
                jobContext.getJobStatistics().incrementFailed();
            }
        }
    }

    class TaskMonitor implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Future taskFuture = taskFutures.take();
                    if (!taskFuture.isDone()) {
                        taskFutures.put(taskFuture);
                    }

                    if (jobSplitDone && taskQueue.isEmpty() && taskFutures.isEmpty()) {
                        taskScheduleThread.interrupt();

                        clean();
                        jobContext.getAllTaskFinishedLatch().countDown();
                        break;
                    }

                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // Ignore
                    break;
                }
            }
        }
    }

    public void addTask(TaskConfig taskConfig) throws InterruptedException {
        taskQueue.putIfPresent(taskConfig);
        jobContext.getJobStatistics().incrementTotalTasks();
    }

    public void setJobSplitDone() {
        this.jobSplitDone = true;
        if (taskQueue.isEmpty() && taskScheduleThread.getState() == Thread.State.WAITING) {
            taskScheduleThread.interrupt();
        }
    }

}
