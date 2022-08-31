package com.irineuantunes.hachinio.network.handlers;

import com.irineuantunes.hachinio.HachiNIOServer;
import com.irineuantunes.hachinio.network.HachiNIOTLSConnection;
import com.irineuantunes.hachinio.util.ByteUtil;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TLSServerReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {

    HachiNIOServer hachiNIOServer;
    protected ExecutorService executor = Executors.newSingleThreadExecutor();

    public TLSServerReadCompletionHandler(HachiNIOServer hachiNIOServer) {
        this.hachiNIOServer = hachiNIOServer;
    }

    @Override
    public void completed(Integer readResult, AsynchronousSocketChannel channel) {
        HachiNIOTLSConnection connection = (HachiNIOTLSConnection) hachiNIOServer.getConnectionMap().get(channel);

        int appBufferSize = connection.getEngine().getSession().getApplicationBufferSize();

        ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);

        //myNetData.clear();
        //peerNetData.clear();

        doSSLCyle(readResult, connection);

        //connection.getRawSocketByteBufferMap().flip();
    }

    private void doSSLCyle(Integer readResult, HachiNIOTLSConnection connection) {
        HandshakeStatus handshakeStatus = connection.getEngine().getHandshakeStatus();

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
        }
    }

    private void unwrapMessage(Integer readResult, HachiNIOTLSConnection connection) {
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
                    System.out.println(ByteUtil.bytesToHex(connection.getSocketByteBuffer().array()));
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

    private void handshakeUnwrap(Integer readResult, HachiNIOTLSConnection connection) {
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

    private void handleBufferUnderflow(HachiNIOTLSConnection connection) {
        if (connection.getEngine().getSession().getPacketBufferSize() < connection.getRawReadSocketByteBuffer().limit()) {
            System.out.println("read more");
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(connection);
            connection.getRawReadSocketByteBuffer().flip();
            replaceBuffer.put(connection.getRawReadSocketByteBuffer());
            connection.setRawReadSocketByteBuffer(replaceBuffer);
            System.out.println("buffer enlarged");
        }

        this.readNext(connection);
    }

    private void readNext(HachiNIOTLSConnection connection) {
        connection.getSocketChannel().read(
                connection.getRawReadSocketByteBuffer(),
                connection.getSocketChannel(),
                connection.getReadCompleteHandler()
        );
    }

    protected ByteBuffer enlargePacketBuffer(HachiNIOTLSConnection connection) {
        return enlargeBuffer(connection.getRawReadSocketByteBuffer(), connection.getEngine().getSession().getPacketBufferSize());
    }

    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }

    private void wrap(Integer readResult, HachiNIOTLSConnection connection) {
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
                    throw new RuntimeException("Write overflow");
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

    private void task(Integer readResult,HachiNIOTLSConnection connection){
        System.out.println("task");
        Runnable task;
        while ((task = connection.getEngine().getDelegatedTask()) != null) {
            executor.execute(task);
        }
        doSSLCyle(readResult, connection);
    }

    @Override
    public void failed(Throwable throwable, AsynchronousSocketChannel asynchronousSocketChannel) {

    }
}
