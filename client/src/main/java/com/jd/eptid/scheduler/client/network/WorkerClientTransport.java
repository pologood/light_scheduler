package com.jd.eptid.scheduler.client.network;

import com.google.common.base.Throwables;
import com.jd.eptid.scheduler.client.core.ClientContext;
import com.jd.eptid.scheduler.client.handler.ClientChannelHandler;
import com.jd.eptid.scheduler.core.decoder.MessageDecoder;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.node.Node;
import com.jd.eptid.scheduler.core.encoder.MessageEncoder;
import com.jd.eptid.scheduler.core.event.MasterChangedEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-9-14.
 */
public class WorkerClientTransport implements ClientTransport {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Bootstrap bootstrap;
    private Channel channel;
    private EventLoopGroup workerGroup;
    private CountDownLatch startedLatch = new CountDownLatch(1);

    public WorkerClientTransport() {
        ClientContext.getInstance().getEventBroadcaster().register(MasterChangedEvent.class, this);
    }

    @Override
    public synchronized void start() {
        initEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ChannelHandler[]{new IdleStateHandler(0, 5, 0), new MessageEncoder(), new MessageDecoder(1 * 1024 * 1024), new ClientChannelHandler()});
            }
        }).option(ChannelOption.SO_KEEPALIVE, true);

        startedLatch.countDown();
    }

    @Override
    public synchronized void connect() {
        try {
            startedLatch.await();
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }

        if (channel != null && channel.isActive()) {
            logger.warn("Client already connected to server.");
            return;
        }

        Node masterNode = ClientContext.getInstance().getMasterNode();
        if (masterNode == null) {
            logger.error("Master node not found, abandon the connection try.");
            return;
        }

        ChannelFuture channelFuture = bootstrap.connect(masterNode.getIp(), masterNode.getPort());
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("Connect to server successfully.");
                    channel = future.channel();
                } else {
                    logger.error("Failed to connect to server, try reconnect after 3s...");
                    future.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            connect();
                        }
                    }, 3, TimeUnit.SECONDS);
                }
            }
        });
    }

    @Override
    public synchronized void disconnect() {
        logger.info("Disconnect from the server...");
        if (channel != null) {
            channel.disconnect();
            channel = null;
        }
        logger.info("Disconnect from the server successful.");
    }

    private void initEventLoopGroup() {
        workerGroup = new NioEventLoopGroup();
    }

    @Override
    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public boolean isAlive() {
        return channel != null && channel.isActive();
    }

    @Override
    public void send(Message message) {
        channel.writeAndFlush(message);
    }

    @Override
    public void onEvent(MasterChangedEvent event) {
        Node oldMasterNode = ClientContext.getInstance().getMasterNode();
        Node newMasterNode = event.getMasterNode();
        ClientContext.getInstance().setMasterNode(newMasterNode);

        if (channel != null && channel.isActive()) {
            this.disconnect();
        }

        if (newMasterNode != null && !newMasterNode.equals(oldMasterNode)) {
            this.connect();
        }
    }
}
