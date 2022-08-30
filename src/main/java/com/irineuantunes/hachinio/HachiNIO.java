package com.irineuantunes.hachinio;

import com.irineuantunes.hachinio.network.handlers.HachiNIOHandler;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public interface HachiNIO {

    boolean isActive();
    HachiNIOHandler getHandler();
    void stop() throws IOException;
}
