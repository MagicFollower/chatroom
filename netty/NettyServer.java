package com.job.day23.nio.netty;

import com.job.day23.nio.netty.handler.ServerInboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NettyServer {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);
        List<ChannelHandlerContext> onlineClients = new CopyOnWriteArrayList<>();

        new ServerBootstrap().group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ServerInboundHandler(onlineClients));
                    }
                }).bind(6666).sync().addListener(f -> {
                    if (f.isSuccess()) {
                        System.out.println("BIND OK!");
                    } else {
                        System.out.println("BIND ERROR!");
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
