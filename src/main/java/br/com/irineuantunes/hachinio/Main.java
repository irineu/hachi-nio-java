package br.com.irineuantunes.hachinio;

import br.com.irineuantunes.hachinio.handlers.HachiNIOHandler;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try {
            HachiNIOServer server = new HachiNIOServer("127.0.0.1", 3575, new HachiNIOHandler() {
                @Override
                public void onConnect(AsynchronousSocketChannel channel) {
                    System.out.println("on connect");
                }

                @Override
                public void onDisconnect(AsynchronousSocketChannel channel) {
                    System.out.println("on disconnect");
                }

                @Override
                public void onClientError(Throwable ex, AsynchronousSocketChannel channel) {
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
                public void onMessage(AsynchronousSocketChannel channel, Map header, byte[] message) {
                    System.out.println("on message");
                }
            });

            server.listen();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                        server.close();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


            HachiNIOServer server2 = new HachiNIOServer("127.0.0.1", 3575, new HachiNIOHandler() {
                @Override
                public void onConnect(AsynchronousSocketChannel channel) {
                    System.out.println("on connect");
                }

                @Override
                public void onDisconnect(AsynchronousSocketChannel channel) {
                    System.out.println("on disconnect");
                }

                @Override
                public void onClientError(Throwable ex, AsynchronousSocketChannel channel) {
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
                public void onMessage(AsynchronousSocketChannel channel, Map header, byte[] message) {
                    System.out.println("on message");
                }
            });

            server2.listen();



        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
