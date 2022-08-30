package com.irineuantunes.hachinio.network.handlers.protocol;

import com.irineuantunes.hachinio.HachiNIOServer;
import com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.irineuantunes.hachinio.util.ByteUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class NIOProtocolReader {
    public static void read(HachiNIOConnection connection, HachiNIOServer hachiNIOServer) {
        try {
            byte arr [] = connection.getSocketByteBuffer().array();
            boolean retry = true;

            while(retry){
                retry = false;
                int pos = 0;

                if (connection.getMessageLength() == -1){
                    //System.out.println("will read message len");
                    int len = ByteBuffer.wrap(Arrays.copyOfRange(arr, pos, pos + ByteUtil.INT_LEN))
                            .order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

                    if(len == 0){
                        continue;
                    }

                    connection.setMessageLength(len);
                    pos += ByteUtil.INT_LEN;
                }

                if (connection.getHeaderLength() == -1){
                    //System.out.println("will read header len");
                    int len = ByteBuffer.wrap(Arrays.copyOfRange(arr, pos, pos + ByteUtil.INT_LEN))
                            .order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                    connection.setHeaderLength(len);
                    pos += ByteUtil.INT_LEN;
                    //System.out.println(socketData.getMessageLength());
                    //System.out.println(socketData.getHeaderLength());
                    //System.out.println(socketData.getDataLength());
                }

                if(connection.getHeaderOutputStream().size() < connection.getHeaderLength() && pos < arr.length){
                    //System.out.println("will read header " +socketData.getHeaderLength());
                    int end = arr.length - pos >= connection.getHeaderLength() ? connection.getHeaderLength() : arr.length;
                    connection.getHeaderOutputStream().write(Arrays.copyOfRange(arr, pos, pos+end));
                    pos += end;
                    //System.out.println(new String(socketData.getHeaderOutputStream().toString()));
                }

                if(connection.getMessageOutputStream().size() < connection.getDataLength() && pos < arr.length){
                    //System.out.println("will read message " + socketData.getDataLength());
                    int end = arr.length - pos >= connection.getDataLength() ? connection.getDataLength() : arr.length;
                    connection.getMessageOutputStream().write(Arrays.copyOfRange(arr, pos, pos+end));
                    pos += end;

                    String strHeader = new String(connection.getHeaderOutputStream().toString());
                    Map header = new Gson().fromJson(strHeader, Map.class);

                    hachiNIOServer.getHandler().onMessage(connection, header, connection.getMessageOutputStream().toByteArray());

                    connection.clear();
                }

                if(pos < arr.length){
                    arr = Arrays.copyOfRange(arr, pos, arr.length);
                    retry = true;
                }
            }
        } catch (IOException e) {
            //TODO handle this better
            e.printStackTrace();
        }
    }
}
