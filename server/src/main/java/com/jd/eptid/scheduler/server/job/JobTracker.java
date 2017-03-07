package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.domain.job.Job;
import com.jd.eptid.scheduler.core.domain.job.JobStatus;
import com.jd.eptid.scheduler.core.domain.job.ScheduledJob;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.event.ScheduleEvent;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.response.JobSplitResponse;
import com.jd.eptid.scheduler.core.statistics.JobStatistics;
import com.jd.eptid.scheduler.server.core.ServerContext;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import com.jd.eptid.scheduler.server.task.TaskScheduler;
import com.jd.eptid.scheduler.server.task.TasksReceiver;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class JobTracker {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ScheduledJobDao scheduledJobDao;
    private TasksReceiver tasksReceiver;
    private JobContext jobContext;
    private ScheduledJob scheduledJob;
    private TaskScheduler taskScheduler;
    private Thread theThread;

    public JobTracker(JobContext context) {
        this.jobContext = context;
        taskScheduler = new TaskScheduler(jobContext);
        tasksReceiver = new TasksReceiver(jobContext);
        scheduledJobDao = ServerContext.getInstance().getScheduledJobDao();
        theThread = Thread.currentThread();
    }

    public void track() throws Exception {
        prepare();

        try {
            beforeRun();

            runJob();

            recordResult();
        } catch (Exception e) {
            handleException(e);
        } finally {
            tasksReceiver.destroy();
            taskScheduler.stop(false);
            logger.info("Job [{}, {}] done.", jobContext.getJob().getName(), jobContext.getScheduleId());
            ServerContext.getInstance().getEventBroadcaster().publish(new ScheduleEvent<Job>(jobContext.getJob(), jobContext.getScheduleId(), ScheduleEvent.Code.DONE));
        }
    }

    private void prepare() {
        ScheduledJob scheduledJob = new ScheduledJob();
        scheduledJob.setJobId(jobContext.getJob().getId());
        scheduledJob.setScheduleId(jobContext.getScheduleId());
        scheduledJob.setStatus(JobStatus.WAITING);
        scheduledJob.setCreateTime(new Date());
        int affected = scheduledJobDao.add(scheduledJob);
        if (affected <= 0) {
            throw new ScheduleException("Failed to add scheduled job: " + scheduledJob);
        }
        this.scheduledJob = scheduledJob;
        jobContext.setStoreId(scheduledJob.getId());
    }

    private void beforeRun() throws InterruptedException {
        updateStatus(JobStatus.RUNNING);
        tasksReceiver.init();
        taskScheduler.start();
    }

    private void updateStatus(JobStatus newStatus) {
        int affected = 0;
        switch (newStatus) {
            case RUNNING:
                affected = scheduledJobDao.start(scheduledJob.getId());
                break;
            case PARTIAL_SUCCESS:
            case SUCCESS:
            case FAILED:
                JobStatistics jobStatistics = jobContext.getJobStatistics();
                affected = scheduledJobDao.end(scheduledJob.getId(), newStatus.getCode(), jobStatistics.getTotalTasks(), jobStatistics.getSuccessTasks(), jobStatistics.getFailedTasks());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported status: " + newStatus);
        }

        if (affected <= 0) {
            throw new IllegalStateException("Failed to change status of scheduled job: " + scheduledJob.getId() + ", new status: " + newStatus);
        }
    }

    private void runJob() throws Exception {
        List<TaskConfig> taskConfigs = null;
        do {
            jobContext.incrSplitTimes();
            try {
                JobSplitResponse response = tasksReceiver.receive(jobContext.getSplitTimes(), taskConfigs);
                taskConfigs = response.getTaskConfigs();
                if (taskConfigs == null) {
                    throw new ScheduleException("Illegal split result.");
                }
                for (TaskConfig taskConfig : taskConfigs) {
                    taskScheduler.addTask(taskConfig);
                }

                if (response.isLast()) {
                    markSplitFinished();
                    break;
                }
            } catch (Exception e) {
                logger.error("Failed to split the job: {}.", jobContext.getJob().getName(), e);
                markSplitFinished();
                throw e;
            }
        } while (taskConfigs != null && CollectionUtils.isNotEmpty(taskConfigs));

        logger.info("Job split finished. job:{}, scheduledId: {}.", jobContext.getJob().getName(), jobContext.getScheduleId());
        waitForDone();
    }

    private void markSplitFinished() throws InterruptedException {
        // insert a poison object to mark the finish of job splitting.
        taskScheduler.addTask(TaskConfig.POISON);
    }

    private void waitForDone() throws InterruptedException {
        jobContext.getJobStatistics().waitForDone();
    }

    private void recordResult() {
        int totalTasks = jobContext.getJobStatistics().getTotalTasks();
        int failedTasks = jobContext.getJobStatistics().getFailedTasks();
        int successTasks = jobContext.getJobStatistics().getSuccessTasks();
        if (totalTasks == successTasks) {
            logger.info("Job [{}] has been executed successful.", jobContext.getJob().getName());
            updateStatus(JobStatus.SUCCESS);
        } else if (totalTasks == failedTasks) {
            logger.info("Job [{}] has been executed failed.", jobContext.getJob().getName());
            updateStatus(JobStatus.FAILED);
        } else {
            logger.info("Job [{}] has been executed partially successful.", jobContext.getJob().getName());
            updateStatus(JobStatus.PARTIAL_SUCCESS);
        }
    }

    public JobContext getJobContext() {
        return jobContext;
    }

    private void handleException(Exception e) throws Exception {
        if (e instanceof InterruptedException) {
            logger.error("Job scheduling [{}, {}] is canceled.", jobContext.getJob().getName(), jobContext.getScheduleId());
            taskScheduler.stop(true);
            updateStatus(JobStatus.CANCELED);
        } else {
            updateStatus(JobStatus.FAILED);
            throw e;
        }
    }

    public void cancel() {
        theThread.interrupt();
    }

}