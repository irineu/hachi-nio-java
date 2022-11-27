package com.irineuantunes.hachinio.network;

import com.irineuantunes.hachinio.network.handlers.protocol.ProtocolWritter;
import com.irineuantunes.hachinio.util.SSLUtil;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;

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

    @Override
    public void send(Map header, byte[] message) throws IOException {
        this.rawWriteSocketByteBuffer.clear();
        sslWriteFlow(ProtocolWritter.parseProtocol(header,message));
    }

    private void sslWriteFlow(ByteBuffer outByteBuffer) throws SSLException {
        SSLEngineResult result = engine.wrap(outByteBuffer, this.rawWriteSocketByteBuffer);

        switch (result.getStatus()) {
            case OK:
                this.rawWriteSocketByteBuffer.flip();
                getSocketChannel().write(this.rawWriteSocketByteBuffer);
                break;
            case BUFFER_OVERFLOW:
                System.out.println("reply overflow");
                SSLUtil.enlargePacketBuffer(this, this.rawWriteSocketByteBuffer);
                //now retry
                sslWriteFlow(outByteBuffer);
                break;
            case BUFFER_UNDERFLOW:
                throw new SSLException("Buffer underflow occured after a wrap. impossible situation.");
            case CLOSED:
                //TODO
                System.out.println("TODO closed");
                break;
            default:
                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
        }
    }

    public void setRawWriteSocketByteBuffer(ByteBuffer replaceBuffer) {
        this.rawWriteSocketByteBuffer = replaceBuffer;
    }
}
