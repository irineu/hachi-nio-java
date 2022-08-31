package com.irineuantunes.hachinio;

import com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.irineuantunes.hachinio.network.handlers.HachiNIOServerHandler;
import com.irineuantunes.hachinio.util.ByteUtil;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

public class MainTLSServer {
    public static void main(String[] args) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException, InterruptedException {

        HachiNIOTLSServer server = new HachiNIOTLSServer("127.0.0.1", 3575, new HachiNIOServerHandler() {
            @Override
            public void onConnect(HachiNIOConnection connection) {
                System.out.println("on connect");
            }

            @Override
            public void onDisconnect(HachiNIOConnection connection) {
                System.out.println("on disconnect");
                System.out.println(connection.isActive());
            }

            @Override
            public void onClientError(Throwable ex, HachiNIOConnection connection) {
                if (ex.getMessage() != null) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onServerError(Throwable ex, AsynchronousServerSocketChannel serverSockChannel) {
                if (ex.getMessage() != null) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onMessage(HachiNIOConnection connection, Map header, byte[] message) {
                System.out.println(header.get("transaction"));
                System.out.println(ByteUtil.bytesToHex(message));
                System.out.println(new String(message));

                try {
                    connection.send(header, "ok".getBytes(StandardCharsets.UTF_8));
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
    }
}
