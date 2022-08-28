package br.com.irineuantunes.hachinio.handlers;

import br.com.irineuantunes.hachinio.HachiNIOServer;
import br.com.irineuantunes.hachinio.util.ByteUtil;
import br.com.irineuantunes.hachinio.util.NIOData;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.Map;

public class ServerReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {

    HachiNIOServer hachiNIOServer;

    public ServerReadCompletionHandler(HachiNIOServer hachiNIOServer) {
        this.hachiNIOServer = hachiNIOServer;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel  ) {
        NIOData socketData = hachiNIOServer.getNioDataMap().get(channel);
        socketData.getSocketByteBuffer().flip();

        //System.out.println(socketData.getSocketByteBuffer().hasRemaining());
        //System.out.println(socketData.getSocketByteBuffer().array());

        if(result == -1){
            try {
                channel.close();
                this.hachiNIOServer.getHandler().onDisconnect(channel);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        parseProtocol(socketData, channel);

        //clear buff
        socketData.getSocketByteBuffer().clear();

        //try read next buff
        channel.read(socketData.getSocketByteBuffer(), channel, socketData.getReadCompleteHandler());
    }

    private void parseProtocol(NIOData socketData, AsynchronousSocketChannel channel) {
        try {
            byte arr [] = socketData.getSocketByteBuffer().array();
            boolean retry = true;

            while(retry){
                retry = false;
                int pos = 0;

                if (socketData.getMessageLength() == -1){
                    //System.out.println("will read message len");
                    int len = ByteBuffer.wrap(Arrays.copyOfRange(arr, pos, pos + ByteUtil.INT_LEN))
                            .order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

                    if(len == 0){
                        continue;
                    }

                    socketData.setMessageLength(len);
                    pos += ByteUtil.INT_LEN;
                }

                if (socketData.getHeaderLength() == -1){
                    //System.out.println("will read header len");
                    int len = ByteBuffer.wrap(Arrays.copyOfRange(arr, pos, pos + ByteUtil.INT_LEN))
                            .order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                    socketData.setHeaderLength(len);
                    pos += ByteUtil.INT_LEN;
                    //System.out.println(socketData.getMessageLength());
                    //System.out.println(socketData.getHeaderLength());
                    //System.out.println(socketData.getDataLength());
                }

                if(socketData.getHeaderOutputStream().size() < socketData.getHeaderLength() && pos < arr.length){
                    //System.out.println("will read header " +socketData.getHeaderLength());
                    int end = arr.length - pos >= socketData.getHeaderLength() ? socketData.getHeaderLength() : arr.length;
                    socketData.getHeaderOutputStream().write(Arrays.copyOfRange(arr, pos, pos+end));
                    pos += end;
                    //System.out.println(new String(socketData.getHeaderOutputStream().toString()));
                }

                if(socketData.getMessageOutputStream().size() < socketData.getDataLength() && pos < arr.length){
                    //System.out.println("will read message " + socketData.getDataLength());
                    int end = arr.length - pos >= socketData.getDataLength() ? socketData.getDataLength() : arr.length;
                    socketData.getMessageOutputStream().write(Arrays.copyOfRange(arr, pos, pos+end));
                    pos += end;

                    String strHeader = new String(socketData.getHeaderOutputStream().toString());
                    Map header = new Gson().fromJson(strHeader, Map.class);

                    hachiNIOServer.getHandler().onMessage(channel, header, socketData.getMessageOutputStream().toByteArray());

                    socketData.clear();
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

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel ) {
        if(!(exc instanceof AsynchronousCloseException)){
            System.out.println( "fail to read message from client");
            exc.printStackTrace();
        }

    }
}