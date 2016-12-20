package com.jd.eptid.scheduler.client.core;

/**
 * Created by classdan on 16-10-31.
 */
public interface ConfigItem {
    String MASTER_SERVER_IP_KEY = "scheduler.server.master.ip";
    String BACKUP_SERVER_IP_KEY = "scheduler.server.backup.ip";
    String SERVICE_PORT_KEY = "scheduler.server.port";
    String JOB_SPLIT_POOL_SIZE = "job.split.pool.size";
    String TASK_EXECUTE_POOL_SIZE = "task.execute.pool.size";
}
