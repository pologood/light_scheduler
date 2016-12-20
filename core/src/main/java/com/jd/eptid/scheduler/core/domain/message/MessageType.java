package com.jd.eptid.scheduler.core.domain.message;

/**
 * Created by ClassDan on 2016/9/18.
 */
public enum MessageType {
    Heartbeat(0), Hello(1), Task_Split(2), Task_Run(3), Bye(4);

    private int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static MessageType getMessageType(int code) {
        for (MessageType type : MessageType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new UnsupportedOperationException("Unsupported message type code: " + code);
    }
}
