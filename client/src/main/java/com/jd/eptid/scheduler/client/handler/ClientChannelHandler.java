package com.jd.eptid.scheduler.client.handler;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.client.core.ClientContext;
import com.jd.eptid.scheduler.client.network.ClientTransport;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.event.NetworkStateEvent;
import com.jd.eptid.scheduler.core.processor.MessageProcessor;
import com.jd.eptid.scheduler.core.utils.NetworkUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * Created by classdan on 16-9-27.
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int maxOvertimeTimes = 3;
    private ClientTransport clientTransport;
    private int overtimeTimes = 0;

    public ClientChannelHandler() {
        clientTransport = ClientContext.getInstance().getClientTransport();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Register on server...");
        notifyConnected(ctx.channel());
        updateLocalNode(ctx.channel());
        sendHelloMessage(ctx);
    }

    private void updateLocalNode(Channel channel) {
        Pair<String, Integer> ipAndPort = NetworkUtils.getIpAndPort(channel);
        ClientContext.getInstance().changeNode(ipAndPort.getLeft(), ipAndPort.getRight());
    }

    private void notifyConnected(Channel channel) {
        ClientContext.getInstance().getEventBroadcaster().publish(new NetworkStateEvent(channel, NetworkStateEvent.Code.READY));
    }

    private void sendHelloMessage(ChannelHandlerContext ctx) {
        Set<String> jobNames = ClientContext.getInstance().getJobs().keySet();

        Message message = new Message();
        message.setType(MessageType.Hello.getCode());
        message.setContent(JSON.toJSONString(jobNames));
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("Disconnected from the server. Try reconnect it...");
        notifyDisconnected(ctx.channel());
        ClientContext.getInstance().getClientRegistry().unregister();
        clientTransport.connect();
    }

    private void notifyDisconnected(Channel channel) {
        ClientContext.getInstance().getEventBroadcaster().publish(new NetworkStateEvent(channel, NetworkStateEvent.Code.UNAVAILABLE));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        MessageType type = MessageType.getMessageType(message.getType());
        if (type == MessageType.Heartbeat) {
            logger.debug("Received server's heartbeat: {}.", ctx.channel().remoteAddress());
            overtimeTimes = 0;
            return;
        }

        MessageProcessor messageProcessor = ClientContext.getInstance().getMessageProcessor(type);
        if (messageProcessor != null) {
            messageProcessor.process(message, ctx);
        } else {
            logger.warn("No message processor found for message: {}, discard it.", message);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                if (overtimeTimes < maxOvertimeTimes) {
                    sendHeartbeatMessage(ctx);
                    ++overtimeTimes;
                } else {
                    logger.error("Receive heartbeat from server {} is overtime, close the connection.", ctx.channel().remoteAddress());
                    ctx.close();
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void sendHeartbeatMessage(ChannelHandlerContext ctx) {
        logger.debug("Send heartbeat to server...");
        Message message = Message.buildHeartbeatMessage();
        ctx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        if (cause instanceof IOException && ctx.channel().isActive()) {
            ctx.close();
        }
    }

}
