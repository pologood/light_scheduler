package com.jd.eptid.scheduler.server.task;

import com.jd.eptid.scheduler.core.common.ScheduleThreadFactory;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.task.TaskClient;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.server.chooser.RandomTaskClientChooser;
import com.jd.eptid.scheduler.server.chooser.TaskClientChooser;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import com.jd.eptid.scheduler.server.job.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Created by classdan on 16-9-29.
 */
public class TaskScheduler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JobContext jobContext;
    private ExecutorService taskExecutors = null;
    private TaskQueue taskQueue = null;
    private TaskClientChooser taskClientChooser;
    private Thread taskScheduleThread = null;

    public TaskScheduler(JobContext jobContext) {
        this.jobContext = jobContext;
        taskQueue = new TaskQueue(100);
        taskClientChooser = new RandomTaskClientChooser(jobContext.getJob().getName());
    }

    public void start() {
        taskClientChooser.init();

        taskExecutors = Executors.newFixedThreadPool(10, new ScheduleThreadFactory("TaskTracker-" + jobContext.getJob().getName() + "-"));

        taskScheduleThread = new ScheduleThreadFactory("TaskScheduler-" + jobContext.getJob().getName(), false).newThread(new Scheduler());
        taskScheduleThread.start();
    }

    class Scheduler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    TaskConfig taskConfig = taskQueue.take();
                    if (taskConfig == TaskConfig.POISON) {
                        break;
                    }

                    taskExecutors.submit(new TaskRunner(taskConfig));
                } catch (InterruptedException e) {
                    logger.debug("Task schedule is canceled.", e);
                    break;
                }
            }

            taskExecutors.shutdown();
        }
    }

    class TaskRunner implements Runnable {
        private String scheduleId;
        private TaskConfig taskConfig;

        TaskRunner(TaskConfig taskConfig) {
            this.scheduleId = UUID.randomUUID().toString();
            this.taskConfig = taskConfig;
        }

        @Override
        public void run() {
            try {
                boolean isSuccess = runWithFailover();
                if (isSuccess) {
                    jobContext.getJobStatistics().incrementSuccess();
                } else {
                    jobContext.getJobStatistics().incrementFailed();
                }
            } finally {
                taskQueue.releaseOne();
            }
        }

        private boolean runWithFailover() {
            int maxRetryTimes = Configuration.getInteger(ConfigItem.TASK_RETRY_TIMES, 3);
            while (taskConfig.getRetryTimes() < maxRetryTimes) {
                if (taskConfig.getRetryTimes() != 0) {
                    logger.info("Retry task [{}, {}]: {}...", jobContext.getJob().getName(), scheduleId, taskConfig.getRetryTimes());
                }

                try {
                    runTask();
                    return true;
                } catch (InterruptedException e) {
                    logger.error("Task tracker is cancelled. {}.", taskConfig, e);
                    break;
                } catch (Exception e) {
                    logger.error("Task [{}, {}] execute failed.", jobContext.getJob().getName(), scheduleId, e);
                    taskConfig.incrRetryTimes();
                }
            }
            return false;
        }

        private void runTask() throws Exception {
            Client taskClient = null;
            try {
                taskClient = chooseTaskClient();
                logger.info("Run task: {}, client: {}.", taskConfig, taskClient);
                TaskTracker taskTracker = new TaskTracker(jobContext, scheduleId, taskClient, taskConfig);
                taskTracker.track();
            } finally {
                if (taskClient != null) {
                    taskClientChooser.back(taskClient);
                }
                jobContext.removeTaskClient(scheduleId);
            }
        }

        private Client chooseTaskClient() throws InterruptedException, TimeoutException {
            Client taskClient = taskClientChooser.choose();
            jobContext.addTaskClient(scheduleId, new TaskClient(scheduleId, taskConfig, taskClient));
            return taskClient;
        }
    }

    public void addTask(TaskConfig taskConfig) throws InterruptedException {
        if (taskConfig == TaskConfig.POISON) {
            taskQueue.put(taskConfig);
        } else {
            taskQueue.putIfPresent(taskConfig);
            jobContext.getJobStatistics().incrementTotalTasks();
        }
    }

    public void stop(boolean mayInterruptIfRunning) throws InterruptedException {
        logger.info("Stop task scheduler [{}, {}]...", jobContext.getJob().getName(), jobContext.getScheduleId());
        if (mayInterruptIfRunning) {
            taskScheduleThread.interrupt();
        }
        taskClientChooser.destroy();
    }

}
