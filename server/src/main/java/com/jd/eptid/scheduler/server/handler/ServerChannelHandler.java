package com.jd.eptid.scheduler.server.handler;

import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.processor.MessageProcessor;
import com.jd.eptid.scheduler.core.utils.NetworkUtils;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.eptid.scheduler.server.network.ChannelHolder;
import com.jd.eptid.scheduler.server.network.HeartbeatChecker;
import com.jd.eptid.scheduler.server.network.ResponseFuture;
import com.jd.eptid.scheduler.server.network.ResponseFuturePool;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by classdan on 16-9-14.
 */
@Component
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private ClientManager clientManager;
    @Resource
    private HeartbeatChecker heartbeatChecker;
    @Resource(name = "messageProcessors")
    private Map<MessageType, MessageProcessor> messageProcessors = new HashMap<MessageType, MessageProcessor>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Client {} connected.", ctx.channel().remoteAddress());
        ChannelHolder.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Client {} disconnected.", ctx.channel().remoteAddress());
        ChannelHolder.removeChannel(ctx.channel());

        unregister(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        ResponseFuture<Message> responseFuture = ResponseFuturePool.getResponseFuture(message.getMessageId());
        if (responseFuture != null) {
            responseFuture.setResponse(message);
            return;
        }

        MessageType messageType = MessageType.getMessageType(message.getType());
        MessageProcessor messageProcessor = messageProcessors.get(messageType);
        if (messageProcessor != null) {
            messageProcessor.process(message, ctx);
        } else {
            logger.warn("No message processor found for message: {}, discard it.", message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        if (cause instanceof IOException && ctx.channel().isActive()) {
            ctx.close();
            unregister(ctx);
        }
    }

    private void unregister(ChannelHandlerContext ctx) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        clientManager.unregister(NetworkUtils.getIpAddress(inetSocketAddress), NetworkUtils.getPort(inetSocketAddress));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                heartbeatChecker.check(ctx);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    public void setMessageProcessors(Map<MessageType, MessageProcessor> messageProcessors) {
        this.messageProcessors = messageProcessors;
    }
}
