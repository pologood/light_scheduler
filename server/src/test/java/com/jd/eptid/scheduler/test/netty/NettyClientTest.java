package com.jd.eptid.scheduler.test.netty;

import com.alibaba.fastjson.JSON;
import com.jd.eptid.scheduler.core.decoder.MessageDecoder;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.encoder.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-9-12.
 */
public class NettyClientTest {

    public static class Client {
        private Channel channel;
        private EventLoopGroup workerGroup;

        public void connect(String ip, int port) throws InterruptedException {
            workerGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new MessageEncoder());
                    ch.pipeline().addLast(new MessageDecoder(1024));
                    ch.pipeline().addLast(new ClientChannelHandler());
                }
            }).option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
            channel = channelFuture.channel();
        }

        public void shutdown() throws InterruptedException {
            channel.closeFuture();
            workerGroup.shutdownGracefully();
        }

        public void send(Message message) throws InterruptedException {
            ChannelFuture future = channel.writeAndFlush(message);
            future.sync();
        }

    }

    public static class ClientChannelHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Connect server successful.");

            /*Header header = new Header();
            header.setLength(200);
            Message message = new Message(header, "Hi, server.");
            ctx.writeAndFlush(message);*/
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Connection with server has broken.");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Message message = (Message) msg;
            System.out.println(message);

            Message response = new Message();
            response.setType(MessageType.Task_Split.getCode());
            response.setContent("hello");
            ctx.writeAndFlush(response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    @Test
    public void test() throws InterruptedException, ParseException {
        Client client = new Client();
        client.connect("127.0.0.1", 9188);

        Message message = new Message();
        message.setType(MessageType.Hello.getCode());
        Set<String> supportedJobs = new HashSet<String>(Arrays.asList("job1", "job2"));
        message.setContent(JSON.toJSONString(supportedJobs));
        client.send(message);

        TimeUnit.HOURS.sleep(1);
        client.shutdown();
    }

}
