package com.jd.eptid.scheduler.core.encoder;

import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.serialize.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.springframework.util.Assert;

/**
 * Created by classdan on 16-9-14.
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        Assert.notNull(msg);

        byte[] bytes = ProtostuffUtil.serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
        ctx.flush();
    }

}
