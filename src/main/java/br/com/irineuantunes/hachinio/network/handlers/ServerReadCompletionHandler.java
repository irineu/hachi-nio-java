package br.com.irineuantunes.hachinio.network.handlers;

import br.com.irineuantunes.hachinio.HachiNIOServer;
import br.com.irineuantunes.hachinio.network.handlers.protocol.NIOProtocolReader;
import br.com.irineuantunes.hachinio.network.HachiNIOConnection;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ServerReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {

    HachiNIOServer hachiNIOServer;

    public ServerReadCompletionHandler(HachiNIOServer hachiNIOServer) {
        this.hachiNIOServer = hachiNIOServer;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel  ) {
        HachiNIOConnection connection = hachiNIOServer.getConnectionMap().get(channel);
        connection.getSocketByteBuffer().flip();

        //System.out.println(socketData.getSocketByteBuffer().hasRemaining());
        //System.out.println(socketData.getSocketByteBuffer().array());

        if(result == -1){
            try {
                channel.close();
                this.hachiNIOServer.getHandler().onDisconnect(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        NIOProtocolReader.read(connection, hachiNIOServer);

        //clear buff
        connection.getSocketByteBuffer().clear();

        //try read next buff
        channel.read(connection.getSocketByteBuffer(), channel, connection.getReadCompleteHandler());
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel ) {
        if(!(exc instanceof AsynchronousCloseException)){
            System.out.println( "fail to read message from client");
            exc.printStackTrace();
        }

    }
}