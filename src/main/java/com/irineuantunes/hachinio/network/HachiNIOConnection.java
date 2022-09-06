package com.irineuantunes.hachinio.network;

import com.irineuantunes.hachinio.network.handlers.protocol.ProtocolWritter;
import com.irineuantunes.hachinio.util.ByteUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.Objects;

public class HachiNIOConnection {

    protected AsynchronousSocketChannel socketChannel;

    private ByteBuffer socketByteBuffer;
    private CompletionHandler<Integer, AsynchronousSocketChannel> readCompleteHandler;
    private CompletionHandler<Integer, AsynchronousSocketChannel> writeCompletionHandler;
    private ByteArrayOutputStream headerOutputStream = new ByteArrayOutputStream();
    private ByteArrayOutputStream messageOutputStream = new ByteArrayOutputStream();
    private int messageLength = -1;
    private int headerLength = -1;

    public HachiNIOConnection(ByteBuffer socketByteBuffer,
                              CompletionHandler<Integer, AsynchronousSocketChannel> readCompleteHandler,
                              CompletionHandler<Integer, AsynchronousSocketChannel> writeCompletionHandler,
                              AsynchronousSocketChannel channel) {
        this.socketChannel = channel;
        this.socketByteBuffer = socketByteBuffer;
        this.readCompleteHandler = readCompleteHandler;
        this.writeCompletionHandler = writeCompletionHandler;
    }

    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void send(Map header, byte message[]) throws IOException {
        System.out.println("sent");
        ProtocolWritter.write(header, message, this);
    }

    public ByteArrayOutputStream getHeaderOutputStream() {
        return headerOutputStream;
    }

    public ByteArrayOutputStream getMessageOutputStream() {
        return messageOutputStream;
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

    public int getMessageLength() {
        return messageLength;
    }

    public CompletionHandler<Integer, AsynchronousSocketChannel> getWriteCompletionHandler() {
        return writeCompletionHandler;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public int getDataLength() {
        return getMessageLength() - getHeaderLength() - ByteUtil.HACHI_LEN - ByteUtil.PREFIX_BUFFER.length;
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
