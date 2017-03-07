package com.jd.eptid.scheduler.core.domain.job;

/**
 * Created by classdan on 16-9-7.
 */
public enum JobStatus {
    WAITING(0), RUNNING(1), PARTIAL_SUCCESS(2), SUCCESS(3), FAILED(4), FORCE_STOP(5), CANCELED(6);

    private int code;

    JobStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static JobStatus getStatus(int code) {
        for (JobStatus type : JobStatus.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new UnsupportedOperationException("Unsupported status code: " + code);
    }
}
