package com.irineuantunes.hachinio.network.handlers;

import com.irineuantunes.hachinio.HachiNIOClient;

import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ClientWriteCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {

    private HachiNIOClient hachiNIOClient;

    public ClientWriteCompletionHandler(HachiNIOClient hachiNIOClient) {
        this.hachiNIOClient = hachiNIOClient;
    }

    @Override
    public void completed(Integer integer, AsynchronousSocketChannel asynchronousSocketChannel) {
        this.hachiNIOClient.getHandler().onWritten(this.hachiNIOClient.getConnection());
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel asynchronousSocketChannel) {
        if(!(exc instanceof AsynchronousCloseException)){
            exc.printStackTrace();
        }
    }
}
