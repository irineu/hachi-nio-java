package com.irineuantunes.hachinio.network.handlers;

import com.irineuantunes.hachinio.HachiNIOClient;
import com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.irineuantunes.hachinio.network.handlers.protocol.NIOProtocolReader;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ClientReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {

    private HachiNIOClient hachiNIOClient;

    public ClientReadCompletionHandler(HachiNIOClient hachiNIOClient) {
        this.hachiNIOClient = hachiNIOClient;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel) {
        HachiNIOConnection connection = hachiNIOClient.getConnection();
        connection.getSocketByteBuffer().flip();

        if(result == -1){
            try {
                channel.close();
                this.hachiNIOClient.stop();
                this.hachiNIOClient.getHandler().onDisconnect(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        NIOProtocolReader.read(connection, this.hachiNIOClient);

        //clear buff
        connection.getSocketByteBuffer().clear();

        //try read next buff
        channel.read(connection.getSocketByteBuffer(), channel, connection.getReadCompleteHandler());
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel asynchronousSocketChannel) {
        if(!(exc instanceof AsynchronousCloseException)){
            exc.printStackTrace();
        }
    }
}
