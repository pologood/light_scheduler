package com.jd.eptid.scheduler.server.processor;

import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.processor.MessageProcessor;
import com.jd.eptid.scheduler.core.utils.NetworkUtils;
import com.jd.eptid.scheduler.server.core.AppContext;
import com.jd.eptid.scheduler.server.core.ClientManager;
import com.jd.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * Created by classdan on 16-9-26.
 */
@Component
public class HelloMessageProcessor implements MessageProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private ClientManager clientManager;

    public HelloMessageProcessor() {
        clientManager = AppContext.getInstance().getClientManager();
    }

    @Override
    public void process(Message message, ChannelHandlerContext ctx) {
        Assert.isTrue(message.getType() == MessageType.Hello.getCode());

        Set<String> supportedJobs = JSON.parseObject(message.getContent(), Set.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = NetworkUtils.getIpAddress(inetSocketAddress);
        int port = NetworkUtils.getPort(inetSocketAddress);
        clientManager.register(ip, port, supportedJobs);
        logger.info("Client is found. ip: {}, port: {}, supportedJobs: {}.", ip, port, supportedJobs);
    }

}
