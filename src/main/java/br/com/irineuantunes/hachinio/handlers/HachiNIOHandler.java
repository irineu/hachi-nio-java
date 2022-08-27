package br.com.irineuantunes.hachinio.handlers;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public interface HachiNIOHandler {

    void onConnect(AsynchronousSocketChannel channel);
    void onDisconnect(AsynchronousSocketChannel channel);
    void onClientError(Throwable ex, AsynchronousSocketChannel channel);
    void onServerError(Throwable ex, AsynchronousServerSocketChannel serverSockChannel);
    void onMessage(AsynchronousSocketChannel channel, Map header, byte[] message);

}
