package com.jd.eptid.scheduler.core.failover;

/**
 * Created by classdan on 17-1-20.
 */
public enum FailoverPolicy {
    NONE(0), RETRY(1), MANUAL(2), ALARM(3);

    private int code;

    FailoverPolicy(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static FailoverPolicy getFailoverPolicy(int code) {
        for (FailoverPolicy type : FailoverPolicy.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new UnsupportedOperationException("Unsupported failover policy code: " + code);
    }
}
