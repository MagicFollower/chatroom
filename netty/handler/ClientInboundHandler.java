package com.job.day23.nio.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String socketAddress = ctx.channel().localAddress().toString();
        System.out.println("启动成功："+ socketAddress);
        new Thread(()->{
            // 读取控制台输入
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String s = scanner.nextLine().trim();
                ctx.writeAndFlush(Unpooled.copiedBuffer(s.getBytes(StandardCharsets.UTF_8)));
            }
        },"scanner-"+socketAddress.substring(socketAddress.lastIndexOf(":")+1)).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf message = (ByteBuf) msg;
        System.out.println(message.toString(StandardCharsets.UTF_8));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
