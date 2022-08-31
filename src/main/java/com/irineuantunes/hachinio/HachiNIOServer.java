package com.irineuantunes.hachinio;

import com.irineuantunes.hachinio.network.handlers.HachiNIOServerHandler;
import com.irineuantunes.hachinio.network.handlers.ServerReadCompletionHandler;
import com.irineuantunes.hachinio.network.handlers.ServerWriteCompletionHandler;
import com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.irineuantunes.hachinio.util.ProcessUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class HachiNIOServer implements HachiNIO {

    protected Map<AsynchronousSocketChannel, HachiNIOConnection> connectionMap;
    protected HachiNIOServerHandler handler;
    protected String bindAddr;
    protected Integer bindPort;
    protected Boolean listening;
    protected AsynchronousServerSocketChannel serverSockChannel;

    public Boolean isListening() {
        return listening;
    }

    private void setListening(Boolean listening) {
        this.listening = listening;
    }

    public Map<AsynchronousSocketChannel, HachiNIOConnection> getConnectionMap() {
        return connectionMap;
    }

    public HachiNIOServerHandler getHandler() {
        return handler;
    }

    public void setHandler(HachiNIOServerHandler handler) {
        this.handler = handler;
    }

    public HachiNIOServer(String bindAddr, int bindPort, HachiNIOServerHandler hanler){
        this.bindAddr = bindAddr;
        this.bindPort = bindPort;
        this.handler = hanler;
        this.connectionMap = new HashMap<>();
    }

    public void listen() throws IOException, InterruptedException {
        HachiNIOServer instance = this;
        InetSocketAddress inetAddr = new InetSocketAddress(this.bindAddr, this.bindPort);

        this.serverSockChannel =  AsynchronousServerSocketChannel.open().bind(inetAddr);

        this.serverSockChannel.accept(this.serverSockChannel, new CompletionHandler<AsynchronousSocketChannel,AsynchronousServerSocketChannel >() {

            @Override
            public void completed(AsynchronousSocketChannel clientSockChannel, AsynchronousServerSocketChannel serverSockChannel ) {

                HachiNIOConnection connection = new HachiNIOConnection(ByteBuffer.allocate(512), new ServerReadCompletionHandler(instance), new ServerWriteCompletionHandler(instance), clientSockChannel);

                connectionMap.put(clientSockChannel, connection);
                instance.getHandler().onConnect(connection);

                if (serverSockChannel.isOpen()) {
                    serverSockChannel.accept(serverSockChannel, this);
                }

                clientSockChannel.read(
                        connectionMap.get(clientSockChannel).getSocketByteBuffer(),
                        clientSockChannel,
                        connectionMap.get(clientSockChannel).getReadCompleteHandler()
                );
            }

            @Override
            public void failed(Throwable ex, AsynchronousServerSocketChannel serverChannel) {
                instance.getHandler().onServerError(ex, serverChannel);
            }

        });

        this.listening = true;
        ProcessUtil.attach(this);
    }

    @Override
    public boolean isActive() {
        return this.isListening();
    }

    @Override
    public void stop() throws IOException {
        this.serverSockChannel.close();
        for (AsynchronousSocketChannel activeClient : connectionMap.keySet()) {
            activeClient.close();
        }
        this.connectionMap.clear();
        this.setListening(false);
    }
}
