package com.jd.eptid.scheduler.server.web.response;

import com.jd.eptid.scheduler.core.domain.job.Job;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by classdan on 17-2-7.
 */
public class SchedulingView {
    private Job job;
    private Map<String, ScheduleInfo> scheduleInfo = new HashMap<String, ScheduleInfo>();

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Map<String, ScheduleInfo> getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(Map<String, ScheduleInfo> scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }
}
