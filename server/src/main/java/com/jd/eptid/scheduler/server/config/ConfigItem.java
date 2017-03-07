package com.jd.eptid.scheduler.server.config;

/**
 * Created by classdan on 16-10-31.
 */
public interface ConfigItem {
    String SERVICE_PORT = "scheduler.server.service.port";
    String BIZ_POOL_SIZE = "scheduler.server.job.pool.size";
    String JOB_SPLIT_TIMEOUT = "scheduler.server.job.split.timeout";
    String TASK_RUN_TIMEOUT = "scheduler.server.task.run.timeout";
    String JOB_RETRY_TIMES = "scheduler.server.job.retry.times";
    String TASK_RETRY_TIMES = "scheduler.server.task.retry.times";
}
