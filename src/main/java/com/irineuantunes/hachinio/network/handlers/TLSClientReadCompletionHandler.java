package com.irineuantunes.hachinio.network.handlers;

import com.irineuantunes.hachinio.HachiNIOClient;
import com.irineuantunes.hachinio.network.HachiNIOTLSConnection;
import com.irineuantunes.hachinio.network.handlers.protocol.ProtocolReader;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TLSClientReadCompletionHandler extends TLSReadCompletionHandler{

    protected ExecutorService executor = Executors.newSingleThreadExecutor();

    public TLSClientReadCompletionHandler(HachiNIOClient hachiNIOClient) {
        this.hachiNIO = hachiNIOClient;
    }

    @Override
    public void completed(Integer readResult, AsynchronousSocketChannel asynchronousSocketChannel) {
        HachiNIOTLSConnection connection = (HachiNIOTLSConnection) ((HachiNIOClient)hachiNIO).getConnection();
        doSSLCyle(readResult, connection);
    }

    public void handshake(){
        doSSLCyle(0, (HachiNIOTLSConnection) ((HachiNIOClient)hachiNIO).getConnection());
    }

    @Override
    protected void unwrapMessage(Integer readResult, HachiNIOTLSConnection connection) {
        System.out.println("message unrap " + readResult);

        if(readResult == -1){
            try {
                connection.getSocketChannel().close();
                this.hachiNIO.getHandler().onDisconnect(connection);
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
                    ProtocolReader.read(connection, hachiNIO);
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
    protected void handshakeUnwrap(Integer readResult, HachiNIOTLSConnection connection) {
        System.out.println("handshake unrap");

        if(readResult < 0){
            if (connection.getEngine().isInboundDone() && connection.getEngine().isOutboundDone()) {
                throw new RuntimeException("Handshake problem");
            }

            try {
                connection.getEngine().closeInbound();
            } catch (SSLException e) {
                System.err.println("This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.");
            }
            connection.getEngine().closeOutbound();
            doSSLCyle(readResult, connection);
            return;
        }

        connection.getRawReadSocketByteBuffer().flip();
        try {
            SSLEngineResult result = connection.getEngine().unwrap(connection.getRawReadSocketByteBuffer(), connection.getSocketByteBuffer());
            connection.getRawReadSocketByteBuffer().compact();

            switch(result.getStatus()){
                case OK:
                    System.out.println("unwrap OK");
                    doSSLCyle(readResult, connection);
                    break;
                case BUFFER_UNDERFLOW:
                    System.out.println("unrap underflow");
                    this.handleBufferUnderflow(connection);
                    break;
                case BUFFER_OVERFLOW:
                    throw new RuntimeException("Read overflow");
                case CLOSED:
                    if (connection.getEngine().isOutboundDone()) {
                        throw new RuntimeException("Handshake problem");
                    }else{
                        connection.getEngine().closeOutbound();
                        doSSLCyle(readResult, connection);
                    }
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
