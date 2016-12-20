package com.jd.eptid.scheduler.server.network;

import com.jd.eptid.scheduler.core.config.Configuration;
import com.jd.eptid.scheduler.core.decoder.MessageDecoder;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.encoder.MessageEncoder;
import com.jd.eptid.scheduler.core.network.AbstractTransport;
import com.jd.eptid.scheduler.server.config.ConfigItem;
import com.jd.eptid.scheduler.server.handler.ServerChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by classdan on 16-9-14.
 */
@Component
public class SchedulerServerTransport extends AbstractTransport<Channel> implements ServerTransport {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private ServerChannelHandler serverChannelHandler;
    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private CountDownLatch startedLatch = new CountDownLatch(1);

    @Override
    public void start() {
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
                    logger.info("Server start successful. Port:{}.", port);
                    serverChannel = future.channel();
                    startedLatch.countDown();
                    notifyReadyListeners(serverChannel);
                } else {
                    logger.info("Failed to start server. Port:{}.", port);
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

    @Override
    public void shutdown() {
        logger.info("Server shutdown...");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
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

    private void checkStarted() {
        try {
            startedLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Transport is not ready.");
        }
    }

}
