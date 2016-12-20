package com.jd.eptid.scheduler.test.netty;

import com.jd.eptid.scheduler.core.decoder.MessageDecoder;
import com.jd.eptid.scheduler.core.domain.message.Header;
import com.jd.eptid.scheduler.core.domain.message.Message;
import com.jd.eptid.scheduler.core.domain.message.MessageType;
import com.jd.eptid.scheduler.core.encoder.MessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by classdan on 16-9-12.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:spring-config-beans.xml"})
public class NettyServerTest {

    public static class Server {
        private EventLoopGroup bossGroup;
        private EventLoopGroup workerGroup;
        private ConcurrentMap<String, Channel> clientChannels = new ConcurrentHashMap<String, Channel>();

        public void start(int port) {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            final Server thisServer = this;
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new MessageEncoder());
                    ch.pipeline().addLast(new MessageDecoder(1024));
                    ch.pipeline().addLast(new ServerChannelHandler(thisServer));
                }
            }).option(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.bind(port);
        }

        public void shutdown() {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

        public void send(String clientId, Message message) {
            Channel channel = clientChannels.get(clientId);
            if (channel == null || !channel.isOpen()) {
                System.out.println("Channel is unavailable.");
                return;
            }
            channel.writeAndFlush(message);
        }

        public void addClient(String id, Channel channel) {
            clientChannels.put(id, channel);
        }

        public void removeClient(String id) {
            clientChannels.remove(id);
        }
    }

    public static class ServerChannelHandler extends ChannelInboundHandlerAdapter {
        private Server server;

        public ServerChannelHandler(Server server) {
            super();
            this.server = server;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String clientId = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
            System.out.println("Client [" + clientId + "] connected.");
            server.addClient(clientId, ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String clientId = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
            System.out.println("Client disconnected...");
            server.removeClient(clientId);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Message message = (Message) msg;
            System.out.println(message);

            Message response = null;
            switch (message.getType()) {
                case 1:
                    response = new Message(MessageType.Hello, "Hi, client.");
                    break;
                default:
                    response = new Message(MessageType.Hello, "NOP");
                    break;
            }
            ctx.writeAndFlush(response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    @Test
    public void test() throws InterruptedException, UnsupportedEncodingException {
        Server server = new Server();
        server.start(9188);

        /*TimeUnit.SECONDS.sleep(10);

        Header header = new Header();
        header.setLength(200);
        header.setHeaderLength(12);
        Message message = new Message(MessageType.Hello, "A good day.");
        server.send("127.0.0.1", message);*/

        TimeUnit.HOURS.sleep(1);
        server.shutdown();
    }

}
