package com.irineuantunes.hachinio.network;

import com.irineuantunes.hachinio.network.handlers.ServerReadCompletionHandler;
import com.irineuantunes.hachinio.network.handlers.ServerWriteCompletionHandler;
import com.irineuantunes.hachinio.network.handlers.protocol.NIOProtocolWritter;
import com.irineuantunes.hachinio.util.ByteUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.Objects;

public class HachiNIOConnection {

    private AsynchronousSocketChannel socketChannel;

    private ByteBuffer socketByteBuffer;
    private CompletionHandler<Integer, AsynchronousSocketChannel> readCompleteHandler;
    private CompletionHandler<Integer, AsynchronousSocketChannel> writeCompletionHandler;
    private ByteArrayOutputStream headerOutputStream = new ByteArrayOutputStream();
    private ByteArrayOutputStream messageOutputStream = new ByteArrayOutputStream();
    private int messageLength = -1;
    private int headerLength = -1;

    public HachiNIOConnection(ByteBuffer socketByteBufferMap, CompletionHandler<Integer, AsynchronousSocketChannel> readCompleteHandlerMap, CompletionHandler<Integer, AsynchronousSocketChannel> writeCompletionHandler, AsynchronousSocketChannel channel) {
        this.socketChannel = channel;

        this.socketByteBuffer = socketByteBufferMap;
        this.readCompleteHandler = readCompleteHandlerMap;
        this.writeCompletionHandler = writeCompletionHandler;
    }

    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void send(Map header, byte message[]) throws IOException {
        System.out.println("sent");
        NIOProtocolWritter.write(header, message, this, null);
    }


    public ByteArrayOutputStream getHeaderOutputStream() {
        return headerOutputStream;
    }

    public void setHeaderOutputStream(ByteArrayOutputStream headerOutputStream) {
        this.headerOutputStream = headerOutputStream;
    }

    public ByteArrayOutputStream getMessageOutputStream() {
        return messageOutputStream;
    }

    public void setMessageOutputStream(ByteArrayOutputStream messageOutputStream) {
        this.messageOutputStream = messageOutputStream;
    }

    public ByteBuffer getSocketByteBuffer() {
        return socketByteBuffer;
    }

    public void setSocketByteBuffer(ByteBuffer socketByteBuffer) {
        this.socketByteBuffer = socketByteBuffer;
    }

    public CompletionHandler<Integer, AsynchronousSocketChannel> getReadCompleteHandler() {
        return readCompleteHandler;
    }

    public void setReadCompleteHandler(ServerReadCompletionHandler readCompleteHandler) {
        this.readCompleteHandler = readCompleteHandler;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public CompletionHandler<Integer, AsynchronousSocketChannel> getWriteCompletionHandler() {
        return writeCompletionHandler;
    }

    public void setWriteCompletionHandler(ServerWriteCompletionHandler writeCompletionHandler) {
        this.writeCompletionHandler = writeCompletionHandler;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public int getDataLength() {
        return getMessageLength() - getHeaderLength() - ByteUtil.HACHI_LEN;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public void clear() {
        this.setMessageLength(-1);
        this.setHeaderLength(-1);
        this.getHeaderOutputStream().reset();
        this.getMessageOutputStream().reset();
    }

    public boolean isActive(){
        return this.socketChannel.isOpen();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HachiNIOConnection)) return false;
        HachiNIOConnection that = (HachiNIOConnection) o;
        return socketChannel.equals(that.socketChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socketChannel);
    }

}
