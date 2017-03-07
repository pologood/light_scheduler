package com.jd.eptid.scheduler.core.domain.task;

/**
 * Created by classdan on 17-1-13.
 */
public enum TaskStatus {
    WAITING(0), RUNNING(1), SUCCESS(3), FAILED(4), FORCE_STOP(5);

    private int code;

    TaskStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static TaskStatus getStatus(int code) {
        for (TaskStatus type : TaskStatus.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new UnsupportedOperationException("Unsupported status code: " + code);
    }
}
