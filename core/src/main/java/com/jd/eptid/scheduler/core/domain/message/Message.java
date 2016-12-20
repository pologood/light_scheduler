package com.jd.eptid.scheduler.core.domain.message;

import java.util.UUID;

/**
 * Created by classdan on 16-9-12.
 */
public class Message {
    private String messageId;
    private int type;
    private String content;
    private long timestamp;

    public Message() {
        messageId = generateId();
        timestamp = System.currentTimeMillis();
    }

    public Message(MessageType type, String content) {
        this();
        this.type = type.getCode();
        this.content = content;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    public static Message buildHeartbeatMessage() {
        Message message = new Message();
        message.setType(MessageType.Heartbeat.getCode());
        message.setContent("NOP");
        return message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
