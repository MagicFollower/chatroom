package com.job.day23.nio.src;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;

public class Client {
    private Selector selector;
    private SocketChannel socketChannel;
    private final ByteBuffer buffer = ByteBuffer.allocate(512);

    public Client() {
        try {
            final int PORT = 6666;
            selector = Selector.open();
            socketChannel = SocketChannel.open(new InetSocketAddress(PORT));
            // socketChannel#read是否会阻塞（设置为false）
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            throw new RuntimeException("无法连接至服务器，请稍后再试。");
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        new Thread(() -> {
            for (; ; ) {
                client.readData();
            }
        }, "th1").start();

        // 读取控制台输入
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextLine()) {
            String s = scanner.nextLine().trim();
            client.sendData(s);
        }
    }

    public void readData() {
        try {
            if(selector.select()>0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                if(iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel().configureBlocking(false);
                        socketChannel.configureBlocking(false);
                        int read = socketChannel.read(buffer.clear());
                        if (read > 0) {
                            System.out.println(new String(buffer.array(), 0, read).trim());
                        }
                    }
                    iterator.remove();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("与远程服务器失去连接，请稍后再试。");
        }
    }

    public void sendData(String msg) {
        try {
            socketChannel.write(buffer.clear().put(msg.getBytes()).flip());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
