package br.com.irineuantunes.hachinio.network.handlers;

import br.com.irineuantunes.hachinio.HachiNIOServer;

import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ServerWriteCompletionHandler  implements CompletionHandler<Integer, AsynchronousSocketChannel> {

    private HachiNIOServer hachiNIOServer;

    public ServerWriteCompletionHandler(HachiNIOServer hachiNIOServer) {
        this.hachiNIOServer = hachiNIOServer;
    }

    @Override
    public void completed(Integer integer, AsynchronousSocketChannel asynchronousSocketChannel) {
        //finish to write message to client, nothing to do
        this.hachiNIOServer.getHandler().onWritten(this.hachiNIOServer.getConnectionMap().get(asynchronousSocketChannel));
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel asynchronousSocketChannel) {
        if(!(exc instanceof AsynchronousCloseException)){
            System.out.println( "fail to read message from client");
            exc.printStackTrace();
        }
    }

    /*private void startWrite( AsynchronousSocketChannel clientSockChannel, final ByteBuffer buf) {
        HachiNIOServer instance = this;

        clientSockChannel.write(buf, clientSockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel >() {

            @Override
            public void completed(Integer result, AsynchronousSocketChannel clientSockChannel) {

            }

            @Override
            public void failed(Throwable ex, AsynchronousSocketChannel clientSockChannel) {

            }

        });
    }*/
}
