package com.irineuantunes.hachinio;

import com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.irineuantunes.hachinio.network.handlers.ClientReadCompletionHandler;
import com.irineuantunes.hachinio.network.handlers.ClientWriteCompletionHandler;
import com.irineuantunes.hachinio.network.handlers.HachiNIOHandler;
import com.irineuantunes.hachinio.util.ProcessUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class HachiNIOClient implements HachiNIO {

    private String srvAddr;
    private int srvPort;
    private HachiNIOHandler handler;
    private AsynchronousSocketChannel sockChannel;
    private HachiNIOConnection connection;
    private boolean isAlive = false;

    public HachiNIOClient(String srvAddr, int srvPort, HachiNIOHandler handler){
        this.srvAddr = srvAddr;
        this.srvPort = srvPort;
        this.handler = handler;
    }

    public void connect() throws IOException {
        sockChannel = AsynchronousSocketChannel.open();
        sockChannel.connect( new InetSocketAddress(this.srvAddr, this.srvPort), sockChannel, new CompletionHandler<Void, AsynchronousSocketChannel >() {

            @Override
            public void completed(Void unused, AsynchronousSocketChannel asyncSocketChannel) {
                connection = new HachiNIOConnection(ByteBuffer.allocate(512), new ClientReadCompletionHandler(), new ClientWriteCompletionHandler(), asyncSocketChannel);

                handler.onConnect(connection);

                asyncSocketChannel.read(
                        connection.getSocketByteBuffer(),
                        asyncSocketChannel,
                        connection.getReadCompleteHandler()
                );
            }

            @Override
            public void failed(Throwable ex, AsynchronousSocketChannel asynchronousSocketChannel) {
                handler.onClientError(ex, connection);
                isAlive = false;
            }
        });

        this.isAlive = true;
        ProcessUtil.attach(this);
    }

    @Override
    public boolean isActive() {
        return this.isAlive;
    }

    @Override
    public void stop() throws IOException {
        this.sockChannel.close();
        this.isAlive = false;
    }
}