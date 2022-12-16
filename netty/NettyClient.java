package com.job.day23.nio.netty;

import com.job.day23.nio.netty.handler.ClientInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.time.LocalDateTime;

public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();

        /**
         * group
         * channel
         * option       →  handler
         * bind
         * cancel
         */
        new Bootstrap().group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ClientInboundHandler());
                    }
                }).connect("127.0.0.1", 6666).sync().addListener(f -> {
                    if (f.isSuccess()) {
                        System.out.println(LocalDateTime.now());
                        System.out.println("CONNECT OK~");
                    } else {
                        System.out.println("CONNECT ERROR!");
                    }
                }).channel().closeFuture().sync().addListener(f -> {
                    if (f.isSuccess()) {
                        System.out.println("CLOSE OK~");
                    } else {
                        System.out.println("CLOSE ERROR!");
                    }
                });
    }
}
