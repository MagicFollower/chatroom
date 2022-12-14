package com.job.day23.nio.src;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class Server {

    private final ByteBuffer buffer = ByteBuffer.allocate(512);
    private ExecutorService THREAD_POOL;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private Queue<SocketChannel> blockingQueue;
    private List<SocketChannel> onlineSocket;

    public Server() {
        try {
            final int PORT = 6666;
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            final int ACTIVE_THREAD_COUNT = 2;
            THREAD_POOL = new ThreadPoolExecutor(
                    ACTIVE_THREAD_COUNT,
                    ACTIVE_THREAD_COUNT,
                    0,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>()
            );
            blockingQueue = new ArrayBlockingQueue<>(100);
            onlineSocket = new CopyOnWriteArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.listen();
    }

    public void listen() throws IOException {
        while (true) {
            if (selector.select(2000) > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                if (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        // todo ?????????????????? (finished)
                        onlineSocket.add(socketChannel);
                        System.out.println(socketChannel.getRemoteAddress() + "??????!");
                        // ???socketChannel??????????????????
                        blockingQueue.add(socketChannel);
                        // todo ??????????????????????????????????????????????????? (finished)
                        // tips: THREAD_POOL???????????????2?????????????????????????????????????????????????????????CompletableFuture.runAsync??????????????????????????????????????????
                        if (((ThreadPoolExecutor) THREAD_POOL).getActiveCount() != 2) {
                            CompletableFuture.runAsync(() -> {
                                try {
                                    Selector selector = Selector.open();
                                    for (; ; ) {
                                        if (blockingQueue.peek() != null) {
                                            SocketChannel item = blockingQueue.poll();
                                            item.configureBlocking(false);
                                            item.register(selector, SelectionKey.OP_READ);
                                        }
                                        // bug ??? channel??????close()????????????????????????select????????????????????????
                                        // server?????????????????????????????????????????????????????????????????????????????????????????????channel??????open???
                                        // ??? ????????????????????? ??? ?????????????????????????????????????????????????????????????????????????????????????????????close???
                                        if (selector.select(2000) > 0) {
                                            Iterator<SelectionKey> ri = selector.selectedKeys().iterator();
                                            if (ri.hasNext()) {
                                                SelectionKey next = ri.next();
                                                if (next.isReadable()) {
                                                    SocketChannel channel = (SocketChannel) next.channel();
                                                    ri.remove();
                                                    try {
                                                        int read = channel.read(buffer.clear());
                                                        String msg = channel.getRemoteAddress().toString().substring(1)
                                                                + ": " +
                                                                new String(buffer.array(), 0, read, StandardCharsets.UTF_8);
                                                        if (read > 0) {
                                                            // System.out.println("??????????????????(" +
                                                            //         channel.getRemoteAddress().toString() + ")?????????" + msg);
                                                            // todo ?????????????????????????????????????????????finished???
                                                            resendMsgToAll(msg, channel);
                                                        } else {
                                                            System.out.println((channel.getRemoteAddress() + "??????\uD83D\uDE80!"));
                                                            // todo server?????????????????? (finished)
                                                            if (channel.isOpen()) channel.close();
                                                            onlineSocket.remove(channel);
                                                        }
                                                    } catch (IOException e) {
                                                        System.out.println((channel.getRemoteAddress() + "??????\uD83D\uDE80!!"));
                                                        if (channel.isOpen()) channel.close();
                                                        onlineSocket.remove(channel);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }, THREAD_POOL);
                        }
                    }
                    iterator.remove();
                }
            }
        }
    }


    public void resendMsgToAll(String msg, SocketChannel self) throws IOException {
        buffer.clear().put(msg.getBytes());
        for (SocketChannel u : onlineSocket) {
            if (u != self) {
                u.write(buffer.flip());
            }
        }
    }
}
