package com.job.day23.nio.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServerInboundHandler extends ChannelInboundHandlerAdapter {

    List<ChannelHandlerContext> onlineClients;

    public ServerInboundHandler(List<ChannelHandlerContext> onlineClients) {
        this.onlineClients = onlineClients;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        onlineClients.add(ctx);
        System.out.println("新客户端接入，当前在线用户数：" + onlineClients.size());
//        onlineClients.stream().map(ChannelHandlerContext::channel).forEach(x -> {
//            System.out.println(x.remoteAddress());
//        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String s = ((ByteBuf) msg).toString(StandardCharsets.UTF_8);
        String clientAddress = ctx.channel().remoteAddress().toString().substring(1);
        String prepareMsg = clientAddress + ": " + s;
        System.out.println("接受到消息：" + prepareMsg);
        onlineClients.stream().filter(x -> x != ctx).forEach(x -> {
            // System.out.println("转发消息至" + x.channel().remoteAddress() + ": " + prepareMsg);
            x.channel().eventLoop().execute(()->{
                x.writeAndFlush(Unpooled.copiedBuffer(prepareMsg.getBytes(StandardCharsets.UTF_8)));
            });
        });

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        onlineClients.remove(ctx);
        ctx.close();
    }
}
