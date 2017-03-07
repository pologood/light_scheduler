package com.jd.eptid.scheduler.core.event;

/**
 * Created by classdan on 17-1-11.
 */
public class ScheduleEvent<T> extends AbstractEvent {
    private String scheduleId;
    private Code code;

    public ScheduleEvent(T source, String scheduleId, Code code) {
        setSource(source);
        this.scheduleId = scheduleId;
        this.code = code;
    }

    public T source() {
        return (T) super.source();
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public enum Code {
        SUBMIT,
        PROGRESS,
        DONE
    }
}
