package com.jd.eptid.scheduler.core.processor;

import com.jd.eptid.scheduler.core.domain.message.Message;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by classdan on 16-9-26.
 */
public interface MessageProcessor {

    void process(Message message, ChannelHandlerContext ctx);

}
