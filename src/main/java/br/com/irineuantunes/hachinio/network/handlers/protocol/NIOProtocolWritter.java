package br.com.irineuantunes.hachinio.network.handlers.protocol;

import br.com.irineuantunes.hachinio.HachiNIOServer;
import br.com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NIOProtocolWritter {

    public static void write(Map header, byte data[], HachiNIOConnection connection, HachiNIOServer hachiNIOServer) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        String strHeader = new Gson().toJson(header);

        int totalLen = 8 + strHeader.getBytes(StandardCharsets.UTF_8).length + data.length;
        ByteBuffer szTotal = ByteBuffer.allocate(4);
        szTotal.order(ByteOrder.LITTLE_ENDIAN);
        szTotal.putInt(totalLen);

        int headerLen = strHeader.getBytes(StandardCharsets.UTF_8).length;
        ByteBuffer szHeader = ByteBuffer.allocate(4);
        szHeader.order(ByteOrder.LITTLE_ENDIAN);
        szHeader.putInt(headerLen);

        byteArrayOutputStream.write(szTotal.array());
        byteArrayOutputStream.write(szHeader.array());
        byteArrayOutputStream.write(strHeader.getBytes(StandardCharsets.UTF_8));
        byteArrayOutputStream.write(data);

        ByteBuffer buf = ByteBuffer.allocate(totalLen);
        buf.put(byteArrayOutputStream.toByteArray());
        buf.flip();

        connection.getSocketChannel().write(buf, connection.getSocketChannel(), connection.getWriteCompletionHandler());
    }

    public static void write(Map header, String data, HachiNIOConnection connection, HachiNIOServer hachiNIOServer) throws IOException {
        NIOProtocolWritter.write(header, data.getBytes(StandardCharsets.UTF_8), connection, hachiNIOServer);
    }
}
