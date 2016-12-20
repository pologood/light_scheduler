package com.jd.eptid.scheduler.server.network;

import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.utils.NetworkUtils;
import com.jd.eptid.scheduler.server.core.ClientManager;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by classdan on 16-9-27.
 */
@Component
public class HeartbeatChecker {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private ClientManager clientManager;
    private Map<ChannelHandlerContext, Integer> overtimeTimes = new ConcurrentHashMap<ChannelHandlerContext, Integer>();
    private final int maxOvertimeTimes = 3;

    public void check(ChannelHandlerContext ctx) {
        int overtime = getOvertimeTimes(ctx);
        if (overtime < maxOvertimeTimes) {
            sendHeartbeatMessage(ctx);
            incrOvertimeTimes(ctx);
        } else {
            logger.error("Receive heartbeat from client {} is overtime, discard it.", ctx.channel().remoteAddress());
            ctx.close();
            closeConnection(ctx);
        }
    }

    public void resetOvertimeTimes(ChannelHandlerContext ctx) {
        overtimeTimes.put(ctx, 0);
    }

    private void sendHeartbeatMessage(ChannelHandlerContext ctx) {
        Message message = new Message();
        message.setType(MessageType.Heartbeat.getCode());
        message.setContent("NOP");
        ctx.writeAndFlush(message);
    }

    private int getOvertimeTimes(ChannelHandlerContext ctx) {
        Integer overtime = overtimeTimes.get(ctx);
        return overtime == null ? 0 : overtime.intValue();
    }

    private void incrOvertimeTimes(ChannelHandlerContext ctx) {
        Integer overtime = overtimeTimes.get(ctx);
        overtimeTimes.put(ctx, overtime == null ? 1 : overtime.intValue() + 1);
    }

    private void closeConnection(ChannelHandlerContext ctx) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        clientManager.unregister(NetworkUtils.getIpAddress(inetSocketAddress), NetworkUtils.getPort(inetSocketAddress));
    }

}
