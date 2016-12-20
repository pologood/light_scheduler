package com.jd.eptid.scheduler.server.network;

import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.network.Transport;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by classdan on 16-9-14.
 */
public interface ServerTransport extends Transport {

    ChannelFuture send(Channel channel, Message message);

    Message send(Channel channel, Message message, int timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException;

}
