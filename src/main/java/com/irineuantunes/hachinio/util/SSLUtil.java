package com.irineuantunes.hachinio.util;

import com.irineuantunes.hachinio.network.HachiNIOTLSConnection;

import java.nio.ByteBuffer;

public class SSLUtil {
    public static ByteBuffer enlargePacketBuffer(HachiNIOTLSConnection connection, ByteBuffer buf) {
        return enlargeBuffer(buf, connection.getEngine().getSession().getPacketBufferSize());
    }


    private static ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }
}
