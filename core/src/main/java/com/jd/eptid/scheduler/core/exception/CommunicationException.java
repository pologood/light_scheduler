package com.jd.eptid.scheduler.core.exception;

/**
 * Created by ClassDan on 2016/9/15.
 */
public class CommunicationException extends RuntimeException {

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

}
