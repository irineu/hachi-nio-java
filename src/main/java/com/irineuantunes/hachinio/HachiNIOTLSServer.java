package com.irineuantunes.hachinio;

import com.irineuantunes.hachinio.network.HachiNIOTLSConnection;
import com.irineuantunes.hachinio.network.handlers.HachiNIOServerHandler;
import com.irineuantunes.hachinio.network.handlers.ServerWriteCompletionHandler;
import com.irineuantunes.hachinio.network.handlers.TLSServerReadCompletionHandler;
import com.irineuantunes.hachinio.util.ProcessUtil;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.*;
import java.security.cert.CertificateException;

public class HachiNIOTLSServer extends HachiNIOServer{

    private SSLContext context;

    public HachiNIOTLSServer(String bindAddr, int bindPort, SSLContext context, HachiNIOServerHandler hanler) throws NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, KeyManagementException {
        super(bindAddr, bindPort, hanler);
        this.context = context;
    }

    @Override
    public void listen() throws IOException, InterruptedException {
        HachiNIOServer instance = this;
        InetSocketAddress inetAddr = new InetSocketAddress(this.bindAddr, this.bindPort);

        this.serverSockChannel =  AsynchronousServerSocketChannel.open().bind(inetAddr);

        this.serverSockChannel.accept(this.serverSockChannel, new CompletionHandler<AsynchronousSocketChannel,AsynchronousServerSocketChannel >() {

            @Override
            public void completed(AsynchronousSocketChannel clientSockChannel, AsynchronousServerSocketChannel serverSockChannel ) {

                SSLSession dummySession = context.createSSLEngine().getSession();
                int packetBufferSize = dummySession.getPacketBufferSize();
                int applicationBufferSize = dummySession.getApplicationBufferSize();
                dummySession.invalidate();

                HachiNIOTLSConnection connection = new HachiNIOTLSConnection(ByteBuffer.allocate(applicationBufferSize),
                        ByteBuffer.allocate(packetBufferSize),
                        ByteBuffer.allocate(packetBufferSize),
                        new TLSServerReadCompletionHandler(instance),
                        new ServerWriteCompletionHandler(instance),
                        clientSockChannel
                );

                connectionMap.put(clientSockChannel, connection);

                SSLEngine engine = context.createSSLEngine();
                engine.setUseClientMode(false);
                connection.setSSLEngine(engine);

                try {
                    engine.beginHandshake();
                } catch (SSLException e) {
                    e.printStackTrace();
                }

                if (serverSockChannel.isOpen()) {
                    serverSockChannel.accept(serverSockChannel, this);
                }

                //instance.getHandler().onConnect(connection);

                clientSockChannel.read(
                        ((HachiNIOTLSConnection) connectionMap.get(clientSockChannel)).getRawReadSocketByteBuffer(),
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
}
