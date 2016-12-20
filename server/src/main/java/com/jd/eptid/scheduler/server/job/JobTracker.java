package com.jd.eptid.scheduler.server.job;

import com.jd.eptid.scheduler.core.domain.job.ScheduledJob;
import com.jd.eptid.scheduler.core.domain.job.Status;
import com.jd.eptid.scheduler.core.domain.node.Client;
import com.jd.eptid.scheduler.core.domain.task.TaskConfig;
import com.jd.eptid.scheduler.core.exception.ScheduleException;
import com.jd.eptid.scheduler.core.response.JobSplitResponse;
import com.jd.eptid.scheduler.core.statistics.JobStatistics;
import com.jd.eptid.scheduler.server.chooser.ClientChooser;
import com.jd.eptid.scheduler.server.core.AppContext;
import com.jd.eptid.scheduler.server.dao.ScheduledJobDao;
import com.jd.eptid.scheduler.server.task.TaskScheduler;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class JobTracker {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ScheduledJobDao scheduledJobDao;
    private ClientChooser clientChooser;
    private TasksReceiver tasksReceiver;
    private JobContext jobContext;
    private ScheduledJob scheduledJob;
    private TaskScheduler taskScheduler;

    public JobTracker(JobContext context) {
        this.jobContext = context;
        taskScheduler = new TaskScheduler(jobContext);
        tasksReceiver = new TasksReceiver(jobContext);
        clientChooser = AppContext.getInstance().getClientChooser();
        scheduledJobDao = AppContext.getInstance().getScheduledJobDao();
    }

    public void start() {
        prepare();

        try {
            chooseJobClient();

            beforeRun();

            runJob();
        } catch (Throwable e) {
            logger.error("Failed to track job: {}.", jobContext.getJob().getName(), e);
            updateStatus(Status.FAILED);
        } finally {
            JobContextHolder.remove();
            if (jobContext.getJobClient() != null) {
                clientChooser.release(jobContext.getJob().getName(), jobContext.getJobClient());
            }
        }
    }

    private void prepare() {
        ScheduledJob scheduledJob = new ScheduledJob();
        scheduledJob.setJobId(jobContext.getJob().getId());
        scheduledJob.setStatus(Status.WAITING);
        scheduledJob.setCreateTime(new Date());
        int affected = scheduledJobDao.add(scheduledJob);
        if (affected <= 0) {
            throw new ScheduleException("Failed to add scheduled job: " + scheduledJob);
        }
        this.scheduledJob = scheduledJob;
        jobContext.setScheduleId(scheduledJob.getId());
    }

    private void chooseJobClient() {
        logger.info("Choose a job client to run job: {}...", jobContext.getJob());
        Client client = clientChooser.chooseAndOccupy(jobContext.getJob().getName());
        if (client == null) {
            throw new ScheduleException("Failed to choose a job client for job.", JobContextHolder.getContext().getJob());
        }
        logger.info("Job client {} was chosen to run job: {}.", client, jobContext.getJob());
        jobContext.setJobClient(client);
    }

    private void beforeRun() {
        taskScheduler.init();
        taskScheduler.start();

        updateStatus(Status.RUNNING);
    }

    private void updateStatus(Status newStatus) {
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

    private void runJob() {
        List<TaskConfig> taskConfigs = null;
        do {
            jobContext.incrSplitTimes();
            JobSplitResponse response = null;
            try {
                response = tasksReceiver.receive();
            } catch (Exception e) {
                throw new ScheduleException("Failed to split job.", jobContext.getJob(), response.getMessage());
            }

            taskConfigs = response.getTaskConfigs();
            if (CollectionUtils.isNotEmpty(taskConfigs)) {
                for (TaskConfig taskConfig : taskConfigs) {
                    try {
                        taskScheduler.addTask(taskConfig);
                    } catch (InterruptedException e) {
                        logger.error("Failed to add task.", e);
                        break;
                    }
                }
            }

            if (response.isLast()) {
                break;
            }
        } while (taskConfigs != null && CollectionUtils.isNotEmpty(taskConfigs));

        taskScheduler.setJobSplitDone();
        waitForCompleted();
        reportResult();
    }

    private void waitForCompleted() {
        try {
            jobContext.getAllTaskFinishedLatch().await();
        } catch (InterruptedException e) {
            logger.error("Waiting for task completed is canceled.", e);
        }
    }

    private void reportResult() {
        int totalTasks = jobContext.getJobStatistics().getTotalTasks();
        int failedTasks = jobContext.getJobStatistics().getFailedTasks();
        int successTasks = jobContext.getJobStatistics().getSuccessTasks();
        if (totalTasks == successTasks) {
            logger.info("Job {} has been executed successful.", jobContext.getJob().getName());
            updateStatus(Status.SUCCESS);
        } else if (totalTasks == failedTasks) {
            logger.info("Job {} has been executed failed.", jobContext.getJob().getName());
            updateStatus(Status.FAILED);
        } else {
            logger.info("Job {} has been executed partially successful.", jobContext.getJob().getName());
            updateStatus(Status.PARTIAL_SUCCESS);
        }
    }

}