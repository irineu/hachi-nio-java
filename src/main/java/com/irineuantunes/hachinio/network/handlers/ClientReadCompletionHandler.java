package com.irineuantunes.hachinio.network.handlers;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ClientReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    @Override
    public void completed(Integer integer, AsynchronousSocketChannel asynchronousSocketChannel) {

    }

    @Override
    public void failed(Throwable throwable, AsynchronousSocketChannel asynchronousSocketChannel) {

    }
}
