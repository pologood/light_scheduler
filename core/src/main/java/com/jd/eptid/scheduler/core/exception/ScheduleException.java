package com.jd.eptid.scheduler.core.exception;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by classdan on 16-9-21.
 */
public class ScheduleException extends RuntimeException {
    private Object[] contextObjects;

    public ScheduleException(String message) {
        super(message);
    }

    public ScheduleException(String message, Object... contextObjects) {
        super(message);
        this.contextObjects = contextObjects;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + formatParams();
    }

    private String formatParams() {
        if (ArrayUtils.isEmpty(contextObjects)) {
            return StringUtils.EMPTY;
        }

        StringBuilder paramsBuilder = new StringBuilder("[");
        int i = 0;
        for (Object param : contextObjects) {
            if (i++ > 0) {
                paramsBuilder.append(", ");
            }
            paramsBuilder.append(param.toString());
        }
        paramsBuilder.append("]");
        return paramsBuilder.toString();
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
}
