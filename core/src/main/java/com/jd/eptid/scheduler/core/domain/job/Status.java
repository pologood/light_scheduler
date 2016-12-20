package com.jd.eptid.scheduler.core.domain.job;

/**
 * Created by classdan on 16-9-7.
 */
public enum Status {
    WAITING(0), RUNNING(1), PARTIAL_SUCCESS(2), SUCCESS(3), FAILED(4);

    private int code;

    Status(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static Status getStatus(int code) {
        for (Status type : Status.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new UnsupportedOperationException("Unsupported status code: " + code);
    }
}
