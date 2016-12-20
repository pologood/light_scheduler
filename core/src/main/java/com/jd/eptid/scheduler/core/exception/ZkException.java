package com.jd.eptid.scheduler.core.exception;

import org.apache.zookeeper.KeeperException;

/**
 * Created by classdan on 16-11-9.
 */
public class ZkException extends RuntimeException {
    private ErrorCode errorCode;

    public enum ErrorCode {
        EXIST, NOT_EXIST, PARENT_NOT_EXIST, OTHER
    }

    public ZkException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public ZkException(KeeperException e) {
        super(e);
        switch (e.code()) {
            case NONODE:
                this.errorCode = ErrorCode.NOT_EXIST;
                break;
            case NODEEXISTS:
                this.errorCode = ErrorCode.EXIST;
                break;
            default:
                this.errorCode = ErrorCode.OTHER;
                break;
        }
    }

    public ZkException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode code() {
        return this.errorCode;
    }

    public static class ExistException extends ZkException {
        public ExistException(String path) {
            super(ErrorCode.EXIST, "Node [" + path + "] already exists.");
        }
    }

    public static class NotExistException extends ZkException {
        public NotExistException(String path) {
            super(ErrorCode.NOT_EXIST, "Node [" + path + "] not exist.");
        }
    }

    public static class ParentNotExistException extends ZkException {
        public ParentNotExistException(String parentPath) {
            super(ErrorCode.PARENT_NOT_EXIST, "Parent [" + parentPath + "] not exist.");
        }
    }

    public static class OtherException extends ZkException {
        public OtherException(String path, KeeperException.Code code) {
            super(ErrorCode.OTHER, "Node [" + path + "], code: " + code);
        }
    }

}
