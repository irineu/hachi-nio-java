package com.irineuantunes.hachinio.network;

import javax.net.ssl.SSLEngine;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class HachiNIOTLSConnection extends HachiNIOConnection{

    private ByteBuffer rawReadSocketByteBuffer;
    private ByteBuffer rawWriteSocketByteBuffer;
    private SSLEngine engine;

    public HachiNIOTLSConnection(ByteBuffer socketByteBuffer,
                                 ByteBuffer rawReadSocketByteBuffer,
                                 ByteBuffer rawWriteSocketByteBuffer,
                                 CompletionHandler<Integer, AsynchronousSocketChannel> readCompleteHandler,
                                 CompletionHandler<Integer, AsynchronousSocketChannel> writeCompletionHandler,
                                 AsynchronousSocketChannel channel) {
        super(socketByteBuffer, readCompleteHandler, writeCompletionHandler, channel);
        this.rawReadSocketByteBuffer = rawReadSocketByteBuffer;
        this.rawWriteSocketByteBuffer = rawWriteSocketByteBuffer;
    }

    public void setSSLEngine(SSLEngine engine) {
        this.engine = engine;
    }

    public SSLEngine getEngine() {
        return engine;
    }

    public ByteBuffer getRawReadSocketByteBuffer() {
        return rawReadSocketByteBuffer;
    }

    public ByteBuffer getRawWriteSocketByteBuffer() {
        return rawWriteSocketByteBuffer;
    }

    public void setRawReadSocketByteBuffer(ByteBuffer replaceBuffer) {
        this.rawReadSocketByteBuffer = replaceBuffer;
    }
}
