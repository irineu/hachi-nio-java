package com.irineuantunes.hachinio.network.handlers;

import java.nio.channels.AsynchronousServerSocketChannel;

public interface HachiNIOServerHandler extends HachiNIOHandler {
    void onServerError(Throwable ex, AsynchronousServerSocketChannel serverSockChannel);
}
