package com.jd.eptid.scheduler.core.domain.task;

import com.alibaba.fastjson.JSON;

/**
 * Created by classdan on 16-9-22.
 */
public class TaskConfig<T> {
    public static final TaskConfig<Void> POISON = new TaskConfig<Void>();
    private String jobName;
    private int num;
    private T param;
    private int retryTimes = 0;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public T getParam() {
        return param;
    }

    public void setParam(T param) {
        this.param = param;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void incrRetryTimes() {
        ++this.retryTimes;
    }

    @Override
    public String toString() {
        return "{" +
                "jobName='" + jobName + '\'' +
                ", param=" + JSON.toJSONString(param) +
                '}';
    }
}
