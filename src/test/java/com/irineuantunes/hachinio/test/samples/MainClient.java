package com.irineuantunes.hachinio.test.samples;

import com.google.gson.Gson;
import com.irineuantunes.hachinio.HachiNIOClient;
import com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.irineuantunes.hachinio.network.handlers.HachiNIOHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MainClient {

    static HachiNIOClient client;

    public static void main(String[] args) {

        client = new HachiNIOClient("127.0.0.1", 3575, new HachiNIOHandler(){

            @Override
            public void onConnect(HachiNIOConnection connection) {
                System.out.println("on connect");
                Map m = new HashMap();
                m.put("transaction", "123");

                try {
                    connection.send(m, "hello".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnect(HachiNIOConnection connection) {
                System.out.println("on disconnect");
                System.out.println(connection.isActive());
            }

            @Override
            public void onClientError(Throwable ex, HachiNIOConnection connection) {

            }

            @Override
            public void onMessage(HachiNIOConnection connection, Map header, byte[] message) {
                System.out.println("on message");
                System.out.println(new Gson().toJson(header));
                System.out.println(new String(message));
            }

            @Override
            public void onWritten(HachiNIOConnection connection) {
                System.out.println("on written");
            }
        });

        try {
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
