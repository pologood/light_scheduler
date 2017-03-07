package com.jd.eptid.scheduler.server.network;

import com.jd.eptid.scheduler.core.common.ShutdownHook;
import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.decoder.MessageDecoder;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.encoder.MessageEncoder;
import com.jd.eptid.scheduler.core.event.EventBroadcaster;
import com.jd.eptid.scheduler.core.event.NetworkStateEvent;
import com.jd.eptid.scheduler.core.utils.NetworkUtils;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import com.jd.eptid.scheduler.server.handler.ServerChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by classdan on 16-9-14.
 */
public class SchedulerServerTransport implements ServerTransport {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int ORDER = 0;
    private ServerChannelHandler serverChannelHandler;
    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private CountDownLatch startedLatch = new CountDownLatch(1);
    private EventBroadcaster eventBroadcaster;

    public SchedulerServerTransport(EventBroadcaster eventBroadcaster, ServerChannelHandler serverChannelHandler) {
        this.eventBroadcaster = eventBroadcaster;
        this.serverChannelHandler = serverChannelHandler;
        ShutdownHook.getInstance().addLifeCycleObject(this);
    }

    @Override
    public void start() {
        logger.info("Start transport...");
        initEventLoopGroups();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ChannelHandler[]{new MessageEncoder(), new MessageDecoder(1 * 1024 * 1024), serverChannelHandler});
            }
        }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true);

        final int port = Configuration.getInteger(ConfigItem.SERVICE_PORT, 9188);
        final ChannelFuture channelFuture = serverBootstrap.bind(port);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("Transport start successful. Port:{}.", port);
                    serverChannel = future.channel();
                    startedLatch.countDown();
                    notifyReady();
                } else {
                    logger.error("Failed to start transport. Port:{}.", port, future.cause());
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        });
    }

    private void initEventLoopGroups() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
    }

    private void notifyReady() {
        eventBroadcaster.publish(new NetworkStateEvent(serverChannel, NetworkStateEvent.Code.READY));
    }

    @Override
    public void stop() {
        logger.info("Transport shutdown...");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public boolean isAlive() {
        return serverChannel != null && serverChannel.isActive();
    }

    @Override
    public ChannelFuture send(Channel channel, Message message) {
        checkStarted();
        Assert.notNull(channel);

        return channel.writeAndFlush(message);
    }

    @Override
    public Message send(Channel channel, Message message, int timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        checkStarted();
        Assert.notNull(channel);
        checkChannelStatus(channel);

        final ResponseFuture<Message> responseFuture = new ResponseFuture<Message>();
        responseFuture.setTimeout(timeUnit.toMillis(timeout));
        ChannelFuture channelFuture = channel.writeAndFlush(message);
        responseFuture.setChannelFuture(channelFuture);
        ResponseFuturePool.put(message.getMessageId(), responseFuture);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    responseFuture.setCause(future.cause());
                }
            }
        });
        return responseFuture.get(timeout, timeUnit);
    }

    private void checkChannelStatus(Channel channel) {
        if (!channel.isActive()) {
            throw new ChannelException("Channel is inactive. " + NetworkUtils.getIpAddress((InetSocketAddress) channel.remoteAddress()));
        }
    }

    private void checkStarted() {
        try {
            startedLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Transport is not ready.");
        }
    }

}
