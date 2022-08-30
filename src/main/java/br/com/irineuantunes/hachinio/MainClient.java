package br.com.irineuantunes.hachinio;

import br.com.irineuantunes.hachinio.network.HachiNIOConnection;
import br.com.irineuantunes.hachinio.network.handlers.HachiNIOHandler;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Map;

public class MainClient {

    public static void main(String[] args) {
        HachiNIOClient client = new HachiNIOClient("127.0.0.1", 3575, new HachiNIOHandler(){

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

            }

            @Override
            public void onMessage(HachiNIOConnection connection, Map header, byte[] message) {
                System.out.println("on message");
            }

            @Override
            public void onWritten(HachiNIOConnection connection) {

            }
        });

        try {
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
