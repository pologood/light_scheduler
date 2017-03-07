package com.jd.eptid.scheduler.core.event;

import com.jd.eptid.scheduler.core.domain.job.Job;

/**
 * Created by classdan on 17-1-4.
 */
public class JobEvent extends AbstractEvent {
    private Code code;

    public JobEvent(Job job, Code code) {
        super();
        setSource(job);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public enum Code {
        NEW,
        ENABLE,
        DISABLE,
        UPDATE,
        REMOVE
    }

    @Override
    public String toString() {
        return "JobEvent{" +
                "job=" + source() +
                "code=" + code +
                '}';
    }
}
