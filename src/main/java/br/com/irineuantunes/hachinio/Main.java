package br.com.irineuantunes.hachinio;

import br.com.irineuantunes.hachinio.network.handlers.HachiNIOHandler;
import br.com.irineuantunes.hachinio.network.handlers.HachiNIOServerHandler;
import br.com.irineuantunes.hachinio.util.ByteUtil;
import br.com.irineuantunes.hachinio.network.HachiNIOConnection;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try {
            HachiNIOServer server = new HachiNIOServer("127.0.0.1", 3575, new HachiNIOServerHandler() {
                @Override
                public void onConnect(HachiNIOConnection connection) {
                    System.out.println("on connect");
                }

                @Override
                public void onDisconnect(HachiNIOConnection connection) {
                    System.out.println("on disconnect");
                }

                @Override
                public void onClientError(Throwable ex, HachiNIOConnection connection) {
                    if(ex.getMessage() != null){
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onServerError(Throwable ex, AsynchronousServerSocketChannel serverSockChannel) {
                    if(ex.getMessage() != null){
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onMessage(HachiNIOConnection connection, Map header, byte[] message) {
                    System.out.println(header.get("transaction"));
                    System.out.println(ByteUtil.bytesToHex(message));
                    try {
                        connection.send(header, message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onWritten(HachiNIOConnection connection) {
                    System.out.println("written");
                }
            });

            server.listen();

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(10000);
//                        server.close();
//                    } catch (InterruptedException | IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();


            HachiNIOServer server2 = new HachiNIOServer("127.0.0.1", 3575, new HachiNIOServerHandler() {
                @Override
                public void onConnect(HachiNIOConnection connection) {
                    System.out.println("on connect");
                }

                @Override
                public void onDisconnect(HachiNIOConnection connection) {
                    System.out.println("on disconnect");
                }

                @Override
                public void onClientError(Throwable ex, HachiNIOConnection connection) {
                    if(ex.getMessage() != null){
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onServerError(Throwable ex, AsynchronousServerSocketChannel serverSockChannel) {
                    if(ex.getMessage() != null){
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onMessage(HachiNIOConnection connection, Map header, byte[] message) {
                    System.out.println("on message");
                }

                @Override
                public void onWritten(HachiNIOConnection connection) {
                    System.out.println("written");
                }
            });

            //server2.listen();



        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
