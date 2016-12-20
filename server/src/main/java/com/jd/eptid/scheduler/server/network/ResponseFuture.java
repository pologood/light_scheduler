package com.jd.eptid.scheduler.server.network;

import io.netty.channel.ChannelFuture;

import java.util.concurrent.*;

/**
 * Created by classdan on 16-9-22.
 */
public class ResponseFuture<T> implements Future<T> {
    private CountDownLatch latch = new CountDownLatch(1);
    private T response;
    private long sendTime = System.currentTimeMillis();
    private long timeout;
    private ChannelFuture channelFuture;
    private Throwable cause;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return response != null;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        latch.await();
        return response;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        if (response == null) {
            throw new TimeoutException("Response time out.");
        }
        return response;
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - sendTime > timeout;
    }

    public void setResponse(T response) {
        this.response = response;
        latch.countDown();
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }
}
