package br.com.irineuantunes.hachinio.handlers;

import br.com.irineuantunes.hachinio.HachiNIOServer;
import br.com.irineuantunes.hachinio.util.ByteUtil;

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
        hachiNIOServer.getSocketMessageMap().get(channel).flip();

        /**
        System.out.println(result);
        System.out.println(ByteUtil.bytesToHex(server.socketMessageMap.get(channel).array()));
        */

        if(result == -1){
            try {
                channel.close();
                this.hachiNIOServer.getHandler().onDisconnect(channel);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        //
        // TODO parse protocol
        //
        hachiNIOServer.getSocketMessageMap().get(channel).clear();
        // TODO send header and message
        hachiNIOServer.getHandler().onMessage(channel, null, null);
        channel.read(hachiNIOServer.getSocketMessageMap().get(channel), channel, hachiNIOServer.getReadCompleteHandlerMap().get(channel));
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel ) {
        if(!(exc instanceof AsynchronousCloseException)){
            System.out.println( "fail to read message from client");
            exc.printStackTrace();
        }

    }
}