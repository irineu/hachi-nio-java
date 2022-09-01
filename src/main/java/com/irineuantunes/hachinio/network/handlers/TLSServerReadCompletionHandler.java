package com.irineuantunes.hachinio.network.handlers;

import com.irineuantunes.hachinio.HachiNIOServer;
import com.irineuantunes.hachinio.network.HachiNIOTLSConnection;
import com.irineuantunes.hachinio.network.handlers.protocol.ProtocolReader;
import com.irineuantunes.hachinio.util.SSLUtil;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TLSServerReadCompletionHandler extends TLSReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {

    HachiNIOServer hachiNIOServer;


    public TLSServerReadCompletionHandler(HachiNIOServer hachiNIOServer) {
        this.hachiNIOServer = hachiNIOServer;
    }

    @Override
    public void completed(Integer readResult, AsynchronousSocketChannel channel) {
        HachiNIOTLSConnection connection = (HachiNIOTLSConnection) hachiNIOServer.getConnectionMap().get(channel);
        doSSLCyle(readResult, connection);
    }

    protected void readNext(HachiNIOTLSConnection connection) {
        connection.getSocketChannel().read(
                connection.getRawReadSocketByteBuffer(),
                connection.getSocketChannel(),
                connection.getReadCompleteHandler()
        );
    }

    protected void unwrapMessage(Integer readResult, HachiNIOTLSConnection connection) {
        System.out.println("message unrap");

        if(readResult == -1){
            try {
                connection.getSocketChannel().close();
                this.hachiNIOServer.getHandler().onDisconnect(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        connection.getRawReadSocketByteBuffer().flip();

        try {
            SSLEngineResult result = connection.getEngine().unwrap(connection.getRawReadSocketByteBuffer(), connection.getSocketByteBuffer());
            connection.getRawReadSocketByteBuffer().compact();
            switch(result.getStatus()){
                case OK:
                    System.out.println("unwrap OK");
                    // System.out.println(ByteUtil.bytesToHex(connection.getSocketByteBuffer().array()));
                    ProtocolReader.read(connection, hachiNIOServer);
                    connection.getSocketByteBuffer().clear();
                    this.readNext(connection);
                    break;
                case BUFFER_UNDERFLOW:
                    this.handleBufferUnderflow(connection);
                    break;
                case BUFFER_OVERFLOW:
                    throw new RuntimeException("Read overflow");
                case CLOSED:
                    System.out.println("client closed... TODO");
                default:
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        } catch (SSLException e) {
            e.printStackTrace();
            connection.getEngine().closeOutbound();
            doSSLCyle(readResult, connection);
        }
    }

    @Override
    public void failed(Throwable throwable, AsynchronousSocketChannel asynchronousSocketChannel) {

    }
}
