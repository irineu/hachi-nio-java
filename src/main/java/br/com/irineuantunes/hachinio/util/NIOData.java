package br.com.irineuantunes.hachinio.util;

import br.com.irineuantunes.hachinio.handlers.ServerReadCompletionHandler;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class NIOData {

    private ByteBuffer socketByteBuffer;
    private ServerReadCompletionHandler readCompleteHandler;
    private ByteArrayOutputStream headerOutputStream = new ByteArrayOutputStream();
    private ByteArrayOutputStream messageOutputStream = new ByteArrayOutputStream();
    private int messageLength = -1;
    private int headerLength = -1;

    public NIOData(ByteBuffer socketByteBufferMap, ServerReadCompletionHandler readCompleteHandlerMap) {
        this.socketByteBuffer = socketByteBufferMap;
        this.readCompleteHandler = readCompleteHandlerMap;
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

    public ServerReadCompletionHandler getReadCompleteHandler() {
        return readCompleteHandler;
    }

    public void setReadCompleteHandler(ServerReadCompletionHandler readCompleteHandler) {
        this.readCompleteHandler = readCompleteHandler;
    }

    public int getMessageLength() {
        return messageLength;
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
}
