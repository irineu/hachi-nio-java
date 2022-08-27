package br.com.irineuantunes.hachinio;

import br.com.irineuantunes.hachinio.handlers.HachiNIOHandler;
import br.com.irineuantunes.hachinio.handlers.ServerReadCompletionHandler;
import br.com.irineuantunes.hachinio.util.ProcessUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class HachiNIOServer implements HachiNIO {

    private Map<AsynchronousSocketChannel, ByteBuffer> socketMessageMap;
    private Map<AsynchronousSocketChannel, ServerReadCompletionHandler> readCompleteHandlerMap;
    private HachiNIOHandler handler;
    private String bindAddr;
    private Integer bindPort;
    private Boolean listening;
    private AsynchronousServerSocketChannel serverSockChannel;

    public Boolean isListening() {
        return listening;
    }

    private void setListening(Boolean listening) {
        this.listening = listening;
    }

    public Map<AsynchronousSocketChannel, ByteBuffer> getSocketMessageMap() {
        return socketMessageMap;
    }

    public void setSocketMessageMap(Map<AsynchronousSocketChannel, ByteBuffer> socketMessageMap) {
        this.socketMessageMap = socketMessageMap;
    }

    public Map<AsynchronousSocketChannel, ServerReadCompletionHandler> getReadCompleteHandlerMap() {
        return readCompleteHandlerMap;
    }

    public void setReadCompleteHandlerMap(Map<AsynchronousSocketChannel, ServerReadCompletionHandler> readCompleteHandlerMap) {
        this.readCompleteHandlerMap = readCompleteHandlerMap;
    }

    public HachiNIOHandler getHandler() {
        return handler;
    }

    public void setHandler(HachiNIOHandler handler) {
        this.handler = handler;
    }

    public HachiNIOServer(String bindAddr, int bindPort, HachiNIOHandler hanler){
        this.bindAddr = bindAddr;
        this.bindPort = bindPort;
        this.handler = hanler;

        this.socketMessageMap = new HashMap<>();
        this.readCompleteHandlerMap = new HashMap<>();
    }

    private void startWrite( AsynchronousSocketChannel clientSockChannel, final ByteBuffer buf) {
        HachiNIOServer instance = this;

        clientSockChannel.write(buf, clientSockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel >() {

            @Override
            public void completed(Integer result, AsynchronousSocketChannel clientSockChannel) {
                //finish to write message to client, nothing to do
            }

            @Override
            public void failed(Throwable ex, AsynchronousSocketChannel clientSockChannel) {
                instance.getHandler().onClientError(ex, clientSockChannel);
            }

        });
    }

    public void listen() throws IOException, InterruptedException {
        HachiNIOServer instance = this;
        InetSocketAddress inetAddr = new InetSocketAddress(this.bindAddr, this.bindPort);

        this.serverSockChannel =  AsynchronousServerSocketChannel.open().bind(inetAddr);

        this.serverSockChannel.accept(this.serverSockChannel, new CompletionHandler<AsynchronousSocketChannel,AsynchronousServerSocketChannel >() {

            @Override
            public void completed(AsynchronousSocketChannel clientSockChannel, AsynchronousServerSocketChannel serverSockChannel ) {

                socketMessageMap.put(clientSockChannel, ByteBuffer.allocate(8));
                readCompleteHandlerMap.put(clientSockChannel, new ServerReadCompletionHandler(instance));

                instance.getHandler().onConnect(clientSockChannel);

                if (serverSockChannel.isOpen()) {
                    serverSockChannel.accept(serverSockChannel, this);
                }

                clientSockChannel.read(socketMessageMap.get(clientSockChannel), clientSockChannel, readCompleteHandlerMap.get(clientSockChannel));
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
    public void close() throws IOException {
        this.serverSockChannel.close();
        for (AsynchronousSocketChannel activeClient : socketMessageMap.keySet()) {
            activeClient.close();
        }
        this.socketMessageMap.clear();
        this.readCompleteHandlerMap.clear();
        this.setListening(false);
    }
}
