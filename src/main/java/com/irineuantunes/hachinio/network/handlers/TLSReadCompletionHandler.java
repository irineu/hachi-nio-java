package com.irineuantunes.hachinio.network.handlers;

import com.irineuantunes.hachinio.HachiNIO;
import com.irineuantunes.hachinio.HachiNIOClient;
import com.irineuantunes.hachinio.HachiNIOTLSClient;
import com.irineuantunes.hachinio.network.HachiNIOTLSConnection;
import com.irineuantunes.hachinio.util.SSLUtil;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TLSReadCompletionHandler  implements CompletionHandler<Integer, AsynchronousSocketChannel> {

    protected HachiNIO hachiNIO;
    protected ExecutorService executor = Executors.newSingleThreadExecutor();
    protected abstract void unwrapMessage(Integer readResult, HachiNIOTLSConnection connection);

    protected void doSSLCyle(Integer readResult, HachiNIOTLSConnection connection) {
        SSLEngineResult.HandshakeStatus handshakeStatus = connection.getEngine().getHandshakeStatus();
        System.out.println(handshakeStatus);
        if(handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING){
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    handshakeUnwrap(readResult, connection);
                    break;
                case NEED_WRAP:
                    wrap(readResult, connection);
                    break;
                case NEED_TASK:
                    task(readResult, connection);
                    break;
                default:
                    System.out.println(handshakeStatus);
            }
        }else if(handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED){
            this.unwrapMessage(readResult, connection);

            if(this.hachiNIO instanceof HachiNIOClient) {
                if (!((HachiNIOTLSClient) this.hachiNIO).isHandshaked()) {
                    ((HachiNIOTLSClient) this.hachiNIO).setHandshaked(true);
                    this.hachiNIO.getHandler().onConnect(((HachiNIOClient)hachiNIO).getConnection());
                }
            }
        }
    }

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

    protected void handleBufferUnderflow(HachiNIOTLSConnection connection) {
        if (connection.getEngine().getSession().getPacketBufferSize() < connection.getRawReadSocketByteBuffer().limit()) {
            System.out.println("read more");
        } else {
            ByteBuffer replaceBuffer = SSLUtil.enlargePacketBuffer(connection, connection.getRawReadSocketByteBuffer());
            connection.getRawReadSocketByteBuffer().flip();
            replaceBuffer.put(connection.getRawReadSocketByteBuffer());
            connection.setRawReadSocketByteBuffer(replaceBuffer);
            System.out.println("buffer enlarged");
        }

        this.readNext(connection);
    }

    protected void wrap(Integer readResult, HachiNIOTLSConnection connection) {
        connection.getRawWriteSocketByteBuffer().clear();


        try {
            //OBS: getSocketByteBuffer == vazio
            SSLEngineResult result = connection.getEngine().wrap(connection.getSocketByteBuffer(), connection.getRawWriteSocketByteBuffer());

            switch (result.getStatus()){
                case OK:
                    System.out.println("wrap ok, ready to write");
                    connection.getRawWriteSocketByteBuffer().flip();
                    connection.getSocketChannel().write(connection.getRawWriteSocketByteBuffer());
                    doSSLCyle(readResult, connection);
                    //this.readNext(connection);
                    break;
                case BUFFER_UNDERFLOW:
                    throw new RuntimeException("Write underflow");
                case BUFFER_OVERFLOW:
                    System.out.println("OVERFLOW " + connection.getRawWriteSocketByteBuffer().array().length + " " + connection.getEngine().getSession().getPacketBufferSize() );

                    int currentCapacity = connection.getRawWriteSocketByteBuffer().array().length;
                    int proposedCapacity = connection.getEngine().getSession().getPacketBufferSize();;//connection.getEngine().getSession().getApplicationBufferSize();

                    if(proposedCapacity > currentCapacity){

                    }else{
                        proposedCapacity = proposedCapacity * 2;
                    }

                    ByteBuffer bf = ByteBuffer.allocate(proposedCapacity);
                    connection.getRawWriteSocketByteBuffer().flip();
                    bf.put(connection.getRawWriteSocketByteBuffer());
                    connection.setRawWriteSocketByteBuffer(bf);

                    doSSLCyle(readResult, connection);
                    break;

                    //throw new RuntimeException("Write overflow");
                case CLOSED:
                    try{
                        connection.getRawWriteSocketByteBuffer().flip();
                        System.out.println("closed, write the remaining");
                        connection.getSocketChannel().write(connection.getRawWriteSocketByteBuffer());
                        connection.getRawReadSocketByteBuffer().clear();
                    } catch (Exception e) {
                        System.err.println("Failed to send server's CLOSE message due to socket channel's failure.");
                    }
                    doSSLCyle(readResult, connection);
                    break;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        } catch (SSLException e) {
            e.printStackTrace();
            connection.getEngine().closeOutbound();
            doSSLCyle(readResult, connection);
        }
    }

    protected void task(Integer readResult,HachiNIOTLSConnection connection){
        System.out.println("task");
        Runnable task;
        while ((task = connection.getEngine().getDelegatedTask()) != null) {
            executor.execute(task);
        }
        doSSLCyle(readResult, connection);
    }
    protected void readNext(HachiNIOTLSConnection connection) {
        try{
            connection.getSocketChannel().read(
                    connection.getRawReadSocketByteBuffer(),
                    connection.getSocketChannel(),
                    connection.getReadCompleteHandler()
            );
        }catch (ReadPendingException rpe){
            //TODO do nothing
        }

    }

}
