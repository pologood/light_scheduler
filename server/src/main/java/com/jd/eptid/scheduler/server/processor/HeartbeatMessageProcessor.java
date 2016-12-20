package com.jd.eptid.scheduler.server.processor;

import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.processor.MessageProcessor;
import com.jd.eptid.scheduler.server.network.HeartbeatChecker;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * Created by classdan on 16-9-26.
 */
@Component
public class HeartbeatMessageProcessor implements MessageProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private HeartbeatChecker heartbeatChecker;

    @Override
    public void process(Message message, ChannelHandlerContext ctx) {
        Assert.isTrue(message.getType() == MessageType.Heartbeat.getCode());

        logger.debug("Received heartbeat: {}.", ctx.channel().remoteAddress());
        heartbeatChecker.resetOvertimeTimes(ctx);
        responseHeartbeat(ctx);
    }

    private void responseHeartbeat(ChannelHandlerContext ctx) {
        Message message = new Message();
        message.setType(MessageType.Heartbeat.getCode());
        message.setContent("NOP");
        ctx.writeAndFlush(message);
    }
}
