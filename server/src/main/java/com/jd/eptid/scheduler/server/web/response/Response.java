package com.jd.eptid.scheduler.server.web.response;

/**
 * Created by classdan on 16-10-11.
 */
public class Response<T> {
    private boolean success;
    /**
     * 消息
     */
    private String message;
    /**
     * 数据
     */
    private T data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> Response<T> successResponse(T data) {
        Response<T> response = new Response<T>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> Response<T> failureResponse(String message) {
        Response<T> response = new Response<T>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
